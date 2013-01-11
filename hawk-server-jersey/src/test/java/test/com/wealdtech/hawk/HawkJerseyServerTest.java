/*
 *    Copyright 2013 Weald Technology Trading Limited
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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import test.com.wealdtech.hawk.config.ApplicationModule;
import test.com.wealdtech.hawk.jersey.guice.HawkConfigurationModule;
import test.com.wealdtech.hawk.jersey.guice.HawkServletModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;

import static org.testng.Assert.assertEquals;

public class HawkJerseyServerTest
{
  private HawkCredentials testgoodcredentials;
  private URI validuri1;
  private SampleJettyServer webserver;

  // Helper
  private HttpURLConnection connect(final URI uri, final String authorizationHeader) throws Exception
  {
    final HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Authorization", authorizationHeader);
    connection.setDoOutput(true);
    connection.connect();
    return connection;
  }

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testgoodcredentials = new HawkCredentials.Builder()
                                              .keyId("dh37fgj492je")
                                              .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                              .algorithm(HawkCredentials.Algorithm.HMAC_SHA_256)
                                              .build();
    this.validuri1 = new URI("http://localhost:8080/helloworld");

    // Create an injector with our basic configuration
    final Injector injector = Guice.createInjector(new ApplicationModule(),
                                                   new HawkConfigurationModule(),
                                                   new HawkServletModule("test.com.wealdtech.hawk.providers",
                                                                         "test.com.wealdtech.hawk.resources"));
    this.webserver = injector.getInstance(SampleJettyServer.class);
    webserver.start();
  }

  @AfterClass
  public void tearDown() throws Exception
  {
    this.webserver.stop();
  }

  @Test
  public void testSimpleAuth() throws Exception
  {
    final HawkClient testclient = new HawkClient(this.testgoodcredentials);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testFailedAuth() throws Exception
  {
    // Test failed auth due to bad MAC
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.testgoodcredentials)
                                                              .key("bad")
                                                              .build();
    final HawkClient testclient = new HawkClient(badCredentials);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidAuth1() throws Exception
  {
    // Test authentication with unknown key ID
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.testgoodcredentials)
                                                              .keyId("unknown")
                                                              .build();
    final HawkClient testclient = new HawkClient(badCredentials);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidAuth2() throws Exception
  {
    // Test authentication with differing algorithms
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.testgoodcredentials)
                                                              .algorithm(HawkCredentials.Algorithm.HMAC_SHA_1)
                                                              .build();
    final HawkClient testclient = new HawkClient(badCredentials);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    HttpURLConnection connection = connect(this.validuri1, authorizationHeader);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testValidBewit() throws Exception
  {
    // Test a valid bewit
    final String bewit = Hawk.generateBewit(this.testgoodcredentials, this.validuri1, 600L, null);
    URI testUri = new URI(this.validuri1.toString() + "?bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, null);
    assertEquals(connection.getResponseCode(), 200);
  }
}
