package test.com.wealdtech.hawk;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.wealdtech.hawk.HawkServerConfiguration;
import com.wealdtech.hawk.jersey.HawkAuthenticationFilter;

/**
 * A simple HTTP server to test Hawk authentication
 */
public class GrizzlyServer
{
  protected static HttpServer startServer() throws IllegalArgumentException, NullPointerException, IOException, URISyntaxException
  {
    HawkServerConfiguration configuration = new HawkServerConfiguration();
    URI baseuri = new URI("http://localhost:18234/");
    ResourceConfig rc = new PackagesResourceConfig("test.com.wealdtech.hawk.resources");
    rc.getContainerRequestFilters().add(new HawkAuthenticationFilter(configuration));
    return GrizzlyServerFactory.createHttpServer(baseuri, rc);
  }

  public static void main(String[] args) throws InterruptedException, IOException, IllegalArgumentException, NullPointerException, URISyntaxException
  {
    final HttpServer httpServer = startServer();
    System.in.read();
    httpServer.stop();
  }
}
