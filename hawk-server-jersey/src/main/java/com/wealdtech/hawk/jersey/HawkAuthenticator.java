package com.wealdtech.hawk.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.spi.container.ContainerRequest;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.jersey.auth.Authenticator;

public class HawkAuthenticator<P> implements Authenticator<HawkCredentials, P>
{
  @Override
  public Optional<P> authenticate(final ContainerRequest request, final HawkCredentials credentials) throws ServerError
  {
    return Optional.absent();
  }
}
