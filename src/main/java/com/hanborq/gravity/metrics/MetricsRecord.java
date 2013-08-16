package com.hanborq.gravity.metrics;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * An immutable snapshot of metrics with a timestamp.
 */
public class MetricsRecord {

  private final long timestamp;

  private Map<String, Long> longMetrics = null;
  private Map<String, Double> doubleMetrics = null;
  private Map<String, ByteBuffer> binaryMetrics = null;

  public MetricsRecord() {
    this(System.currentTimeMillis());
  }

  public MetricsRecord(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public MetricsRecord addLongMetric(String key, Long value) {
    if (longMetrics == null) {
      longMetrics = new HashMap<String, Long>();
    }
    longMetrics.put(key, value);
    return this;
  }

  public void setLongMetrics(Map<String, Long> longMetrics) {
    this.longMetrics = longMetrics;
  }

  public Map<String, Long> getLongMetrics() {
    return longMetrics;
  }

  public MetricsRecord addDoubleMetric(String key, Double value) {
    if (doubleMetrics == null) {
      doubleMetrics = new HashMap<String, Double>();
    }
    doubleMetrics.put(key, value);
    return this;
  }

  public void setDoubleMetrics(Map<String, Double> doubleMetrics) {
    this.doubleMetrics = doubleMetrics;
  }

  public Map<String, Double> getDoubleMetrics() {
    return doubleMetrics;
  }

  public MetricsRecord addBinaryMetric(String key, ByteBuffer value) {
    if (binaryMetrics == null) {
      binaryMetrics = new HashMap<String, ByteBuffer>();
    }
    binaryMetrics.put(key, value);
    return this;
  }

  public void setBinaryMetrics(Map<String, ByteBuffer> binaryMetrics) {
    this.binaryMetrics = binaryMetrics;
  }

  public Map<String, ByteBuffer> getBinaryMetrics() {
    return binaryMetrics;
  }
}
