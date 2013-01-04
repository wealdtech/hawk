package com.wealdtech.hawk.jersey;

import com.google.common.base.Optional;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.jersey.auth.PrincipalProvider;

public class HawkPrincipalProvider<T> implements PrincipalProvider<T, String>
{
  @Override
  public Optional<T> get(String key) throws DataError, ServerError
  {
    // TODO Auto-generated method stub
    return null;
  }
}
