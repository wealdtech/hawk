package test.com.wealdtech.hawk.model;

import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.jersey.HawkCredentialsProvider;

/**
 * A simple example user class for testing Hawk.
 * <p>This class implements <code>HawkCredentialsProvider</code>,
 * which allows us to use it in authenticators.
 */
public class ExampleUser implements HawkCredentialsProvider
{
  private final String name;
  private final HawkCredentials hawkCredentials;

  public ExampleUser(final String name, final HawkCredentials hawkCredentials)
  {
    this.name = name;
    this.hawkCredentials = hawkCredentials;
  }

  public String getName()
  {
    return this.name;
  }

  @Override
  public HawkCredentials getHawkCredentials()
  {
    return this.hawkCredentials;
  }
}
