package com.hanborq.gravity.lib.demo;

import com.google.common.base.Stopwatch;
import com.hanborq.gravity.Work;
import com.hanborq.gravity.WorkException;
import com.hanborq.gravity.metrics.MetricsRecord;
import com.hanborq.gravity.metrics.analyzer.LatencyAnalyzer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A test work which sleep a random time between 0ms ~ 100ms.
 */
public class Sleeper extends Work {

  private final Stopwatch stopwatch = new Stopwatch();

  private Random random = new Random(System.currentTimeMillis());


  @Override
  public void run() throws WorkException {
    stopwatch.reset();
    stopwatch.start();
    try {
      long ms = random.nextInt(100);
      int ns = random.nextInt(1000000);
      // sleep a random time
      Thread.sleep(ms, ns);
    } catch (InterruptedException e) {
      // ignore
    }
    stopwatch.stop();
    // emit a metrics record for each sleep
    emitMetrics(new MetricsRecord()
                    .addLongMetric(
                            LatencyAnalyzer.TIME_NS_KEY,
                            stopwatch.elapsedTime(TimeUnit.NANOSECONDS)));
  }
}
