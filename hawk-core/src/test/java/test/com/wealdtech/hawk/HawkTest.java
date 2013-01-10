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
  private URI testuri1, testuri2, testuri3;

  @BeforeClass
  public void setUp() throws Exception
  {
    this.testhc1 = new HawkCredentials.Builder()
                                      .keyId("dh37fgj492je")
                                      .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                      .algorithm("hmac-sha-256")
                                      .build();
    this.testuri1 = new URI("http://www.example.com/test/path");
    this.testuri2 = new URI("https://www.example.com/test/path/two?one=1&two=two");
    this.testuri3 = new URI("http://localhost:18234/test");
  }

  @Test
  public void testMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.CORE, 12345L, this.testuri1, "testnonce", "GET", null);
    assertEquals(testmac1, "H5axze1ku3IP8l9AyM6dtmUClLQr4/uPfYshPDg8I9k=");
  }

  @Test
  public void testHttpsMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.CORE, 54321L, this.testuri2, "testnonce", "POST", null);
    assertEquals(testmac1, "Dh08e2Cyb/DuRvYUwPwuWvv8m750iD8elL7l/qjoOL8=");
  }

  @Test
  public void testExtDataMAC() throws DataError, ServerError
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.CORE, 12345L, this.testuri1, "testnonce", "GET", "Extra data");
    assertEquals(testmac1, "/D5e1plpo7zgo8gciszrmj9B/SWIQRoX6WIh2HiDZzM=");
  }

  @Test
  public void testBewit() throws DataError, ServerError
  {
    String testbewit1 = Hawk.generateBewit(this.testhc1, this.testuri3, 600L, null);
    assertEquals(testbewit1, "/D5e1plpo7zgo8gciszrmj9B/SWIQRoX6WIh2HiDZzM=");
  }
}
