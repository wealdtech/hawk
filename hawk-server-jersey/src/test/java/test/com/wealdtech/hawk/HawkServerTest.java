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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;

public class HawkServerTest
{
  private HawkCredentials testgoodcredentials;
  private URI validuri1;

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testgoodcredentials = new HawkCredentials.Builder()
                                              .keyId("dh37fgj492je")
                                              .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                              .algorithm("hmac-sha-256")
                                              .build();
    this.validuri1 = new URI("http://localhost:8080/helloworld");
  }

  @Test
  public void testSimpleAuth() throws Exception
  {
    final HawkClient testclient = new HawkClient(this.testgoodcredentials);
    final String authorizationheader = testclient.generateAuthorizationHeader(this.validuri1, "get", null);
    System.out.println(authorizationheader);
    HttpURLConnection connection = (HttpURLConnection)this.validuri1.toURL().openConnection();
    connection.setRequestProperty("Authorization", authorizationheader);
    connection.setDoOutput(true);
    InputStream is = connection.getInputStream();
  }
}
