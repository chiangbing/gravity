package com.hanborq.gravity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hanborq.gravity.conf.GravityConstants;
import com.hanborq.gravity.thrift.TState;
import com.hanborq.gravity.thrift.TStatusServer;
import com.hanborq.gravity.thrift.TWorkStatus;
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
import java.util.Map;

/**
 * WorkTracker receive status from
 */
public class WorkTracker extends Thread {
  private static final Logger LOG = LoggerFactory.getLogger(WorkTracker.class);

  private Configuration conf;

  private TServerTransport serverSocket;

  private TServer server;

  private int listenPort = -1;

  private List<WorkStatusListener> statusListeners = Lists.newArrayList();

  private Map<String, WorkStatus> trackingStatuses = Maps.newHashMap();


  public WorkTracker(Configuration conf) {
    this.conf = conf;
    this.setName("Work Tracker");
    this.setDaemon(true);
  }

  @Override
  public void run() {
    int port = conf.getInt(GravityConstants.STATUS_SERVER_PORT_START_KEY,
            GravityConstants.DEFAULT_STATUS_SERVER_PORT_START);
    int portEnd = Math.max(65535, port + 1000);

    // loop from port to portEnd to find an available port to start server
    while (port <= portEnd) {
      listenPort = port;
      try {
        serverSocket = new TServerSocket(listenPort);
        final TProcessor processor =
                new TStatusServer.Processor <TStatusServer.Iface>(
                        new WorkStatusHandler());
        server = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverSocket).processor(processor));
        // start to serve, will block here
        server.serve();
        break;
      } catch (TTransportException e) {
        if (e.getType() == TTransportException.ALREADY_OPEN) {
          listenPort = -1;
        } else {
          throw new RuntimeException("Start work status server failed", e);
        }
      }

      // increase and try another port
      ++port;
    }

    if (listenPort < -1) {
      throw new RuntimeException(
              "Start work status server failed due to port exhaustion from " +
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
   * Close this tracker.
   */
  public void close() {
    server.stop();
    serverSocket.close();
    LOG.info("Work tracker stopped.");
  }

  public int getListenPort() {
    return listenPort;
  }

  public void addStatusListener(WorkStatusListener listener) {
    statusListeners.add(listener);
  }


  private static Map<TState, WorkState> statusConvertMap;

  static {
    statusConvertMap = Maps.newHashMap();
    statusConvertMap.put(TState.PENDING, WorkState.PENDING);
    statusConvertMap.put(TState.RUNNING, WorkState.RUNNING);
    statusConvertMap.put(TState.PAUSED, WorkState.PAUSED);
    statusConvertMap.put(TState.ABORTED, WorkState.ABORTED);
    statusConvertMap.put(TState.COMPLETED, WorkState.COMPLETED);
  }

  class WorkStatusHandler implements TStatusServer.Iface {
    @Override
    public void reportWorkStatus(TWorkStatus status) throws TException {
      WorkStatus newStatus = toWorkStatus(status);
      for (WorkStatusListener listener : statusListeners) {
        listener.onWorkStatus(newStatus);
      }

      LOG.debug("Receive new work status, work=" + newStatus.getWorkId() +
          ", room=" + newStatus.getRoom() + ", status=" + newStatus.getState());

      WorkStatus oldStatus = trackingStatuses.get(newStatus.getWorkId());

      switch (newStatus.getState()) {
        case PENDING:
          if (oldStatus == null) {
            for (WorkStatusListener listener : statusListeners) {
              listener.onWorkPending(newStatus);
            }
          } else if (oldStatus.getState() != WorkState.PENDING) {
            LOG.warn("Invalid work state transition from " +
                    oldStatus.getState() + " to " + newStatus.getState());
          }
          break;
        case RUNNING:
          if (oldStatus == null || oldStatus.getState() == WorkState.PENDING) {
            for (WorkStatusListener listener : statusListeners) {
              listener.onWorkRunning(newStatus);
            }
          } else if (oldStatus.getState() == WorkState.PAUSED) {
            for (WorkStatusListener listener : statusListeners) {
              listener.onWorkResumed(newStatus);
            }
          } else {
            LOG.warn("Invalid work state transition from " +
                    oldStatus.getState() + " to " + newStatus.getState());
          }
          break;
        case PAUSED:
          if (oldStatus == null
                  || oldStatus.getState() == WorkState.RUNNING
                  || oldStatus.getState() == WorkState.PENDING) {
            for (WorkStatusListener listener : statusListeners) {
              listener.onWorkPaused(newStatus);
            }
          } else if (oldStatus.getState() == WorkState.ABORTED
                  || oldStatus.getState() == WorkState.COMPLETED) {
            LOG.warn("Invalid work state transition from " +
                    oldStatus.getState() + " to " + newStatus.getState());
          }
          break;
        case ABORTED:
          if (oldStatus == null
                  || oldStatus.getState() == WorkState.PENDING
                  || oldStatus.getState() == WorkState.PAUSED
                  || oldStatus.getState() == WorkState.RUNNING) {
            for (WorkStatusListener listener : statusListeners) {
              listener.onWorkAborted(newStatus);
            }
          }
          break;
        case COMPLETED:
          if (oldStatus == null
                  || oldStatus.getState() == WorkState.PENDING
                  || oldStatus.getState() == WorkState.PAUSED
                  || oldStatus.getState() == WorkState.RUNNING) {
            for (WorkStatusListener listener : statusListeners) {
              listener.onWorkCompleted(newStatus);
            }
          } else if (oldStatus.getState() == WorkState.ABORTED
                  || oldStatus.getState() == WorkState.ERROR
                  || oldStatus.getState() == WorkState.FATAL) {
             LOG.warn("Invalid work transition from " +
                    oldStatus.getState() + " to " + newStatus.getState());
          }
          break;
        default:
          // TODO(clay.chiang) still some states to be deal with
          break;
      }
    }

    private WorkStatus toWorkStatus(TWorkStatus tws) {
      return new WorkStatus(
              tws.getWorkId(),
              tws.getRoom(),
              statusConvertMap.get(tws.getState()),
              tws.getProgress(),
              tws.getErrors());
    }
  }
}
