package com.hanborq.gravity.conf;

import org.apache.hadoop.conf.Configuration;

/**
 * Configuration utility class.
 */
public class GravityConfiguration {
  private static final String GRAVITY_DEFAULT_FILE = "gravity-default.xml";
  private static final String GRAVITY_SITE_FILE = "gravity-site.xml";
  private static final String GRAVITY_CLIENT_DEFAULT_FILE = "client-default.xml";
  private static final String GRAVITY_CLIENT_SITE_FILE = "client-site.xml";
  private static final String GRAVITY_WORKER_DEFAULT_FILE = "worker-default.xml";
  private static final String GRAVITY_WORKER_SITE_FILE = "worker-site.xml";


  public static Configuration getClientConfiguration() {
    Configuration conf = new Configuration(false);
    conf.addResource(GRAVITY_DEFAULT_FILE);
    conf.addResource(GRAVITY_SITE_FILE);
    conf.addResource(GRAVITY_CLIENT_DEFAULT_FILE);
    conf.addResource(GRAVITY_CLIENT_SITE_FILE);
    return conf;
  }

  public static Configuration getWorkerConfiguration() {
    Configuration conf = new Configuration(false);
    conf.addResource(GRAVITY_DEFAULT_FILE);
    conf.addResource(GRAVITY_SITE_FILE);
    conf.addResource(GRAVITY_WORKER_DEFAULT_FILE);
    conf.addResource(GRAVITY_WORKER_SITE_FILE);
    return conf;
  }

  public static Configuration getWorkConfiguration(String workConf) {
    Configuration conf = new Configuration(false);
    conf.addResource(workConf);
    return conf;
  }
}
