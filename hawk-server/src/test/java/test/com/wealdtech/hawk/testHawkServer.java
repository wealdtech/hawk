package test.com.wealdtech.hawk;

import java.net.URI;

import org.testng.annotations.BeforeClass;

import com.wealdtech.hawk.HawkCredentials;

public class testHawkServer
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
    this.validuri1 = new URI("http://localhost:18234/testpath");
    this.validuri2 = new URI("http://localhost:18234/testpath/subpath?param1=val1&param2=val2");

    new DummyHttpServer(this.testcredentials);
  }
}
