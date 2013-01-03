package test.com.wealdtech.hawk;

import com.wealdtech.hawk.HawkCredentials;

/**
 * A simple user class for testing Hawk
 */
public class TestUser
{
  private final String name;
  private final HawkCredentials credentials;

  public TestUser(final String name, final HawkCredentials credentials)
  {
    this.name = name;
    this.credentials = credentials;
  }

  public String getName()
  {
    return this.name;
  }

  public HawkCredentials getCredentials()
  {
    return this.credentials;
  }
}
