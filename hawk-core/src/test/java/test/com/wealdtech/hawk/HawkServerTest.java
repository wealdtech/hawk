/*
 *    Copyright 2012, 2013 Weald Technology Trading Limited
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

import java.net.HttpURLConnection;
import java.net.URI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.DataError;
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServerConfiguration;

import static org.testng.Assert.*;

public class HawkServerTest
{
  private HawkCredentials testcredentials1, testcredentials2;
  private URI validuri1, validuri2;
  private static final String BASEBEWITURI = "http://localhost:18234/helloworld";

  // Helper
  private HttpURLConnection connect(final URI uri, final String authorizationHeader) throws Exception
  {
    final HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
    connection.setRequestMethod("GET");
    if (authorizationHeader != null)
    {
      connection.setRequestProperty("Authorization", authorizationHeader);
    }
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
                                               .algorithm("hmac-sha-256")
                                               .build();
  this.testcredentials2 = new HawkCredentials.Builder()
                                             .keyId("kbmdu72h12xt")
                                             .key("nzvxvljms2n239w7alsaduanpet109apbisuda0bt79")
                                             .algorithm("hmac-sha-256")
                                             .build();
  this.validuri1 = new URI("http://localhost:18234/testpath/subpath?param1=val1&param2=val2");
  this.validuri2 = new URI("http://localhost:18234/v1/usergroups/");
  }

  @Test
  public void testModel() throws Exception
  {
    // Test object methods
    HawkServerConfiguration configuration1 = new HawkServerConfiguration.Builder()
                                                                        .ntpServer("magic.ntp.org")
                                                                        .build();
    configuration1.toString();
    configuration1.hashCode();
    assertEquals(configuration1, configuration1);
    assertNotEquals(null, configuration1);

    HawkServerConfiguration configuration2 = new HawkServerConfiguration.Builder().build();
    configuration2.toString();
    configuration2.hashCode();
    assertEquals(configuration2, configuration2);
    assertNotEquals(null, configuration2);
    assertNotEquals(configuration1, configuration2);
  }

  @Test
  public void testValidation() throws Exception
  {
    // Ensure that invalid timeskew is caught
    try
    {
      new HawkServerConfiguration.Builder().timestampSkew(-5L).build();
      fail("Created Hawk server configuration with invalid timestamp skew");
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testSkewConfiguration() throws Exception
  {
    // Ensure that timeout is working
    HawkServerConfiguration configuration = new HawkServerConfiguration.Builder()
                                                                       .timestampSkew(1L)
                                                                       .build();

    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, configuration);

    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    Thread.sleep(2000L);
    try
    {
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testNtpServerConfiguration() throws Exception
  {
    // Ensure that the configured NTP server is sent on failed responses
    HawkServerConfiguration configuration = new HawkServerConfiguration.Builder()
                                                                       .ntpServer("magic.ntp.org")
                                                                       .build();

    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, configuration);

    final HawkClient testclient = new HawkClient(this.testcredentials1);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", null);
    try
    {
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
      assertTrue(connection.getHeaderField("WWW-Authenticate").contains("magic.ntp.org"));
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testDuplicateConfiguration() throws Exception
  {
    // Ensure that duplicating the configuration works
    final HawkServerConfiguration configuration = new HawkServerConfiguration.Builder()
                                                                             .ntpServer("magic.ntp.org")
                                                                             .timestampSkew(123L)
                                                                             .build();
    final HawkServerConfiguration copy = new HawkServerConfiguration.Builder(configuration).build();
    assertEquals(configuration, copy);
  }

  @Test
  public void testMissingAuthorizationHeader() throws Exception
  {
    // Ensure that a missing authorization header is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HttpURLConnection connection = connect(this.validuri1, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testAuthorizationHeader() throws Exception
  {
    // Test correct implementation
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 200);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader1() throws Exception
  {
    // Ensure that an authorization header with the wrong header is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
      authorizationHeader = authorizationHeader.replaceAll("Hawk", "Eagle");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader2() throws Exception
  {
    // Ensure that an authorization header without a nonce is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
      authorizationHeader = authorizationHeader.replaceAll("nonce=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader3() throws Exception
  {
    // Ensure that an authorization header without a timestamp is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
      authorizationHeader = authorizationHeader.replaceAll("ts=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader4() throws Exception
  {
    // Ensure that an authorization header without a mac is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
      authorizationHeader = authorizationHeader.replaceAll("mac=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader5() throws Exception
  {
    // Ensure that an authorization header without an id is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
      authorizationHeader = authorizationHeader.replaceAll("id=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader6() throws Exception
  {
    // Ensure that an authorization header with an invalid mac is caught
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final HawkClient testclient = new HawkClient(this.testcredentials1);
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri2, "get", null);
      authorizationHeader = authorizationHeader.replaceAll("id=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }
  @Test
  public void testBewit() throws Exception
  {
    // Test a valid bewit
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final String bewit = Hawk.generateBewit(this.testcredentials1, new URI(BASEBEWITURI), 600L, null);
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      final HttpURLConnection connection = connect(testUri, null);
      assertEquals(connection.getResponseCode(), 200);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testExpiredBewit() throws Exception
  {
    // Test an expired bewit
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final String bewit = Hawk.generateBewit(this.testcredentials1, new URI(BASEBEWITURI), 1L, null);
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      Thread.sleep(2000);
      final HttpURLConnection connection = connect(testUri, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidBewit() throws Exception
  {
    // Test an invalid bewit
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final String bewit = Hawk.generateBewit(this.testcredentials1, this.validuri1, 120L, null);
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      Thread.sleep(2000);
      final HttpURLConnection connection = connect(testUri, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }
}
