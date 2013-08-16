package com.hanborq.gravity.metrics;

import com.google.common.collect.Lists;

import java.util.List;


/**
 * The metrics collector that collect metrics from metrics source.
 */
public class MetricsCollector {

  private List<MetricsRecord> records = Lists.newArrayList();


  public void addRecord(MetricsRecord record) {
    records.add(record);
  }

  public List<MetricsRecord> getRecords() {
    return records;
  }

  public void reset() {
    records.clear();
  }
}
