package test.com.wealdtech.hawk.jersey;

import test.com.wealdtech.hawk.TestUser;

import com.google.inject.Inject;
import com.wealdtech.hawk.jersey.HawkAuthenticationFilter;
import com.wealdtech.hawk.jersey.HawkAuthenticator;
import com.wealdtech.hawk.jersey.HawkCredentialsProvider;

public class HawkTestUserAuthenticationFilter extends HawkAuthenticationFilter<TestUser>
{
  @Inject
  public HawkTestUserAuthenticationFilter(HawkAuthenticator<TestUser> authenticator, HawkCredentialsProvider provider)
  {
    super(authenticator, provider);
  }
}
