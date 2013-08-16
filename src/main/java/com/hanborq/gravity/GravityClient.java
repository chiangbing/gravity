package com.hanborq.gravity;

import com.google.common.collect.Lists;
import com.hanborq.gravity.conf.GravityConfiguration;
import com.hanborq.gravity.conf.GravityConstants;
import com.hanborq.gravity.conf.RoomHostsReader;
import com.hanborq.gravity.metrics.MetricsServer;
import com.hanborq.gravity.thrift.TGravityRoom;
import com.hanborq.gravity.thrift.TWork;
import com.hanborq.gravity.utils.EnvHelper;
import com.hanborq.gravity.utils.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Each GravityClient is responsible to run a single work.
 */
public class GravityClient extends WorkStatusListener {

  private static final Log LOG = LogFactory.getLog(GravityClient.class);

  private Configuration conf;

  private List<GravityRoomConnection> connections;

  /* current host name */
  private String host;

  private String workId;

  private MetricsServer metricsServer;

  private WorkTracker workTracker;

  private HttpServer infoServer;

  private boolean isDone = false;

  private int completedCount = 0;
  private int abortedCount = 0;


  /**
   * Constructor.
   * @param conf configuration.
   */
  public GravityClient(Configuration conf) {
    this.conf = conf;
    host = EnvHelper.currentHost();

    // open connections to gravity rooms
    int port = conf.getInt(
            GravityConstants.GRAVITY_ROOM_PORT,
            GravityConstants.DEFAULT_GRAVITY_ROOM_PORT);
    RoomHostsReader workersReader;
    try {
      workersReader = new RoomHostsReader(GravityConstants.DEFAULT_ROOMS_FILE);
    } catch (IOException e) {
      throw new RuntimeException("Load worker hosts from " +
              GravityConstants.DEFAULT_ROOMS_FILE + " failed", e);
    }

    Set<String> hosts = workersReader.getWorkers();
    connections = Lists.newArrayList();
    for (String host : hosts) {
      try {
        GravityRoomConnection conn = new GravityRoomConnection(host, port);
        conn.open();
        connections.add(conn);
      } catch (IOException e) {
        // hmm.. bad gravity room
        throw new RuntimeException(
                "Connect to gravity room " + host + ":" + port + " failed", e);
      }
    }

    int infoPort = conf.getInt(GravityConstants.INFO_SERVER_PORT,
        GravityConstants.DEFAULT_INFO_SERVER_PORT);
    infoServer = new HttpServer(conf, infoPort, "client");

    // start metrics server
    metricsServer = new MetricsServer(conf, infoServer);
    metricsServer.start();

    // start work tracker
    workTracker = new WorkTracker(conf);
    workTracker.addStatusListener(this);
    workTracker.start();

    // block until servers are totally up
    metricsServer.blockUntilUp();
    LOG.info("Start metrics server at " + metricsServer.getListenPort()
        + " successfully.");
    workTracker.blockUntilUp();
    LOG.info("Start work tracker at " + workTracker.getListenPort()
        + " successfully.");

    try {
      infoServer.start();
      LOG.info("Start info server at " + infoServer.getListenPort()
          + " successfully");
    } catch (Exception e) {
      throw new RuntimeException("Start info server failed", e);
    }
  }

  /**
   * Start a work which is described by file workConfFile.
   */
  public void startWork(String workConfFile) {
    Configuration workConf =
            GravityConfiguration.getWorkConfiguration(workConfFile);
    String workClass = workConf.get(GravityConstants.WORK_CLASS);
    if (workClass == null || workClass.isEmpty()) {
      throw new RuntimeException(
              "Start work failed because of missing work class");
    }
    workId = generateWorkID();

    TWork twork = new TWork();
    twork.setId(workId)
         .setClazz(workClass)
         .setClientHost(host)
         .setMetricsServerHost(host)
         .setMetricsServerPort(metricsServer.getListenPort())
         .setStatusServerHost(host)
         .setStatusServerPort(workTracker.getListenPort());

    for (Map.Entry<String,String> param : workConf) {
      twork.putToParams(param.getKey(), param.getValue());
    }

    // start work in all gravity rooms
    List<GravityRoomConnection> failedConnections = Lists.newArrayList();
    for (GravityRoomConnection connection : connections) {
      try {
        LOG.debug("Start work at connection " + connection.getAddress());
        connection.startWork(twork);
      } catch (IOException e) {
        LOG.error(e);
        failedConnections.add(connection);
      }
    }

    if (!failedConnections.isEmpty()) {
      String failedMessage = "Start new work " + workId + " described by " +
              workConfFile + " failed for connectiones: [";
      for (GravityRoomConnection failedConnection : failedConnections) {
        failedMessage += failedConnection.getAddress();
      }
      failedMessage += "], will abort all.";
      LOG.error(failedMessage);
      // abort partially started work
      stopWork();
      throw new RuntimeException(
              "Start new work described by " + workConfFile + "failed.");
    }

    LOG.info("Start work " + workId + " successfully.");
  }

  /**
   * generate a new work ID.
   * @return a new unique work ID.
   */
  private String generateWorkID() {
    return host + "_" + System.currentTimeMillis();
  }

