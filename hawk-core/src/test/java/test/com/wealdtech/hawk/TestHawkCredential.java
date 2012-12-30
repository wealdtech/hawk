package test.com.wealdtech.hawk;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.wealdtech.DataError;
import com.wealdtech.hawk.HawkCredential;

public class TestHawkCredential
{
  @Test
  public void testModel() throws DataError
  {
    final HawkCredential testhc1 = new HawkCredential.Builder().keyId("testkeyid").key("testkey").algorithm("hmac-sha-256").build();
    assertEquals(testhc1.getKeyId(), "testkeyid");
    assertEquals(testhc1.getKey(), "testkey");
    assertEquals(testhc1.getAlgorithm(), "hmac-sha-256");
  }

  @Test
  public void testNullKey() throws DataError
  {
    try
    {
      new HawkCredential.Builder().keyId(null).key("testkey").algorithm("hmac-sha-256").build();
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
      new HawkCredential.Builder().keyId("testkeyid").key(null).algorithm("hmac-sha-256").build();
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
      new HawkCredential.Builder().keyId("testkeyid").key("testkey").algorithm(null).build();
      // Should not reach here
      fail();
    }
    catch (DataError de)
    {
      // Good
    }
  }

  @Test
  public void testInvalidAlgorithm() throws DataError
  {
    try
    {
      new HawkCredential.Builder().keyId("testkeyid").key("testkey").algorithm("md2").build();
      // Should not reach here
      fail();
    }
    catch (DataError de)
    {
      // Good
    }
  }
}
