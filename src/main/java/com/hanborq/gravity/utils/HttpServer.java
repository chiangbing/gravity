package com.hanborq.gravity.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.BindException;
import java.net.URL;


/**
 * Create a Jetty embedded server to answer http requests.
 */
public class HttpServer {
  public static Log LOG = LogFactory.getLog(HttpServer.class);

  private final Configuration conf;

  private final Server webServer;

  private final int port;

  private final Connector listener;

  private final WebAppContext rootContext;

  private final ContextHandlerCollection contexts;

  private final String appDir;

  /**
   * Construct a local HTTP server (for info server).
   * @param conf configuration
   * @param port port to listen, if already bind, increase and retry until
   *             one is available.
   * @param rootApp app for root URL.
   */
  public HttpServer(Configuration conf, int port, String rootApp) {
    this(conf, null, port, rootApp);
  }

  /**
   * Construct a HTTP server (for info server).
   * @param conf configuration
   * @param bindAddress address to bind
   * @param port port to listen, if already bind, increase and retry until
   *             one is available
   */
  public HttpServer(Configuration conf,
                    String bindAddress,
                    int port,
                    String rootApp) {
    this.conf = conf;
    this.port = port;

    webServer = new Server();
    listener = createListener();
    listener.setHost(bindAddress);
    listener.setPort(port);

    webServer.addConnector(listener);
    webServer.setThreadPool(new QueuedThreadPool());

    appDir = getWebAppsPath();

    rootContext = new WebAppContext();

    if (rootApp != null) {
      rootContext.setDisplayName("Gravity Web App");
      rootContext.setContextPath("/");
      rootContext.setResourceBase(appDir + "/" + rootApp);
    }

    // add static resources
    WebAppContext staticContext = new WebAppContext();
    staticContext.setContextPath("/static");
    staticContext.setResourceBase(appDir + "/static");

    contexts = new ContextHandlerCollection();
    contexts.addHandler(rootContext);
    contexts.addHandler(staticContext);

    webServer.setHandler(contexts);
  }

  /**
   * Get the path name to the webapps files.
   * @return the path name as a URL
   * @throws RuntimeException if 'webapps' directory cannot be found on CLASSPATH.
   */
  protected String getWebAppsPath() {
    URL url = getClass().getClassLoader().getResource("webapps");
    if (url == null)
      throw new RuntimeException("webapps not found in CLASSPATH");
    return url.toString();
  }

  /*
   * Create a listener.
   * @return listener.
   */
  private Connector createListener() {
    SelectChannelConnector ret = new SelectChannelConnector();
    ret.setAcceptQueueSize(128);
    ret.setResolveNames(false);
    ret.setUseDirectBuffers(false);
    return ret;
  }

  /**
   * Add a servlet to server.
   * @param name servlet display name.
   * @param pathSpec path specification.
   * @param clazz servlet class.
   */
  public void addServlet(String name,
                         String pathSpec,
                         Class<? extends HttpServlet> clazz) {
    ServletHolder holder = new ServletHolder(clazz);
    if (name != null) {
      holder.setName(name);
    }
    rootContext.addServlet(holder, pathSpec);
  }

  /**
   * Add a standard servlet web app to server.
   * @param webApp web app name.
   * @param contextPath context path.
   */
  public void addWebApp(String webApp, String contextPath) {
    WebAppContext appContext = new WebAppContext(
        rootContext, webApp, contextPath);
    rootContext.addHandler(appContext);
  }

  /**
   * Set attribute.
   * @param name attribute name.
   * @param value attribute value.
   */
  public void setAttribute(String name, Object value) {
    rootContext.setAttribute(name, value);
  }

  /**
   * Start server.
   */
  public void start() throws Exception {
    while (true) {
      try {
        listener.open();
      } catch (IOException e) {
        if (!(e instanceof BindException)) {
          throw new IOException("Start HTTP server failed", e);
        } else {
          LOG.debug("Port " + listener.getPort() +
              " already bind when HTTP server starts, try port: "
              + (listener.getPort() + 1) + ".", e);
          listener.setPort(listener.getPort() + 1);
        }
      }

      Thread.sleep(5000);

      int localPort = listener.getLocalPort();
      if (localPort < 0) {
        LOG.debug("Port " + listener.getPort() +
            " already bind when HTTP server starts, try port: "
            + (listener.getPort() + 1) + ".");
        listener.setPort(listener.getPort() + 1);
      } else {
        break;
      }
    }

    int localPort = listener.getLocalPort();
    if (localPort < 0) {
      throw new IOException("Start HTTP server failed.");
    }

    webServer.start();
  }

  /**
   * Stop server.
   * @throws Exception
   */
  public void stop() throws Exception {
    listener.stop();
    webServer.stop();
  }

  /**
   * Block until server terminated.
   * @throws InterruptedException
   */
  public void join() throws InterruptedException {
    webServer.join();
  }

  /**
   * Get listen port.
   * @return listen port.
   */
  public int getListenPort() {
    return listener.getLocalPort();
  }

}
