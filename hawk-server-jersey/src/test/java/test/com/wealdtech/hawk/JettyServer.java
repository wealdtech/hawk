package test.com.wealdtech.hawk;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.com.wealdtech.hawk.jersey.guice.HawkConfigurationModule;
import test.com.wealdtech.hawk.jersey.guice.HawkServletModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * A simple HTTP server to test Hawk authentication
 */
public class JettyServer
{
  private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

  private transient Server server;

  private transient final Injector injector;

  @Inject
  public JettyServer(final Injector injector)
  {
    this.injector = injector;
  }

  public void start() throws Exception // NOPMD
  {
    // TODO configuration
//    final int port = configuration.getPort();
    final int port = 8080;
    LOGGER.info("Starting http server on port {}", port);
    this.server = new Server(port);

    // TODO Metrics
//    final ServletContextHandler admin = new ServletContextHandler(this.server, "/admin", ServletContextHandler.SESSIONS);
//    admin.addServlet(AdminServlet.class, "/*");

    final ServletContextHandler context = new ServletContextHandler(this.server, "/");
    context.addEventListener(new GuiceServletContextListener()
    {
      @Override
      protected Injector getInjector()
      {
        return JettyServer.this.injector;
      }
    });
    context.addFilter(GuiceFilter.class, "/*", null);
    context.addServlet(DefaultServlet.class, "/");

    this.server.start();
    this.server.join();
  }

  public static void main(final String[] args) throws Exception
  {
    // Create an injector with our basic configuration
    final Injector injector = Guice.createInjector(new HawkConfigurationModule(),
                                                   new HawkServletModule("test.com.wealdtech.hawk.providers",
                                                                         "test.com.wealdtech.hawk.resources"));
    final JettyServer webserver = injector.getInstance(JettyServer.class);
    webserver.start();
  }
}
