/*
 *   Copyright 2012 - 2014 Weald Technology Trading Limited
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

package test.com.wealdtech.hawk.jersey;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.jersey.HawkAuthorizationFilter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;

import static org.testng.Assert.fail;

public class TestHawkClient
{
  private transient Client client;
  private transient HawkClient hawkClient;

  private static transient final String URL = "http://localhost:8080/helloworld";

  @BeforeClass
  public void setUp() throws Exception
  {
    final HawkCredentials hawkCredentials = new HawkCredentials.Builder()
                                                               .keyId("dh37fgj492je")
                                                               .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                                               .build();
    this.hawkClient = new HawkClient.Builder().credentials(hawkCredentials).build();

    final ClientConfig clientconfig = new DefaultClientConfig();
    this.client = Client.create(clientconfig);
    this.client.addFilter(new HawkAuthorizationFilter(this.hawkClient));
  }

  @Test
  public void testBasic()
  {
    final WebResource resource = this.client.resource(URL);
    try
    {
      final String result = resource.accept(MediaType.TEXT_PLAIN).get(String.class);
      System.out.println("Result is " + result);
    }
    catch (UniformInterfaceException uie)
    {
      fail(uie.getResponse().getStatusInfo().getReasonPhrase());
    }
  }
}
