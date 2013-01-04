package test.com.wealdtech.hawk.jersey;

import test.com.wealdtech.hawk.model.ExampleUser;

import com.google.inject.Inject;
import com.wealdtech.hawk.jersey.HawkAuthenticationFilter;
import com.wealdtech.jersey.auth.Authenticator;

public class HawkTestUserAuthenticationFilter extends HawkAuthenticationFilter<ExampleUser>
{
  @Inject
  public HawkTestUserAuthenticationFilter(final Authenticator<ExampleUser> authenticator)
  {
    super(authenticator);
  }
}
