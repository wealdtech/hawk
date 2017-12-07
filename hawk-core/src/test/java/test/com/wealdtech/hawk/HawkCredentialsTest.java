/*
 *    Copyright 2012, 2013 Weald Technology Trading Limited
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

import org.testng.annotations.Test;

import com.wealdtech.DataError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkCredentials.Algorithm;

public class HawkCredentialsTest
{
  @Test
  public void testModel() throws DataError
  {
    final HawkCredentials testhc1 = new HawkCredentials.Builder()
                                                       .keyId("testkeyid")
                                                       .key("testkey")
                                                       .algorithm(HawkCredentials.Algorithm.SHA256)
                                                       .build();
    assertEquals(testhc1.getKeyId(), "testkeyid");
    assertEquals(testhc1.getKey(), "testkey".getBytes());
    assertEquals(testhc1.getAlgorithm().toString(), "sha256");
    testhc1.toString();
    testhc1.hashCode();
    assertEquals(testhc1, testhc1);
    assertNotEquals(null, testhc1);

    final HawkCredentials testhc2 = new HawkCredentials.Builder(testhc1).keyId("testkeyid2").build();
    assertEquals(testhc2.getKeyId(), "testkeyid2");
    assertEquals(testhc1.getKey(), "testkey".getBytes());
    assertEquals(testhc1.getAlgorithm().toString(), "sha256");
    testhc2.toString();
    testhc2.hashCode();
    assertEquals(testhc2, testhc2);
    assertNotEquals(null, testhc2);
    assertNotEquals(testhc1, testhc2);
    assertEquals(testhc1.getJavaAlgorithm(), "HmacSHA256");
  }

  @Test
  public void testNullKey() throws DataError
  {
    try
    {
      new HawkCredentials.Builder().keyId(null).key("testkey").algorithm(HawkCredentials.Algorithm.SHA256).build();
      // Should not reach here
      fail();
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testNullKeyId() throws DataError
  {
    try
    {
      new HawkCredentials.Builder().keyId("testkeyid").key((String) null).algorithm(HawkCredentials.Algorithm.SHA256).build();
      // Should not reach here
      fail();
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testNullAlgorithm() throws DataError
  {
    try
    {
      new HawkCredentials.Builder().keyId("testkeyid").key("testkey").algorithm(null).build();
      // Should not reach here
      fail();
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testValidAlgorithm() throws DataError
  {
    Algorithm.parse("sha256");
  }

  @Test
  public void testInvalidAlgorithm() throws DataError
  {
    try
    {
      Algorithm.parse("invalid");
      fail("Algoritm accepted invalid value");
    }
    catch (DataError de)
    {
      // Good
    }
  }
}
