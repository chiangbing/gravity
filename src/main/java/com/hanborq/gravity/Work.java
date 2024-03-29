package com.hanborq.gravity;

import com.hanborq.gravity.metrics.MetricsBuffer;
import com.hanborq.gravity.metrics.MetricsCollector;
import com.hanborq.gravity.metrics.MetricsRecord;
import com.hanborq.gravity.metrics.MetricsSource;
import org.apache.hadoop.conf.Configuration;


/**
 * An operator execute a operation that user want to take in benchmark.
 */
public abstract class Work implements MetricsSource {

  protected String id;

  protected Configuration conf;

  /* buffer metrics record for later pulling by pullMetrics */
  private MetricsBuffer metricsBuffer = new MetricsBuffer();


  /**
   * Set ID for work. ID should be set immediately after creation, and not
   * intent to be mutable for everyone.
   * @param id work ID.
   */
  void setId(String id) {
    this.id = id;
  }

  /**
   * Return work ID, which identified the work on GravityRoom.
   * @return work ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Set configuration for work.
   * @param conf configuration
   */
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  /**
   * Get work configuration.
   * @return work configuration.
   */
  public Configuration getConf() {
    return conf;
  }

  /**
   * Initialize this work, invoke once before work's first run.
   */
  public void initialize() throws WorkException {}

  /**
   * Start to run this work.
   */
  public abstract void run() throws WorkException;

  /**
   * Invoke when runner is paused. Do nothing by default.
   */
  public void pause() throws WorkException {}

  /**
   * Invoke when runner is resumed. Do nothing by default.
   */
  public void resume() throws WorkException {}

  /**
   * Terminate this work. Do nothing by default.
   */
  public  void terminate() throws WorkException {}

  /**
   * Initialize this work, invoke once after work's last run.
   */
  public void close() throws WorkException {}

  /**
   * Emit a metrics record generated by work.
   */
  protected void emitMetrics(MetricsRecord record) {
    synchronized (metricsBuffer) {
      metricsBuffer.add(record);
    }
  }

  /**
   * Default implementation that pull metrics from buffered metrics records
   * stored by metricsBuffer. Sub-classes want to output a metrics record
   * should use {@link #emitMetrics(MetricsRecord)} instead of overriding
   * this unless you really want to.
   * @param collector
   */
  @Override
  public void pullMetrics(MetricsCollector collector) {
    synchronized (metricsBuffer) {
      for (MetricsRecord record : metricsBuffer) {
        collector.addRecord(record);
      }
      metricsBuffer.clear();
    }
  }
}
