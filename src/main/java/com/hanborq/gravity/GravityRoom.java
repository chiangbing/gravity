package com.hanborq.gravity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.hanborq.gravity.conf.GravityConfiguration;
import com.hanborq.gravity.conf.GravityConstants;
import com.hanborq.gravity.metrics.MetricsSender;
import com.hanborq.gravity.metrics.MetricsCollector;
import com.hanborq.gravity.metrics.MetricsSink;
import com.hanborq.gravity.thrift.TGravityRoom;
import com.hanborq.gravity.thrift.TWork;
import com.hanborq.gravity.utils.EnvHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


/**
 * GravityRoom accept and launch work from client.
 */
public class GravityRoom {
  private static final Logger LOG = LoggerFactory.getLogger(GravityRoom.class);

  private Configuration conf;

  private Thread workListener;

  /* works to be started */
  private final BlockingQueue<String> pendingWorks = Queues.newLinkedBlockingDeque();
  /* works that are running */
  private final BlockingQueue<String> runningWorks = Queues.newLinkedBlockingDeque();

  /* thread to launch work from starting works and put started work to
     runningWorks. */
  private Thread workLauncher;

  /* metrics monitor daemon */
  private Thread metricsMonitor;

  /* work status monitor daemon */
  private Thread statusMonitor;

  /* work ID to Work. */
  private Map<String, WorkRunner> runnerMap = Maps.newHashMap();

  /* work ID to MetricsSink. */
  private Map<String, MetricsSink> metricsSinkMap = Maps.newHashMap();

  /* work ID to WorkStatusReporter. */
  private Map<String, WorkStatusReporter> statusReporterMap = Maps.newHashMap();

  private boolean shouldRun = true;

  private String host = EnvHelper.currentHost();


  public GravityRoom(Configuration conf) {
    this.conf = conf;

    workLauncher = new Thread(new LaunchWorkMonitor());
    workLauncher.setName("Work Launcher");
    workLauncher.start();
    LOG.info("Work launcher started.");

    long metricsEmitInterval = conf.getLong(
            GravityConstants.GRAVITY_ROOM_METRICS_COLLECT_INTERVAL,
            GravityConstants.DEFAULT_WORKER_METRICS_COLLECT_INTERVAL);
    metricsMonitor = new Thread(new WorkMetricsMonitor(metricsEmitInterval));
    metricsMonitor.setName("Metrics Monitor");
    metricsMonitor.start();
    LOG.info("Metrics monitor started.");

    long statusReportInterval = conf.getLong(
            GravityConstants.GRAVITY_ROOM_STATUS_REPORT_INTERVAL,
            GravityConstants.DEFAULT_GRAVITY_ROOM_STATUS_REPORT_INTERVAL);
    statusMonitor = new Thread(new WorkStatusMonitor(statusReportInterval));
    statusMonitor.setName("Status Monitor");
    statusMonitor.start();
    LOG.info("Status monitor started.");

    // at last, start to accept works
    workListener = new Thread(new WorkListener());
    workListener.setName("Work Listener");
    LOG.info("Start to accept client requests.");
    workListener.start();
  }

