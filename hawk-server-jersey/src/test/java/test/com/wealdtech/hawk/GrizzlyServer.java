package test.com.wealdtech.hawk;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.wealdtech.hawk.jersey.HawkAuthenticationFilter;

public class GrizzlyServer
{
  private static URI getBaseURI()
  {
    return UriBuilder.fromUri("https://localhost/").port(getPort(4463)).build();
  }

  private static int getPort(int defaultPort)
  {
    final String port = System.getProperty("jersey.config.test.container.port");
    if (null != port)
    {
      try
      {
        return Integer.parseInt(port);
      }
      catch (NumberFormatException e)
      {
        System.out.println("Value of jersey.config.test.container.port property" + " is not a valid positive integer [" + port
            + "]." + " Reverting to default [" + defaultPort + "].");
      }
    }
    return defaultPort;
  }

  @SuppressWarnings("unchecked")
  protected static HttpServer startServer() throws IllegalArgumentException, NullPointerException, IOException
  {
    ResourceConfig rc = new PackagesResourceConfig("test.com.wealdtech.hawk.resources");
    rc.getContainerRequestFilters().add(new HawkAuthenticationFilter());
    return GrizzlyServerFactory.createHttpServer(getBaseURI(), rc);
  }

  public static void main(String[] args) throws InterruptedException, IOException
  {
    final HttpServer httpServer = startServer();
    System.in.read();
    httpServer.stop();
  }
}
