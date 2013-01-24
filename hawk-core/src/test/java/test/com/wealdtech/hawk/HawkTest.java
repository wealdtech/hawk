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
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.Hawk.AuthType;
import com.wealdtech.hawk.HawkCredentials;

import static org.testng.Assert.*;

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
                                      .algorithm(HawkCredentials.Algorithm.SHA256)
                                      .build();
    this.testuri1 = new URI("http://www.example.com/test/path");
    this.testuri2 = new URI("https://www.example.com/test/path/two?one=1&two=two");
    this.testuri3 = new URI("https://www.example.com/test?param=&lt;&gt;&pound;%54%65%73%74");
  }

  @Test
  public void testMAC() throws Exception
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.HEADER, 12345L, this.testuri1, "testnonce", "GET", null, null);
    assertEquals(testmac1, "yAyaat9VjarwY83O8iv/6Q40YkIrzIpxw1rM8NPB6dM=");
  }

  @Test
  public void testHttpsMAC() throws Exception
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.HEADER, 54321L, this.testuri2, "testnonce", "POST", null, null);
    assertEquals(testmac1, "ZEtWFvxZUZel0BqGw6mJSfdZzVse+5ltv8jXSKcGrcg=");
  }

  @Test
  public void testExtDataMAC() throws Exception
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.HEADER, 12345L, this.testuri1, "testnonce", "GET", null, "Extra data");
    assertEquals(testmac1, "1G+cw0tLT10b+IEno0PDwS2X4tMU0SG6KHIf2ivEMwI=");
  }

  @Test
  public void testRaw() throws Exception
  {
    String testmac1 = Hawk.calculateMAC(this.testhc1, Hawk.AuthType.HEADER, 12345L, this.testuri3, "testnonce", "GET", null, null);
    assertEquals(testmac1, "fFBSTdQ/dx0DzRPA2hTmhp0wdk1ZqG9hjQISlz1bqg0=");
  }

  @Test
  public void testBewitValidation1() throws Exception
  {
    try
    {
      Hawk.generateBewit(this.testhc1, this.testuri1, -1L, null);
      fail("Bewit generated with negative TTL");
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testBewitValidation2() throws Exception
  {
    Hawk.generateBewit(this.testhc1, this.testuri1, 240L, "extdata");
  }

  @Test
  public void testBadScheme() throws Exception
  {
    URI invalidSchemeUri= new URI("ftp://www.example.com/file");
    try
    {
      Hawk.calculateMAC(this.testhc1, Hawk.AuthType.HEADER, 12345L, invalidSchemeUri, "testnonce", "GET", null, null);
      fail("MAC calculated with invalid scheme");
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testValidAuthType() throws Exception
  {
    AuthType.parse("header");
  }

  @Test
  public void testInvalidAuthType() throws Exception
  {
    try
    {
      AuthType.parse("invalid");
      fail("AuthType accepted invalid value");
    }
    catch (DataError de)
    {
      // Good
    }
  }

}
