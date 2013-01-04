package test.com.wealdtech.hawk;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;

public class TestHawkServer
{
  private HawkCredentials testcredentials;
  private URI validuri1, validuri2, invaliduri1, invaliduri2;

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testcredentials = new HawkCredentials.Builder()
                                              .keyId("dh37fgj492je")
                                              .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                              .algorithm("hmac-sha-256")
                                              .build();
    this.validuri1 = new URI("http://localhost:8080/helloworld");
  }

  @Test
  public void testBasic() throws Exception
  {
    final HawkClient testclient = new HawkClient(this.testcredentials);
    final String authorizationheader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    System.out.println(authorizationheader);
    HttpURLConnection connection = (HttpURLConnection)this.validuri1.toURL().openConnection();
    connection.setRequestProperty("Authorization", authorizationheader);
    connection.setDoOutput(true);
    InputStream is = connection.getInputStream();
  }
}
