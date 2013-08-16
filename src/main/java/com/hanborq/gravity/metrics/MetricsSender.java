package com.hanborq.gravity.metrics;

import com.google.common.collect.Lists;
import com.hanborq.gravity.conf.GravityConstants;
import com.hanborq.gravity.metrics.thrift.TMetricsRecord;
import com.hanborq.gravity.metrics.thrift.TMetricsServer;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A metric sink that is responsible for sending collected metrics record to
 * its client.
 */
public class MetricsSender implements MetricsSink {
  private static final Logger LOG = LoggerFactory.getLogger(MetricsSender.class);

  private String serverHost;
  private int serverPort;

  private TMetricsServer.Client sender;
  private TTransport transport;
  private int maxSendBufferSize;
  private long maxSendBufferTime;
  private List<MetricsRecord> sendBuffer = Lists.newArrayList();
  private long lastSendTime;

  public MetricsSender(String serverHost,
                       int serverPort,
                       Configuration conf) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;

    maxSendBufferSize = conf.getInt(
            GravityConstants.CLIENT_METRICS_SENDER_BUFFER_SIZE,
            GravityConstants.DEFAULT_METRICS_SENDER_BUFFER_SIZE);
    maxSendBufferTime = conf.getLong(
            GravityConstants.CLIENT_METRICS_SENDER_BUFFER_TIME,
            GravityConstants.DEFAULT_METRICS_SENDER_BUFFER_TIME);

    transport = new TSocket(serverHost, serverPort);
    TProtocol protocol = new TBinaryProtocol(transport);
    sender = new TMetricsServer.Client(protocol);
    // connect to server
    connect();

    lastSendTime = System.currentTimeMillis();
  }

  private void connect() {
    // connect and retry
    while (true) {
      try {
        transport.open();
        break;
      } catch (TTransportException e) {
        LOG.warn("Connect to metrics server " +
                serverHost + ":" + serverPort + "failed, retry in 1 seconds.");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          // no harm, ignore
        }
      }
    }
  }

  public void close() throws IOException {
    flush();
    transport.close();
  }

  @Override
  public void pushMetrics(List<MetricsRecord> metricsRecords) throws IOException {
    sendBuffer.addAll(metricsRecords);
    // send if sendBuffer exceed buffer size or buffer time is too long
    if (sendBuffer.size() > maxSendBufferSize ||
        System.currentTimeMillis() - lastSendTime > maxSendBufferTime) {
      flush();
      lastSendTime = System.currentTimeMillis();
    }
  }

  /**
   * Convert to thrift metrics record.
   *
   * @param mr metrics record to be converted.
   * @return converted thrift metrics record.
   */
  private TMetricsRecord toThriftMetricsRecord(MetricsRecord mr) {
    TMetricsRecord tmr = new TMetricsRecord(mr.getTimestamp());
    if (mr.getLongMetrics() != null) {
      tmr.setLongMetrics(mr.getLongMetrics());
    }
    if (mr.getDoubleMetrics() != null) {
      tmr.setDoubleMetrics(mr.getDoubleMetrics());
    }
    if (mr.getBinaryMetrics() != null) {
      tmr.setBinaryMetrics(mr.getBinaryMetrics());
    }
    return tmr;
  }

  @Override
  public void flush() throws IOException {
    List<TMetricsRecord> thriftRecords =
            new ArrayList<TMetricsRecord>(sendBuffer.size());

    for (MetricsRecord metricsRecord : sendBuffer) {
      TMetricsRecord thriftRecord = toThriftMetricsRecord(metricsRecord);
      thriftRecords.add(thriftRecord);
    }
    sendBuffer.clear();

    try {
      sender.pushMetrics(thriftRecords);
    } catch (TException e) {
      throw new IOException(e);
    }
  }
}
