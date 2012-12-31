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
