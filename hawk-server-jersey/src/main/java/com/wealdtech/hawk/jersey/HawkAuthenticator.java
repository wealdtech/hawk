package com.wealdtech.hawk.jersey;

import com.google.common.base.Optional;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.jersey.auth.Authenticator;

public class HawkAuthenticator<P> implements Authenticator<HawkCredentials, P>
{
  @Override
  public Optional<P> authenticate(final HawkCredentials credentials) throws DataError, ServerError
  {
    // TODO Auto-generated method stub
    return null;
  }
}