  /** The main loop. */
  public void run() {
    while (shouldRun) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  /*
   * Remove work from gravity room's memory.
   * @param workId work ID
   */
  private void removeWork(String workId) {
    runningWorks.remove(workId);
    MetricsSink metricsSink = metricsSinkMap.remove(workId);
    if (metricsSink != null) {
      try {
        metricsSink.close();
      } catch (IOException e) {
        LOG.error("Close metrics sink for work " + workId + " failed", e);
      }
    }
    WorkStatusReporter reporter = statusReporterMap.remove(workId);
    if (reporter != null) {
      reporter.close();
    }
    runnerMap.remove(workId);
  }

  /**
   * Close this gravity room. All running works will also be stopped.
   */
  public void close() {
    shouldRun = false;
    // join all daemon
    try {
      workListener.join();
      workLauncher.join();
      metricsMonitor.join();
      statusMonitor.join();
    } catch (InterruptedException e) {
      LOG.warn("Interrupts occur when gravity room close", e);
      // ignore
    }
  }


  /**
   * Listen and accept works from GravityClients.
   */
  class WorkListener implements Runnable, TGravityRoom.Iface {
    @Override
    public void run() {
      try {
        int port = conf.getInt(GravityConstants.GRAVITY_ROOM_PORT,
                               GravityConstants.DEFAULT_GRAVITY_ROOM_PORT);
        TServerSocket serverSocket = new TServerSocket(port);
        TGravityRoom.Processor processor =
                new TGravityRoom.Processor<TGravityRoom.Iface>(this);
        TServer server = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverSocket).processor(processor));
        server.serve();
      } catch (TTransportException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void startWork(TWork twork) throws TException {
      String workId = twork.getId();
      if (runnerMap.containsKey(workId)) {
        throw new TException("Add duplicated work: " + workId);
      }
      String workClazzName = twork.getClazz();
      Map<String, String> workParams = twork.getParams();
      Configuration workConf = new Configuration(conf);
      for (Map.Entry<String, String> param : workParams.entrySet()) {
        workConf.set(param.getKey(), param.getValue());
      }
      workConf.set(GravityConstants.WORK_ID, workId);
      workConf.set(GravityConstants.WORK_CLASS, workClazzName);

      String metricsServerHost = twork.getMetricsServerHost();
      int metricsServerPort = twork.getMetricsServerPort();
      MetricsSender metricsSender =
              new MetricsSender(metricsServerHost, metricsServerPort, workConf);
      metricsSinkMap.put(workId, metricsSender);

      String statusServerHost = twork.getStatusServerHost();
      int statusServerPort = twork.getStatusServerPort();
      WorkStatusReporter statusReporter =
              new WorkStatusReporter(statusServerHost, statusServerPort);
      statusReporterMap.put(workId, statusReporter);

      try {
        WorkRunner runner = createWorkRunner(
                WorkFactory.getFactory(workConf), workConf);
        runnerMap.put(workId, runner);
      } catch (WorkException e) {
        metricsSinkMap.remove(workId);
        statusReporterMap.remove(workId);
        throw new TException("Start work failed", e);
      }

      // ready to be launched
      try {
        pendingWorks.put(workId);
      } catch (InterruptedException e) {
        LOG.error("Queue work " + workId + " error", e);
        removeWork(workId);
      }
    }

    private WorkRunner createWorkRunner(WorkFactory workFactory,
                                        Configuration conf)
            throws WorkException {
      String workRunnerClass = conf.get(GravityConstants.WORK_RUNNER_CLASS,
          GravityConstants.DEFAULT_WORK_RUNNER_CLASS);
      try {
        Class clazz = Class.forName(workRunnerClass);
        Constructor constructor = clazz.getConstructor(
            WorkFactory.class, Configuration.class);
        return (WorkRunner) constructor.newInstance(workFactory, conf);
      } catch (Exception e) {
        throw new RuntimeException("Create work runner failed", e);
      }
    }

    @Override
    public void stopWork(String workId) throws TException {
      pendingWorks.remove(workId);
      WorkRunner runner = runnerMap.get(workId);
      if (runner != null) {
        runner.stop();
      }
      removeWork(workId);
    }

    @Override
    public void pauseWork(String work_id) throws TException {
      WorkRunner runner = runnerMap.get(work_id);
      if (runner != null) {
        runner.pause();
      }
    }

    @Override
    public void resumeWork(String workId) throws TException {
      WorkRunner runner = runnerMap.get(workId);
      if (runner != null) {
        runner.resume();
      }
    }
  }


  /**
   * LaunchWorkMonitor poll works from pendingWorks and launch them.
   */
  class LaunchWorkMonitor implements Runnable {

    @Override
    public void run() {
      while (shouldRun) {
        String workId = null;
        try {
          workId = pendingWorks.take();
        } catch (InterruptedException e) {
          continue;
        }
        WorkRunner runner = runnerMap.get(workId);
        runner.start();
        try {
          runningWorks.put(workId);
        } catch (InterruptedException e) {
          LOG.error("Start work " + workId + " failed", e);
          runner.stop();
          removeWork(workId);
        }
      }
    }
  }


  /**
   * WorkMetricsMonitor periodically pull and push work metrics record.
   */
  class WorkMetricsMonitor implements Runnable {
    private MetricsCollector collector = new MetricsCollector();
    private long emitInterval;

    public WorkMetricsMonitor(long emitInterval) {
      this.emitInterval = emitInterval;
    }

    @Override
    public void run() {
      while (shouldRun) {
        for (String workId : runningWorks) {
          collector.reset();
          WorkRunner runner = runnerMap.get(workId);
          runner.pullMetrics(collector);
          MetricsSink sink = metricsSinkMap.get(workId);
          try {
            sink.pushMetrics(collector.getRecords());
          } catch (IOException e) {
            // TODO(clay.chiang) retry pushing
          }
        }

        try {
          Thread.sleep(emitInterval);
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }


  /**
   * Report work status periodically to status server.
   */
  class WorkStatusMonitor implements Runnable {

    private long reportInterval;

    public WorkStatusMonitor(long reportInterval) {
      this.reportInterval = reportInterval;
    }

    @Override
    public void run() {
      while (shouldRun) {
        // collect statuses
        Map<String, WorkStatus> statuses = Maps.newHashMap();

        for (String workId : pendingWorks) {
          WorkStatus status = new WorkStatus(
              workId, host, WorkState.PENDING, 0, 0);
          statuses.put(workId, status);
        }

        for (String workId : runningWorks) {
          WorkRunner runner = runnerMap.get(workId);
          WorkStatus status = new WorkStatus(
                  workId, host, runner.getState(), runner.getProgress(), 0);
          statuses.put(workId, status);
        }

        // send all collected statuses
        for (Map.Entry<String, WorkStatus> statusEntry : statuses.entrySet()) {
          String workId = statusEntry.getKey();
          WorkStatus status = statusEntry.getValue();
          WorkStatusReporter reporter = statusReporterMap.get(workId);
          try {
            reporter.report(status);
          } catch (IOException e) {
            LOG.error("Send work status to " + reporter.getServerAddress()
                + " failed.", e);
          }
        }

        // remove completed work from memory
        List<String> recentCompletedWorks = Lists.newArrayList();

        for (String workId : runningWorks) {
          WorkRunner runner = runnerMap.get(workId);
          if (runner.getState() == WorkState.COMPLETED) {
            LOG.info("Work " + workId + " completed.");
            recentCompletedWorks.add(workId);
          }
        }

        runningWorks.removeAll(recentCompletedWorks);
        for (String workId : recentCompletedWorks) {
          removeWork(workId);
        }

        try {
          Thread.sleep(reportInterval);
        } catch (InterruptedException e) {
          // ignore
        }
      } // while(shouldRun)
    }
  }


  public static void main(String[] args) {
    Configuration conf = GravityConfiguration.getWorkerConfiguration();
    GravityRoom gravityRoom = new GravityRoom(conf);
    gravityRoom.run();
  }
}
