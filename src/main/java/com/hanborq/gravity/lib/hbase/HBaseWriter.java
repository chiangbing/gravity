package com.hanborq.gravity.lib.hbase;

import com.google.common.base.Stopwatch;
import com.hanborq.gravity.Work;
import com.hanborq.gravity.WorkException;
import com.hanborq.gravity.metrics.MetricsRecord;
import com.hanborq.gravity.metrics.analyzer.LatencyAnalyzer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Put records to HBase table.
 */
public class HBaseWriter extends Work {

  private static final String HBASE_TABLE_KEY = "gravity.hbase.table.name";
  private static final String DEFAULT_HBASE_TABLE = "gravity_test";
  private static final String HBASE_BATCH_SIZE_KEY = "gravity.hbase.batch.size";
  private static final long DEFAULT_HBASE_BATCH_SIZE = 100;

  private static final byte[] family = Bytes.toBytes("f");
  private static final byte[] qualifier = Bytes.toBytes("q");

  private HTable htable;

  private long batchSize;

  private GdrSim gdrSimulator = new GdrSim();

  private Stopwatch stopwatch = new Stopwatch();


  public void initialize() throws WorkException {
    Configuration conf = getConf();
    String tableName = conf.get(HBASE_TABLE_KEY, DEFAULT_HBASE_TABLE);
    try {
      this.htable = new HTable(conf, tableName);
    } catch (IOException e) {
      throw new WorkException("Create hbase table failed", e);
    }
    batchSize = conf.getLong(HBASE_BATCH_SIZE_KEY, DEFAULT_HBASE_BATCH_SIZE);
  }

  @Override
  public void run() throws WorkException {
    long timeSum = 0;

    for (long i = 0; i < batchSize; ++i) {
      try {
      Put put = nextPut();
      stopwatch.reset();
      stopwatch.start();
      htable.put(put);
      stopwatch.stop();
      timeSum += stopwatch.elapsedTime(TimeUnit.NANOSECONDS);
      } catch (IOException e) {
        throw new WorkException("HBase put failed", e);
      }
    }

    long averageTimeNs = (long) Math.ceil(timeSum / batchSize);
    MetricsRecord metricsRecord = new MetricsRecord();
    metricsRecord.addLongMetric(LatencyAnalyzer.TIME_NS_KEY, averageTimeNs);
    emitMetrics(metricsRecord);
  }

  private Put nextPut() {
    gdrSimulator.next();
    byte[] msisdn = Bytes.toBytes(gdrSimulator.msisdn);
    byte[] timestamp = Bytes.toBytes(gdrSimulator.timestamp);
    byte[] rowkey = Bytes.add(msisdn, timestamp);
    byte[] apn = Bytes.toBytes(gdrSimulator.apn);
    byte[] destip = Bytes.toBytes(gdrSimulator.destip);
    byte[] frontno = Bytes.toBytes(gdrSimulator.frontno);
    byte[] gdrtime = Bytes.toBytes(gdrSimulator.gdrtime);
    byte[] gdrtype = Bytes.toBytes(gdrSimulator.gdrtype);
    byte[] gtpver = Bytes.toBytes(gdrSimulator.gtpver);
    byte[] imsi = Bytes.toBytes(gdrSimulator.imsi);
    byte[] offset = Bytes.toBytes(gdrSimulator.offset);
    byte[] remoteno = Bytes.toBytes(gdrSimulator.remoteno);
    byte[] reqnum = Bytes.toBytes(gdrSimulator.reqnum);
    byte[] result = Bytes.toBytes(gdrSimulator.result);
    byte[] cf = MyBytesUtils.addBytes(apn, destip, frontno, gdrtime, gdrtype,
            gtpver, imsi, offset, remoteno, reqnum, result);
    Put put = new Put(rowkey);
    put.add(family, qualifier, cf);
    return put;
  }

  @Override
  public void close() throws WorkException {
    try {
      htable.close();
    } catch (IOException e) {
      throw new WorkException("Close HBase table failed", e);
    }
  }

}
