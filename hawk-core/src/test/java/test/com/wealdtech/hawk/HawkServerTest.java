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

import com.google.common.io.BaseEncoding;
import com.wealdtech.DataError;
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.Hawk.PayloadValidation;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;
import com.wealdtech.hawk.HawkServerConfiguration;

import static org.testng.Assert.*;

public class HawkServerTest
{
  private HawkCredentials testcredentials1, testcredentials2;
  private HawkClient testclient;
  private URI validuri1, validuri2;
  private static final String BASEBEWITURI = "http://localhost:18234/helloworld";

  // Helper
  private HttpURLConnection connect(final URI uri, final String authorizationHeader, final String body) throws Exception
  {
    final HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
    connection.setRequestMethod("GET");
    if (authorizationHeader != null)
    {
      connection.setRequestProperty("Authorization", authorizationHeader);
    }
    if (body != null)
    {
      connection.setDoOutput(true);
    }
    connection.setDoInput(true);
    connection.connect();
    if (body != null)
    {
      connection.getOutputStream().write(body.getBytes());
    }
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
    this.testcredentials2 = new HawkCredentials.Builder()
                                               .keyId("kbmdu72h12xt")
                                               .key("nzvxvljms2n239w7alsaduanpet109apbisuda0bt79")
                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                               .build();
    this.testclient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    this.validuri1 = new URI("http://localhost:18234/testpath/subpath?param1=val1&param2=val2");
    this.validuri2 = new URI("http://localhost:18234/v1/usergroups/");
  }

  @Test
  public void testModel() throws Exception
  {
    // Test a default server
    final HawkServer server1 = new HawkServer.Builder().build();
    server1.toString();
    server1.hashCode();
    assertEquals(server1, server1);
    assertNotEquals(null, server1);
    assertNotEquals(server1, null);

    // Test a copied server
    HawkServer server2 = new HawkServer.Builder(server1)
                                       .configuration(new HawkServerConfiguration.Builder()
                                                                                 .payloadValidation(PayloadValidation.MANDATORY)
                                                                                 .build())
                                       .build();
    server2.toString();
    server2.hashCode();
    assertEquals(server2, server2);
    assertNotEquals(null, server2);
    assertNotEquals(server2, null);
    assertNotEquals(server1, server2);
  }

