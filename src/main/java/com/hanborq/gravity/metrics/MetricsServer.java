package com.hanborq.gravity.metrics;

import com.google.common.collect.Lists;
import com.hanborq.gravity.conf.GravityConstants;
import com.hanborq.gravity.metrics.analyzer.LatencyAnalyzer;
import com.hanborq.gravity.metrics.analyzer.LatencyServlet;
import com.hanborq.gravity.metrics.thrift.TMetricsRecord;
import com.hanborq.gravity.metrics.thrift.TMetricsServer;
import com.hanborq.gravity.utils.HttpServer;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * MetricsServer that receive metrics from MetricsSender.
 */
public class MetricsServer extends Thread {
  private static final Logger LOG = LoggerFactory.getLogger(MetricsServer.class);

  private Configuration conf;

  private List<MetricsAnalyzer> analyzers = Lists.newArrayList();

  private TServer server;

  private int listenPort = -1;


  public MetricsServer(Configuration conf, HttpServer infoServer) {
    this.conf = conf;
    this.setName("Metrics Server");
    this.setDaemon(true);

    // add analyzer
    LatencyAnalyzer latencyAnalyzer = new LatencyAnalyzer(conf);
    analyzers.add(latencyAnalyzer);
    infoServer.setAttribute(LatencyAnalyzer.CTX_KEY, latencyAnalyzer);
    infoServer.addServlet("latency", "/latency", LatencyServlet.class);
  }

  @Override
  public void run() {
    int port = conf.getInt(
            GravityConstants.METRICS_SERVER_PORT_KEY,
            GravityConstants.DEFAULT_METRICS_SERVER_PORT);
    // if port is not available, increase and retry at most 1000 times
    int portEnd = Math.max(port + 1000, 65535);

    while (port <= portEnd) {
      listenPort = port;
      try {
        final TServerTransport serverSocket = new TServerSocket(listenPort);
        final TProcessor processor =
                new TMetricsServer.Processor<TMetricsServer.Iface>(new MetricsRecordHandler());
        server = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverSocket).processor(processor));
        // start to server, and will block here
        server.serve();
        break;
      } catch (TTransportException e) {
        if (e.getType() == TTransportException.ALREADY_OPEN) {
          listenPort = -1;
          // try other port
        } else {
          throw new RuntimeException("Start metrics server failed", e);
        }
      }
      // increase port and retry
      ++port;
    }

    if (listenPort < -1) {
      throw new RuntimeException(
              "Start metrics server failed due to port exhaustion from " +
                  port + " to " + portEnd + ".");
    }
  }

  /**
   * Block until server is up and serving.
   */
  public void blockUntilUp() {
    while (server == null || !server.isServing()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  /**
   * Close this server.
   */
  public void close() {
    server.stop();
    LOG.info("Metric server stopped.");
  }

  public int getListenPort() {
    return listenPort;
  }


  class MetricsRecordHandler implements TMetricsServer.Iface {

    @Override
    public void pushMetrics(List<TMetricsRecord> records) throws TException {
      if (records.isEmpty()) {
        return;
      }
      LOG.debug("Receive " + records.size() + " metrics records.");

      for (TMetricsRecord record : records) {
        MetricsRecord mr = toMetricsRecord(record);
        for (MetricsAnalyzer listener : analyzers) {
          listener.onMetricsRecord(mr);
        }
      }
    }

    /* Convert thrift TMetricsRecord to MetricsRecord.
     * @param tmr thrift metrics record to be converted.
     * @return the converted metrics record.
     */
    private MetricsRecord toMetricsRecord(TMetricsRecord tmr) {
      MetricsRecord mr = new MetricsRecord(tmr.getTimestamp());
      if (tmr.getLongMetrics() != null) {
        mr.setLongMetrics(tmr.getLongMetrics());
      }
      if (tmr.getDoubleMetrics() != null) {
        mr.setDoubleMetrics(tmr.getDoubleMetrics());
      }
      if (tmr.getBinaryMetrics() != null) {
        mr.setBinaryMetrics(tmr.getBinaryMetrics());
      }
      return mr;
    }
  }
}
