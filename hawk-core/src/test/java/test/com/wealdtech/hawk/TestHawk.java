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

import java.net.URL;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.HawkCredentials;

public class TestHawk
{
  private HawkCredentials testhc1;
  private URL testurl1;
  private URL testurl2;

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testhc1 = new HawkCredentials.Builder().keyId("testkeyid").key("testkey").algorithm("hmac-sha-256").build();
    this.testurl1 = new URL("http://www.example.com/test/path");
    this.testurl2 = new URL("https://www.example.com/test/path/two?one=1&two=two");
  }

  @Test
  public void testMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, 12345L, this.testurl1, "testnonce", "GET", null);
    assertEquals(testmac1, "TQcJRRT6IlUStBw9VmyALdHM/2HmO5cqc20jPXsCMoM=");
  }

  @Test
  public void testHttpsMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, 54321L, this.testurl2, "testnonce", "POST", null);
    assertEquals(testmac1, "b2rbmwxNmtK3Q+s1O4Ac7EJQgOjNJcmeE3LEUegq8x4=");
  }

  @Test
  public void testExtDataMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, 12345L, this.testurl1, "testnonce", "GET", "Extra data");
    assertEquals(testmac1, "CGxOiUfSKmdVfk030DE454MQ6jW/dKlSLi5iIz9IC/I=");
  }

}