  /**
   * Stop works in all gravity rooms.
   * @return true if all works in all gravity rooms are stopped, or false
   *         vice vera.
   */
  public boolean stopWork() {
    List<GravityRoomConnection> failedConnections = Lists.newArrayList();
    for (GravityRoomConnection connection : connections) {
      try {
        connection.stop(workId);
      } catch (IOException e) {
        LOG.error(e);
        failedConnections.add(connection);
      }
    }

    if (!failedConnections.isEmpty()) {
      String failedMessage = "Stop work failed for connections: [";
      for (GravityRoomConnection failedConnection : failedConnections) {
        failedMessage += failedConnection.getAddress();
      }
      failedMessage += "]";
      LOG.error(failedMessage);
      return false;
    }

    return true;
  }

  /**
   * Pause works in all gravity rooms.
   * @return true if all works in all gravity rooms are paused, or false
   *         vice vera.
   */
  public boolean pauseWork() {
    List<GravityRoomConnection> failedConnections = Lists.newArrayList();
    for (GravityRoomConnection connection : connections) {
      try {
        connection.pause(workId);
      } catch (IOException e) {
        LOG.error(e);
        failedConnections.add(connection);
      }
    }

    if (!failedConnections.isEmpty()) {
      String failedMessage = "Pause work failed for connections: [";
      for (GravityRoomConnection failedConnection : failedConnections) {
        failedMessage += failedConnection.getAddress();
      }
      failedMessage += "]";
      LOG.error(failedMessage);
      return false;
    }

    return true;
  }

  /**
   * Resume works in all gravity rooms.
   * @return true if all works in all gravity rooms are resumed, or false
   *         vice vera.
   */
  public boolean resumeWork() {
    List<GravityRoomConnection> failedConnections = Lists.newArrayList();
    for (GravityRoomConnection connection : connections) {
      try {
        connection.resume(workId);
      } catch (IOException e) {
        LOG.error(e);
        failedConnections.add(connection);
      }
    }

    if (!failedConnections.isEmpty()) {
      String failedMessage = "Resume work failed for connections: [";
      for (GravityRoomConnection failedConnection : failedConnections) {
        failedMessage += failedConnection.getAddress();
      }
      failedMessage += "]";
      LOG.error(failedMessage);
      return false;
    }

    return true;
  }

  /**
   * Close client's work and daemons.
   */
  public void close() {
    for (GravityRoomConnection connection : connections) {
      try {
        connection.stop(workId);
      } catch (IOException e) {
        LOG.error("Stop work " + workId + " failed for connection "
                + connection.getAddress());
      }
      connection.close();
    }
    metricsServer.close();
    workTracker.close();

    try {
      infoServer.stop();
    } catch (Exception e) {
      throw new RuntimeException("Stop info server failed", e);
    }
  }

  @Override
  public void onWorkAborted(WorkStatus workStatus) {
    ++abortedCount;
    if (completedCount + abortedCount >= connections.size()) {
      isDone = true;
    }
  }

  @Override
  public void onWorkCompleted(WorkStatus workStatus) {
    LOG.info("Work in " + workStatus.getRoom() + " completed.");
    ++completedCount;
    if (completedCount + abortedCount >= connections.size()) {
      isDone = true;
    }
  }

  /**
   * Block until the started work is completed.
   * @return true if work is completed successfully, false vice vera.
   */
  public boolean waitForCompletion() {
    while (!isDone) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }

    // close itself
    close();

    return (abortedCount == 0);
  }

  /**
   * Configuration accessor.
   * @return configuration.
   */
  public Configuration getConf() {
    return conf;
  }

  public int getInfoPort() {
    return infoServer.getListenPort();
  }


  /**
   * A connection to GravityRoom to send command to it.
   */
  static class GravityRoomConnection {
    private String host;
    private int port;
    private TTransport transport;
    private TGravityRoom.Client client;

    public GravityRoomConnection(String host, int port) {
      this.host = host;
      this.port = port;
      transport = new TSocket(host, port);
      TProtocol protocol = new TBinaryProtocol(transport);
      client = new TGravityRoom.Client(protocol);
    }

    public void open() throws IOException {
      try {
        transport.open();
      } catch (TTransportException e) {
        throw new IOException(e);
      }
    }

    public void close() {
      transport.close();
    }

    public void startWork(TWork work) throws IOException {
      try {
        client.startWork(work);
      } catch (TException e) {
        throw new IOException("Start work " + work.getId() + " failed", e);
      }
    }

    public void stop(String workId) throws IOException {
      try {
        client.stopWork(workId);
      } catch (TException e) {
        throw new IOException("Stop work " + workId + " failed", e);
      }
    }

    public void pause(String workId) throws IOException {
      try {
        client.pauseWork(workId);
      } catch (TException e) {
        throw new IOException("Pause work " + workId + " failed", e);
      }
    }

    public void resume(String workId) throws IOException {
      try {
        client.resumeWork(workId);
      } catch (TException e) {
        throw new IOException("Resume work " + workId + " failed", e);
      }
    }

    public String getAddress() {
      return host + ":" + port;
    }
  }


  /* Test */
  public static void main(String[] args) throws MalformedURLException, InterruptedException {
    if (args.length < 1) {
      System.err.println("Error: need one argument as work config");
      System.exit(0);
    }

    String workConf = args[0];
//    List<URL> classPath = Lists.newArrayList();
//    classPath.add(new File(workConf).toURI().toURL());
//    ClassLoader classLoader = new URLClassLoader(classPath.toArray(new URL[0]),
//        GravityClient.class.getClassLoader());
//    Thread.currentThread().setContextClassLoader(classLoader);

    Configuration conf = GravityConfiguration.getClientConfiguration();
    GravityClient client = new GravityClient(conf);
    System.out.println("Info Server: http://:" + client.getInfoPort());

    Thread.sleep(10000);

    client.startWork(workConf);

    client.waitForCompletion();
  }
}
