package com.hanborq.gravity;

import com.google.common.collect.Lists;
import com.hanborq.gravity.conf.GravityConstants;
import com.hanborq.gravity.metrics.MetricsCollector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DefaultWorkRunner may be used to run works in multiple threads, and in each
 * work thread, work is repeated multiple times.
 */
public class DefaultWorkRunner implements WorkRunner {
  private static Log LOG = LogFactory.getLog(DefaultWorkRunner.class);

  private WorkState status;

  /* executor service to start work threads. */
  private ExecutorService executor;

  /* work threads, each of which run one work. */
  private WorkThread[] workThreads;

  /* list of created works */
  private List<Work> works = Lists.newArrayList();


  /**
   * Constructor.
   * @param workFactory work factory.
   * @param conf runner configuration.
   * @throws WorkException
   */
  public DefaultWorkRunner(WorkFactory workFactory, Configuration conf)
          throws WorkException {
    int numThreads = conf.getInt(GravityConstants.WORK_THREADS_NUM, -1);
    if (numThreads < 0) {
      throw new WorkException(
              "no " + GravityConstants.WORK_THREADS_NUM + " set.");
    }
    executor = Executors.newFixedThreadPool(numThreads);

    long totalRepeatNum = conf.getLong(GravityConstants.WORK_REPETITION, -1);
    if (totalRepeatNum < 0) {
      throw new WorkException(
              "no " + GravityConstants.WORK_REPETITION + " set.");
    }

    workThreads = new WorkThread[numThreads];
    for (int i = 0; i < numThreads; ++i) {
      long repeatPerThread = (i == numThreads - 1)
              ? totalRepeatNum/numThreads + totalRepeatNum%numThreads
              : totalRepeatNum/numThreads;
      Work work = workFactory.createWorkInstance();
      work.initialize();
      works.add(work);
      workThreads[i] = new WorkThread(work, repeatPerThread);
    }
  }

  @Override
  public void start() {
    // TODO(clay.chiang) should mimic Hadoop TaskTracker to spawn a separate process to run works
    status = WorkState.RUNNING;
    for (WorkThread workThread : workThreads) {
      executor.submit(workThread);
    }
    // no works to start any more
    executor.shutdown();

    // start a thread to join work threads
    final WorkJoiner workJoiner = new WorkJoiner();
    workJoiner.start();
  }

  @Override
  public void pause() {
    if (!status.equals(WorkState.RUNNING)) {
      return;
    }
    status = WorkState.PAUSED;
  }

  @Override
  public void resume() {
    if (!status.equals(WorkState.PAUSED)) {
      return;
    }
    status = WorkState.RUNNING;
  }

  @Override
  public void stop() {
    status = WorkState.ABORTED;
    executor.shutdownNow();
  }

  @Override
  public WorkState getState() {
    return status;
  }

  @Override
  public double getProgress() {
    if (status == WorkState.PENDING) {
      return 0.0;
    }

    double sum = 0;
    for (WorkThread wt : workThreads) {
      sum += wt.getProgress();
    }
    return sum / workThreads.length;
  }

  /**
   * Pull metrics from all work threads.
   * @param collector metrics collector
   */
  @Override
  public void pullMetrics(MetricsCollector collector) {
    for (WorkThread workThread : workThreads) {
      workThread.getWork().pullMetrics(collector);
    }
  }


  /**
   * WorkThread run work multiple times in a separate thread.
   */
  class WorkThread implements Runnable {

    private Work work;

    private long curr = 0;

    private long repeat;

    private int exceptionCount;

    public WorkThread(Work work, long repeat) {
      this.repeat = repeat;
      this.work = work;
      this.exceptionCount = 0;
    }

    @Override
    public void run() {
      LOG.info("Start work thread for work " + work.getId());
      for (curr = 0; curr < repeat; ++curr) {
        if (status == WorkState.RUNNING) {
          try {
            work.run();
          } catch (WorkException e) {
            ++exceptionCount;
            // TODO(clay.chiang) abort this thread if too many exceptions
          }
        } else if (status == WorkState.PAUSED) {
          // wait until not paused
          while (status == WorkState.PAUSED) {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              // ignore
            }
          }
        }
      }
      LOG.info("Work thread for work " + work.getId() + " terminated.");
    }

    public int getExceptionCount() {
      return exceptionCount;
    }

    public Work getWork() {
      return work;
    }

    public double getProgress() {
      return ((double)curr) / repeat;
    }
  }


  /**
   * A thread to join work threads.
   */
  class WorkJoiner extends Thread {
    @Override
    public void run() {
      while (true) {
        try {
          executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
          // ignore
        }
        if (executor.isTerminated()) {
          break;
        }
      }
      if (status == WorkState.RUNNING) {
        status = WorkState.COMPLETED;
      }
    }
  }
}
