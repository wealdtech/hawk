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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.DataError;
import com.wealdtech.configuration.ConfigurationSource;
import com.wealdtech.hawk.Hawk.PayloadValidation;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkClientConfiguration;
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

  @AfterClass
  public void tearDown() throws Exception
  {
    this.server.stop();
  }

  @Test
  public void testModel() throws Exception
  {
    final HawkClient testClient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    testClient.toString();
    testClient.hashCode();
    assertEquals(testClient, testClient);
    assertNotEquals(testClient, null);
    assertNotEquals(null, testClient);

    final HawkClient testClient2 = new HawkClient.Builder(testClient).credentials(this.testcredentials2).build();
    testClient2.toString();
    testClient2.hashCode();
    assertEquals(testClient2, testClient2);
    assertNotEquals(testClient2, testClient);
  }

  @Test
  public void testValidRequest() throws Exception
  {
    final HawkClient testClient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    final String authorizationHeader = testClient.generateAuthorizationHeader(this.validuri1, "get", null, null, null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testBlankExt() throws Exception
  {
    // Test with blank EXT data
    final HawkClient testClient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    final String authorizationHeader = testClient.generateAuthorizationHeader(this.validuri1, "get", null, "", null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidExt() throws Exception
  {
    // Test with EXT data
    final HawkClient testClient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    final String authorizationHeader = testClient.generateAuthorizationHeader(this.validuri1, "get", null, "some data", null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testIncorrectMethod() throws Exception
  {
    // Mismatch of HTTP method
    final HawkClient testClient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    final String authorizationHeader = testClient.generateAuthorizationHeader(this.validuri1, "post", null, null, null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testDuplicateNonce() throws Exception
  {
    // Attempt repeat requests
    final HawkClient testClient = new HawkClient.Builder().credentials(this.testcredentials1).build();
    final String authorizationHeader = testClient.generateAuthorizationHeader(this.validuri1, "get", null, null, null, null);
    final HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
    final HttpURLConnection connection2 = connect(this.validuri1, authorizationHeader);
    assertEquals(connection2.getResponseCode(), 401);
  }

  @Test
  public void testPrefix() throws Exception
  {
    // Check client path prefix
    final HawkClient testClient1 = new HawkClient.Builder().credentials(this.testcredentials1).build();
    assertTrue(testClient1.isValidFor("/test/test2"));
    assertTrue(testClient1.isValidFor(null));

    HawkClientConfiguration clientConfiguration = new HawkClientConfiguration.Builder()
                                                                             .pathPrefix("/foo")
                                                                             .build();
    final HawkClient testClient2 = new HawkClient.Builder()
                                             .credentials(this.testcredentials1)
                                             .configuration(clientConfiguration)
                                             .build();
    assertTrue(testClient2.isValidFor("/foo"));
    assertFalse(testClient2.isValidFor("/test/test2"));

    clientConfiguration = new HawkClientConfiguration.Builder(clientConfiguration)
                                                     .pathPrefix("/test/")
                                                     .build();
    final HawkClient testClient3 = new HawkClient.Builder()
                                             .credentials(this.testcredentials1)
                                             .configuration(clientConfiguration)
                                             .build();
    assertTrue(testClient3.isValidFor("/test/test2"));
    assertFalse(testClient3.isValidFor("/testtest2"));
  }

  @Test
  public void testConfigurationModel() throws Exception
  {
    // Test default configuration
    final HawkClientConfiguration configuration1 = new HawkClientConfiguration();
    assertNotNull(configuration1);
    configuration1.toString();
    configuration1.hashCode();
    assertEquals(configuration1, configuration1);
    assertNotEquals(null, configuration1);
    assertNotEquals(configuration1, null);

    // Test non-default configuration
    final HawkClientConfiguration configuration2 = new HawkClientConfiguration.Builder(configuration1)
                                                                              .payloadValidation(PayloadValidation.MANDATORY)
                                                                              .pathPrefix("/test")
                                                                              .build();
    assertNotNull(configuration2);
    configuration2.toString();
    configuration2.hashCode();
    assertEquals(configuration2, configuration2);
    assertNotEquals(null, configuration2);
    assertNotEquals(configuration2, null);
    assertNotEquals(configuration2, configuration1);
  }

  @Test
  public void testConfiguration() throws Exception
  {
    // Test obtaining the configuration from a valid configuration source
    final HawkClientConfiguration configuration = new ConfigurationSource<HawkClientConfiguration>().getConfiguration("clientconfig-test1.json", HawkClientConfiguration.class);
    assertNotNull(configuration);
    configuration.toString();
    configuration.hashCode();
    assertEquals(configuration, configuration);
    assertNotEquals(null, configuration);
    assertNotEquals(configuration, null);
  }

  @Test
  public void testInvalidConfiguration1() throws Exception
  {
    // Test obtaining the configuration with an invalid path prefix
    try
    {
      new ConfigurationSource<HawkClientConfiguration>().getConfiguration("clientconfig-test2.json", HawkClientConfiguration.class);
      fail("Obtained invalid client configuration");
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testInvalidConfiguration2() throws Exception
  {
    // Test obtaining the configuration with an invalid payload validation
    try
    {
      new ConfigurationSource<HawkClientConfiguration>().getConfiguration("clientconfig-test3.json", HawkClientConfiguration.class);
      fail("Obtained invalid client configuration");
    }
    catch (DataError de)
    {
      // Good
    }
  }
}
