package test.com.wealdtech.hawk;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.wealdtech.hawk.HawkServerConfiguration;
import com.wealdtech.hawk.jersey.HawkAuthenticationFilter;
import com.wealdtech.hawk.jersey.HawkAuthenticator;
import com.wealdtech.hawk.jersey.HawkCredentialsProvider;

/**
 * A simple HTTP server to test Hawk authentication
 */
public class GrizzlyServer
{
  @SuppressWarnings("unchecked")
  protected static HttpServer startServer() throws IllegalArgumentException, NullPointerException, IOException, URISyntaxException
  {
    // Hawk configuration
    // Configuration for the Hawk server
    final HawkServerConfiguration configuration = new HawkServerConfiguration();
    // The class which will authenticate the request against a set of credentials
    final HawkAuthenticator<TestUser> authenticator = new HawkAuthenticator<>();
    // The class which will provide Hawk credentials given a key ID
    final HawkCredentialsProvider provider = new HawkCredentialsProvider();

    final URI baseuri = new URI("http://localhost:18234/");
    final ResourceConfig rc = new PackagesResourceConfig("test.com.wealdtech.hawk.resources");
    rc.getContainerRequestFilters().add(new HawkAuthenticationFilter<TestUser>(configuration, authenticator, provider));
    return GrizzlyServerFactory.createHttpServer(baseuri, rc);
  }

  public static void main(String[] args) throws InterruptedException, IOException, IllegalArgumentException, NullPointerException, URISyntaxException
  {
    final HttpServer httpServer = startServer();
    System.in.read();
    httpServer.stop();
  }
}
