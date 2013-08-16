package com.hanborq.gravity.metrics;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;


/**
 * MetricsBuffer buffers metrics record for later usage.
 */
public class MetricsBuffer implements Iterable<MetricsRecord> {
  private List<MetricsRecord> buffer = Lists.newArrayList();

  /**
   * Add a metrics record to this MetricsBuffer.
   * @param record
   */
  public void add(MetricsRecord record) {
    buffer.add(record);
  }

  /**
   * Clear all records in this MetricsBuffer.
   */
  public void clear() {
    buffer.clear();
  }

  /**
   * Iterate each buffered metrics records.
   * @return iterator.
   */
  @Override
  public Iterator<MetricsRecord> iterator() {
    return buffer.iterator();
  }
}
