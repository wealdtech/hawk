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
import org.testng.annotations.Test;

import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.HawkCredentials;

import static org.testng.Assert.assertEquals;

public class HawkTest
{
  private HawkCredentials testhc1;
  private URI testuri1;
  private URI testuri2;

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testhc1 = new HawkCredentials.Builder()
                                      .keyId("testkeyid")
                                      .key("testkey")
                                      .algorithm("hmac-sha-256")
                                      .build();
    this.testuri1 = new URI("http://www.example.com/test/path");
    this.testuri2 = new URI("https://www.example.com/test/path/two?one=1&two=two");
  }

  @Test
  public void testMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, 12345L, this.testuri1, "testnonce", "GET", null);
    assertEquals(testmac1, "rRqrRxa6nbacbSGRPOFSygr6AXCtfa119LRAFedXY0M=");
  }

  @Test
  public void testHttpsMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, 54321L, this.testuri2, "testnonce", "POST", null);
    assertEquals(testmac1, "XHHRCaKhtlmjWxn6xsizLxgF2zd/tSqCSEu7l02GOq4=");
  }

  @Test
  public void testExtDataMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, 12345L, this.testuri1, "testnonce", "GET", "Extra data");
    assertEquals(testmac1, "HSvuj0v2YMAaLItL6DNWxrNLB/ab1vdmR3pfmZKgY/k=");
  }

}
