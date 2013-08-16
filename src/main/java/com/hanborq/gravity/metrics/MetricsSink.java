package com.hanborq.gravity.metrics;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface MetricsSink {
  /**
   * Put a list of  MetricsRecords to the sink.
   */
  public void pushMetrics(List<MetricsRecord> metricsRecords) throws IOException;

  /**
   * Flush any buffered metrics.
   */
  public void flush() throws IOException;

  /**
   * Close this sink.
   * @throws IOException
   */
  public void close() throws IOException;
}
