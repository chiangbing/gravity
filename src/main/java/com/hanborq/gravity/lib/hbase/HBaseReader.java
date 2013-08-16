package com.hanborq.gravity.lib.hbase;

import com.google.common.base.Stopwatch;
import com.hanborq.gravity.Work;
import com.hanborq.gravity.WorkException;
import com.hanborq.gravity.metrics.MetricsRecord;
import com.hanborq.gravity.metrics.analyzer.LatencyAnalyzer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Read records from HBase table.
 */
public class HBaseReader extends Work {
  private static final String HBASE_TABLE_KEY = "gravity.hbase.table.name";
  private static final String DEFAULT_HBASE_TABLE = "gravity_test";
  private static final String HBASE_BATCH_SIZE_KEY = "gravity.hbase.batch.size";
  private static final long DEFAULT_HBASE_BATCH_SIZE = 100;

  private HTable htable;

  private long batchSize;

  private GdrSim gdrSimulator = new GdrSim();

  private Stopwatch stopwatch = new Stopwatch();


  @Override
  public void initialize( ) throws WorkException {
    Configuration conf = getConf();
    String tableName = conf.get(HBASE_TABLE_KEY, DEFAULT_HBASE_TABLE);
    try {
      this.htable = new HTable(conf, tableName);
    } catch (IOException e) {
      throw new WorkException("Create HBase table failed", e);
    }
    batchSize = conf.getLong(HBASE_BATCH_SIZE_KEY, DEFAULT_HBASE_BATCH_SIZE);
  }

  @Override
  public void run() throws WorkException {
    long timeSum = 0;

    try {
      for (long i = 0; i < batchSize; ++i) {
        stopwatch.reset();
        stopwatch.start();
        // get and ignore result
        htable.get(nextGet());
        stopwatch.stop();
        timeSum += stopwatch.elapsedTime(TimeUnit.NANOSECONDS);
      }
    } catch (IOException e) {
      throw new WorkException("HBase get failed", e);
    }

    long averageTimeNs = (long) Math.ceil(timeSum / batchSize);
    MetricsRecord metricsRecord = new MetricsRecord();
    metricsRecord.addLongMetric(LatencyAnalyzer.TIME_NS_KEY, averageTimeNs);
    emitMetrics(metricsRecord);
  }

  @Override
  public void close() throws WorkException {
    try {
      htable.close();
    } catch (IOException e) {
      throw new WorkException("Close HBase table failed", e);
    }
  }

  private Get nextGet() {
    gdrSimulator.next();
    byte[] msisdn = Bytes.toBytes(gdrSimulator.msisdn);
    byte[] timestamp = Bytes.toBytes(gdrSimulator.timestamp);
    byte[] rowkey = Bytes.add(msisdn, timestamp);
    return new Get(rowkey);
  }
}
