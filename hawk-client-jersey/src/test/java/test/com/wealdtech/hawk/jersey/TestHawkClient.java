package test.com.wealdtech.hawk.jersey;

import static org.testng.Assert.*;

import javax.ws.rs.core.MediaType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.jersey.HawkAuthorizationFilter;

public class TestHawkClient
{
  private transient Client client;
  private transient HawkClient hawkClient;

  private static transient final String URL = "http://localhost:8080/helloworld";

  @BeforeClass
  public void setUp() throws Exception
  {
    final HawkCredentials hawkCredentials = new HawkCredentials.Builder()
                                                               .keyId("dh37fgj492je")
                                                               .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                                               .algorithm("hmac-sha-256")
                                                               .build();
    this.hawkClient = new HawkClient(hawkCredentials);

    final ClientConfig clientconfig = new DefaultClientConfig();
    this.client = Client.create(clientconfig);
    this.client.addFilter(new HawkAuthorizationFilter(this.hawkClient));
  }

  @Test
  public void testBasic()
  {
    final WebResource resource = this.client.resource(URL);
    try
    {
      final String result = resource.accept(MediaType.TEXT_PLAIN).get(String.class);
      System.out.println("Result is " + result);
    }
    catch (UniformInterfaceException uie)
    {
      fail(uie.getResponse().getClientResponseStatus().getReasonPhrase());
    }
  }
}
