package test.com.wealdtech.hawk.jersey;

import test.com.wealdtech.hawk.TestUser;

import com.google.common.base.Optional;
import com.sun.jersey.spi.container.ContainerRequest;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.jersey.HawkAuthenticator;

public class SampleHawkAuthenticator extends HawkAuthenticator<TestUser>
{
  @Override
  public Optional<TestUser> authenticate(final ContainerRequest request, final HawkCredentials credentials) throws ServerError
  {
    // TODO probably shouldn't be passing back the user here, it's overloading the function
    return Optional.absent();
  }

}