  @Test
  public void testConfigurationModel() throws Exception
  {
    // Test the default configuration
    final HawkServerConfiguration configuration1 = new HawkServerConfiguration.Builder().build();
    configuration1.toString();
    configuration1.hashCode();
    assertEquals(configuration1, configuration1);
    assertNotEquals(configuration1, null);
    assertNotEquals(null, configuration1);

    // Test a copied configuration
    final HawkServerConfiguration configuration2 = new HawkServerConfiguration.Builder(configuration1)
                                                                              .bewitAllowed(false)
                                                                              .nonceCacheSize(500L)
                                                                              .payloadValidation(PayloadValidation.MANDATORY)
                                                                              .timestampSkew(30L)
                                                                              .build();
    configuration2.toString();
    configuration2.hashCode();
    assertEquals(configuration2, configuration2);
    assertNotEquals(configuration2, null);
    assertNotEquals(null, configuration2);
    assertNotEquals(configuration2, configuration1);
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

    // Ensure that invalid nonce cache size is caught
    try
    {
      new HawkServerConfiguration.Builder().nonceCacheSize(-5L).build();
      fail("Created Hawk server configuration with invalid nonce cache");
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

    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    Thread.sleep(2000L);
    try
    {
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
      assertEquals(connection.getResponseCode(), 401);
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
      final HttpURLConnection connection = connect(this.validuri1, null, null);
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
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
      assertEquals(connection.getResponseCode(), 200);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testAuthorizationHeader2() throws Exception
  {
    // Test correct implementation with body
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    final String body = "Body of request";
    final String hash = Hawk.calculateMac(this.testcredentials1, body);
    try
    {
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", hash, null);
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, body);
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
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      authorizationHeader = authorizationHeader.replace("Hawk", "Eagle");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
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
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      authorizationHeader = authorizationHeader.replace("nonce=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
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
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      authorizationHeader = authorizationHeader.replace("ts=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
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
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      authorizationHeader = authorizationHeader.replace("mac=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
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
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      authorizationHeader = authorizationHeader.replace("id=", "invalid=");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
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
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri2, "get", null, null);
      authorizationHeader = authorizationHeader.replace("mac=\"", "mac=\"x");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader7() throws Exception
  {
    // Ensure that bad body hashes are caught
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    final String body = "Body of request";
    final String hash = Hawk.calculateMac(this.testcredentials1, "Some other text");
    try
    {
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", hash, null);
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, body);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader8() throws Exception
  {
    // Ensure that invalid timestamps are caught
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      authorizationHeader = authorizationHeader.replace("ts=\"", "ts=\"x");
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader9() throws Exception
  {
    // Ensure that a blank Hawk authorization header is caught
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      String authorizationHeader = "Hawk";
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader10() throws Exception
  {
    // Ensure that if payload hash is required that this is enforced
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, new HawkServerConfiguration.Builder().payloadValidation(PayloadValidation.MANDATORY).build());

    final String body = "Body text";
    try
    {
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, body);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidAuthorizationHeader11() throws Exception
  {
    // Ensure that if payload hash is required that this is enforced, but not if there is no body
    final SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, new HawkServerConfiguration.Builder().payloadValidation(PayloadValidation.MANDATORY).build());

    try
    {
      String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
      final HttpURLConnection connection = connect(this.validuri1, authorizationHeader, null);
      assertEquals(connection.getResponseCode(), 200);
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
      final HttpURLConnection connection = connect(testUri, null, null);
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
      final HttpURLConnection connection = connect(testUri, null, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidBewit1() throws Exception
  {
    // Test an invalid bewit due to mismatched URIs
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    try
    {
      final String bewit = Hawk.generateBewit(this.testcredentials1, this.validuri1, 120L, null);
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      final HttpURLConnection connection = connect(testUri, null, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidBewit2() throws Exception
  {
    // Test an invalid bewit due to missing Key ID
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    // Calculate expiry from ttl and current time
    Long expiry = System.currentTimeMillis() / 1000L + 120L;
    final String mac = Hawk.calculateMAC(this.testcredentials1, Hawk.AuthType.BEWIT, expiry, new URI(BASEBEWITURI), null, null, null, null);

    final StringBuffer sb = new StringBuffer(256);
    sb.append('\\');
    sb.append(String.valueOf(expiry));
    sb.append('\\');
    sb.append(mac);
    sb.append('\\');
    final String bewit = BaseEncoding.base64().encode(sb.toString().getBytes());

    try
    {
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      final HttpURLConnection connection = connect(testUri, null, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidBewit3() throws Exception
  {
    // Test an invalid bewit due to missing expiry
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    // Calculate expiry from ttl and current time
    Long expiry = System.currentTimeMillis() / 1000L + 120L;
    final String mac = Hawk.calculateMAC(this.testcredentials1, Hawk.AuthType.BEWIT, expiry, new URI(BASEBEWITURI), null, null, null, null);

    final StringBuffer sb = new StringBuffer(256);
    sb.append(this.testcredentials1.getKeyId());
    sb.append('\\');
    sb.append('\\');
    sb.append(mac);
    sb.append('\\');
    final String bewit = BaseEncoding.base64().encode(sb.toString().getBytes());

    try
    {
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      final HttpURLConnection connection = connect(testUri, null, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidBewit4() throws Exception
  {
    // Test an invalid bewit due to missing mac
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    // Calculate expiry from ttl and current time
    Long expiry = System.currentTimeMillis() / 1000L + 120L;
//    final String mac = Hawk.calculateMAC(this.testcredentials1, Hawk.AuthType.BEWIT, expiry, new URI(BASEBEWITURI), null, null, null, null);

    final StringBuffer sb = new StringBuffer(256);
    sb.append(this.testcredentials1.getKeyId());
    sb.append('\\');
    sb.append(String.valueOf(expiry));
    sb.append('\\');
    sb.append('\\');
    final String bewit = BaseEncoding.base64().encode(sb.toString().getBytes());

    try
    {
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      final HttpURLConnection connection = connect(testUri, null, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }

  @Test
  public void testInvalidBewit5() throws Exception
  {
    // Test an invalid bewit due to malforation
    SimpleHttpServer server = new SimpleHttpServer(this.testcredentials1, null);

    // Calculate expiry from ttl and current time
    Long expiry = System.currentTimeMillis() / 1000L + 120L;
    final String mac = Hawk.calculateMAC(this.testcredentials1, Hawk.AuthType.BEWIT, expiry, new URI(BASEBEWITURI), null, null, null, null);

    final StringBuffer sb = new StringBuffer(256);
    sb.append(this.testcredentials1.getKeyId());
    sb.append('\\');
    sb.append(String.valueOf(expiry));
    sb.append('\\');
    sb.append(mac);
    final String bewit = BaseEncoding.base64().encode(sb.toString().getBytes());

    try
    {
      URI testUri = new URI(BASEBEWITURI + "?bewit=" + bewit);
      final HttpURLConnection connection = connect(testUri, null, null);
      assertEquals(connection.getResponseCode(), 401);
    }
    finally
    {
      server.stop();
    }
  }
}
