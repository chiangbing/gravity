package com.hanborq.gravity.conf;

/**
 * Constants for Gravity.
 */
public class GravityConstants {

  public static final String DEFAULT_ROOMS_FILE = "rooms";

  public static final String GRAVITY_ROOM_PORT = "gravity.room.port";
  public static final int DEFAULT_GRAVITY_ROOM_PORT = 15900;
  public static final String GRAVITY_ROOM_METRICS_COLLECT_INTERVAL = "gravity.room.metrics.collect.interval";
  public static final long DEFAULT_WORKER_METRICS_COLLECT_INTERVAL = 1000;
  public static final String GRAVITY_ROOM_STATUS_REPORT_INTERVAL = "gravity.room.status.report.interval";
  public static final long DEFAULT_GRAVITY_ROOM_STATUS_REPORT_INTERVAL = 1000;

  public static final String METRICS_SERVER_PORT_KEY = "metrics.server.port";
  public static final int DEFAULT_METRICS_SERVER_PORT = 16900;
  public static final String CLIENT_METRICS_SENDER_BUFFER_SIZE = "metrics.sender.send.buffer.size";
  public static final int DEFAULT_METRICS_SENDER_BUFFER_SIZE = 1000;
  public static final String CLIENT_METRICS_SENDER_BUFFER_TIME = "metrics.sender.send.buffer.time";
  public static final long DEFAULT_METRICS_SENDER_BUFFER_TIME = 1000;

  public static final String STATUS_SERVER_PORT_START_KEY = "status.server.port";
  public static final int DEFAULT_STATUS_SERVER_PORT_START = 18900;

  public static final String WORK_RUNNER_CLASS = "work.runner.class";
  public static final String DEFAULT_WORK_RUNNER_CLASS = "com.hanborq.gravity.DefaultWorkRunner";

  public static final String WORK_CLASS = "work.class";
  public static final String WORK_FACTORY_CLASS = "work.factory.class";
  public static final String DEFAULT_WORK_FACTORY_CLASS = "com.hanborq.gravity.DefaultWorkFactory";

  public static final String WORK_THREADS_NUM = "work.threads.num";
  public static final String WORK_REPETITION = "work.repetition";

  public static final String INFO_SERVER_PORT = "info.server.port";
  public static final int DEFAULT_INFO_SERVER_PORT = 19900;


  // for inner usage

  public static final String WORK_ID = "work.id";
}
