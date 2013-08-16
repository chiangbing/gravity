package com.hanborq.gravity;

import com.google.common.collect.Maps;
import com.hanborq.gravity.thrift.TState;
import com.hanborq.gravity.thrift.TStatusServer;
import com.hanborq.gravity.thrift.TWorkStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.Map;


/**
 * WorkStatusReporter send work status report to WorkTracker.
 */
public class WorkStatusReporter {
  private Log LOG = LogFactory.getLog(WorkStatusReporter.class);

  private String serverHost;
  private int serverPort;

  private TStatusServer.Client reporter;
  private TTransport transport;

  public WorkStatusReporter(String serverHost,
                            int serverPort) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    transport = new TSocket(serverHost, serverPort);
    TProtocol protocol = new TBinaryProtocol(transport);
    reporter = new TStatusServer.Client(protocol);
    // connect to status server
    connect();
  }

  private void connect() {
    // connect and retry
    while (true) {
      try {
        transport.open();
        break;
      } catch (TTransportException e) {
        LOG.warn("Connect to status server " +
                serverHost + ":" + serverPort + "failed, retry in 1 seconds");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          // no harm, ignore
        }
      }
    }
  }

  public void report(WorkStatus workStatus) throws IOException {
    TWorkStatus tws = toThriftWorkStatus(workStatus);
    try {
      reporter.reportWorkStatus(tws);
    } catch (TException e) {
      throw new IOException("Send work status to " +
              serverHost + ":" + serverPort + "f ailed.", e);
    }
  }

  private static Map<WorkState, TState> statusConvertMap;

  static {
    statusConvertMap = Maps.newHashMap();
    statusConvertMap.put(WorkState.PENDING, TState.PENDING);
    statusConvertMap.put(WorkState.RUNNING, TState.RUNNING);
    statusConvertMap.put(WorkState.PAUSED, TState.PAUSED);
    statusConvertMap.put(WorkState.ABORTED, TState.ABORTED);
    statusConvertMap.put(WorkState.COMPLETED, TState.COMPLETED);
  }

  private TWorkStatus toThriftWorkStatus(WorkStatus ws) {
    TWorkStatus tws = new TWorkStatus();
    tws.setWorkId(ws.getWorkId())
       .setRoom(ws.getRoom())
       .setProgress(ws.getProgress())
       .setState(statusConvertMap.get(ws.getState()))
       .setErrors(ws.getErrors());
    return tws;
  }

  public String getServerAddress() {
    return serverHost + ":" + serverPort;
  }

  public void close() {
    transport.close();
  }
}
