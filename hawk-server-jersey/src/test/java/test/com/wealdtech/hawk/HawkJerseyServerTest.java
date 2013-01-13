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

import static org.testng.Assert.*;

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
import com.wealdtech.hawk.Hawk.PayloadValidation;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkClientConfiguration;
import com.wealdtech.hawk.HawkCredentials;

public class HawkJerseyServerTest
{
  private HawkCredentials goodCredentials;
  private URI validuri1, validuri2;
  private SampleJettyServer webserver;

  // Helper
  private HttpURLConnection connect(final URI uri, final String method, final String authorizationHeader, String body) throws Exception
  {
    final HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
    connection.setRequestMethod(method);
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
    this.goodCredentials = new HawkCredentials.Builder()
                                              .keyId("dh37fgj492je")
                                              .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                              .algorithm(HawkCredentials.Algorithm.SHA256)
                                              .build();
    this.validuri1 = new URI("http://localhost:8080/helloworld");
    this.validuri2 = new URI("http://localhost:8080/helloworld?one=one&two=two");

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
  public void testValidAuth() throws Exception
  {
    // Test valid authentication with a GET request
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    HttpURLConnection connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidAuth2() throws Exception
  {
    // Test valid authentication with a POST request
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", null, null);
    HttpURLConnection connection = connect(this.validuri1, "POST", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidAuth3() throws Exception
  {
    // Test valid authentication with a POST request and body
    final HawkClientConfiguration configuration = new HawkClientConfiguration.Builder().payloadValidation(PayloadValidation.MANDATORY).build();
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).configuration(configuration).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", null, null);
    HttpURLConnection connection = connect(this.validuri1, "POST", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidAuth4() throws Exception
  {
    // Test valid authentication with body hash
    final HawkClientConfiguration configuration = new HawkClientConfiguration.Builder().payloadValidation(PayloadValidation.MANDATORY).build();
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).configuration(configuration).build();
    final String body = "Test body";
    final String hash = Hawk.calculateMac(this.goodCredentials, body);
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "post", hash, null);
    HttpURLConnection connection = connect(this.validuri1, "POST", authorizationHeader, body);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testReplayProtection() throws Exception
  {
    // Test catching a replay of an older request
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    HttpURLConnection connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 200);
    connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testSkewProtection() throws Exception
  {
    // Test catching an anachronistic request
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    Thread.sleep(61000L);
    HttpURLConnection connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testFailedAuth() throws Exception
  {
    // Test failed auth due to bad MAC
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.goodCredentials)
                                                              .key("bad")
                                                              .build();
    final HawkClient testclient = new HawkClient.Builder().credentials(badCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    HttpURLConnection connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidAuth1() throws Exception
  {
    // Test authentication with unknown key ID
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.goodCredentials)
                                                              .keyId("unknown")
                                                              .build();
    final HawkClient testclient = new HawkClient.Builder().credentials(badCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    HttpURLConnection connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidAuth2() throws Exception
  {
    // Test authentication with differing algorithms
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.goodCredentials)
                                                              .algorithm(HawkCredentials.Algorithm.SHA1)
                                                              .build();
    final HawkClient testclient = new HawkClient.Builder().credentials(badCredentials).build();
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "get", null, null);
    HttpURLConnection connection = connect(this.validuri1, "GET", authorizationHeader, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidAuth3() throws Exception
  {
    // Test authentication with incorrect body hash
    final HawkClientConfiguration configuration = new HawkClientConfiguration.Builder().payloadValidation(PayloadValidation.MANDATORY).build();
    final HawkClient testclient = new HawkClient.Builder().credentials(this.goodCredentials).configuration(configuration).build();
    final String body = "Test body";
    final String hash = Hawk.calculateMac(this.goodCredentials, "bad body");
    final String authorizationHeader = testclient.generateAuthorizationHeader(this.validuri1, "put", hash, null);
    HttpURLConnection connection = connect(this.validuri1, "PUT", authorizationHeader, body);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testValidBewit1() throws Exception
  {
    // Test a valid bewit with a simple request
    final String bewit = Hawk.generateBewit(this.goodCredentials, this.validuri1, 600L, null);
    URI testUri = new URI(this.validuri1.toString() + "?bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, "GET", null, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidBewit2() throws Exception
  {
    // Test a valid bewit appending to an existing query string
    final String bewit = Hawk.generateBewit(this.goodCredentials, this.validuri2, 600L, null);
    URI testUri = new URI(this.validuri2.toString() + "&bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, "GET", null, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidBewit3() throws Exception
  {
    // Test a valid bewit in the middle of a query string
    final URI validuri = new URI(this.validuri2.toString() + "&three=three");
    final String bewit = Hawk.generateBewit(this.goodCredentials, validuri, 600L, null);
    URI testUri = new URI(this.validuri2.toString() + "&bewit=" + bewit + "&three=three");
    final HttpURLConnection connection = connect(testUri, "GET", null, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testValidBewit4() throws Exception
  {
    // Test a valid bewit with extra data
    final String bewit = Hawk.generateBewit(this.goodCredentials, this.validuri1, 600L, "extra");
    URI testUri = new URI(this.validuri1.toString() + "?bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, "GET", null, null);
    assertEquals(connection.getResponseCode(), 200);
  }

  @Test
  public void testInvalidBewit1() throws Exception
  {
    // Test an invalid bewit due to using the PUT method
    final String bewit = Hawk.generateBewit(this.goodCredentials, this.validuri1, 600L, null);
    URI testUri = new URI(this.validuri1.toString() + "?bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, "PUT", null, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidBewit2() throws Exception
  {
    // Test an invalid bewit due to bad bewit
    final HawkCredentials badCredentials = new HawkCredentials.Builder(this.goodCredentials)
                                                              .key("bad")
                                                              .build();
    final String bewit = Hawk.generateBewit(badCredentials, this.validuri1, 600L, null);
    URI testUri = new URI(this.validuri1.toString() + "?bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, "GET", null, null);
    assertEquals(connection.getResponseCode(), 401);
  }

  @Test
  public void testInvalidBewit3() throws Exception
  {
    // Test an invalid bewit due to expiry
    final String bewit = Hawk.generateBewit(this.goodCredentials, this.validuri1, 1L, null);
    Thread.sleep(2000L);
    URI testUri = new URI(this.validuri1.toString() + "?bewit=" + bewit);
    final HttpURLConnection connection = connect(testUri, "GET", null, null);
    assertEquals(connection.getResponseCode(), 401);
  }
}
