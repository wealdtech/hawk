package test.com.wealdtech.hawk;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;

public class testHawkClient
{
  private HawkCredentials testcredentials;
  private URL validurl1, validurl2, invalidurl1, invalidurl2;

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testcredentials = new HawkCredentials.Builder()
        .keyId("dh37fgj492je")
        .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
        .algorithm("hmac-sha-256")
        .build();
    this.validurl2 = new URL("http://localhost:18234/testpath");
    this.validurl1 = new URL("http://localhost:18234/testpath/subpath?param1=val1&param2=val2");
  }

  @Test
  public void testValidRequest1() throws Exception
  {
    final HawkClient testclient = new HawkClient(this.testcredentials);
    final String authorizationheader = testclient.generateAuthorizationHeader(this.validurl1, "get", null);

    HttpURLConnection connection = (HttpURLConnection)this.validurl1.openConnection();
    connection.setRequestProperty("Authorization", authorizationheader);
    connection.setDoOutput(true);
    InputStream is = connection.getInputStream();
  }
}
