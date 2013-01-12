/*
 *    Copyright 2012 Weald Technology Trading Limited
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package test.com.wealdtech.hawk;

import static org.testng.Assert.*;

import java.net.HttpURLConnection;
import java.net.URI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;

public class HawkClientTest
{
  private SimpleHttpServer server;
  private HawkCredentials testcredentials1, testcredentials2;
  private URI validuri1, validuri2;

  // Helper
  private HttpURLConnection connect(final URI uri, final String authorizationHeader) throws Exception
  {
    final HttpURLConnection connection = (HttpURLConnection)this.validuri1.toURL().openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Authorization", authorizationHeader);
    connection.setDoOutput(true);
    connection.connect();
    return connection;
  }

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testcredentials1 = new HawkCredentials.Builder()
                                               .keyId("dh37fgj492je")
                                               .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                               .build();
    this.server = new SimpleHttpServer(this.testcredentials1, null);
    this.testcredentials2 = new HawkCredentials.Builder()
                                               .keyId("kbmdu72h12xt")
                                               .key("nzvxvljms2n239w7alsaduanpet109apbisuda0bt79")
                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                               .build();
    this.validuri1 = new URI("http://localhost:18234/testpath/subpath?param1=val1&param2=val2");
    this.validuri2 = new URI("http://localhost:18234/v1/usergroups/");
  }

  @Test
  public void testValidRequest() throws Exception
  {
    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testBlankExt() throws Exception
  {
    // Test with blank EXT data
    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, "");
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidExt() throws Exception
  {
    // Test with EXT data
    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, "some data");
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testIncorrectMethod() throws Exception
  {
    // Mismatch of HTTP method
    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testDuplicateNonce() throws Exception
  {
    // Attempt repeat requests
    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
    final HttpURLConnection connection2 = connect(this.validuri1, authorizationHeader);
    assertEquals(connection2.getResponseCode(), 401);
  }

  @Test
  public void testPrefix() throws Exception
  {
    // Check client path prefix
    final HawkClient client1 = new HawkClient(this.testcredentials1);
    assertTrue(client1.isValidFor("/test/test2"));
    assertTrue(client1.isValidFor(null));

    final HawkClient client2 = new HawkClient(this.testcredentials1, "/foo");
    assertTrue(client2.isValidFor("/foo"));
    assertFalse(client2.isValidFor("/test/test2"));

    final HawkClient client3 = new HawkClient(this.testcredentials1, "/test/");
    assertTrue(client3.isValidFor("/test/test2"));
    assertFalse(client3.isValidFor("/testtest2"));

    final HawkClient client4 = new HawkClient(this.testcredentials1, "");
    assertTrue(client4.isValidFor(""));
    assertTrue(client4.isValidFor(null));
  }
}
