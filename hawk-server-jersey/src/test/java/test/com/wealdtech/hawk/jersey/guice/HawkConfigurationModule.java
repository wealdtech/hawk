package test.com.wealdtech.hawk.jersey.guice;

import test.com.wealdtech.hawk.TestUser;

import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.wealdtech.hawk.jersey.HawkAuthenticator;
import com.wealdtech.hawk.jersey.HawkCredentialsProvider;

public class HawkConfigurationModule extends ServletModule
{
  @Override
  public void configureServlets()
  {
    bind(new TypeLiteral<HawkAuthenticator<TestUser>>(){}).toInstance(new HawkAuthenticator<TestUser>());
    bind(HawkCredentialsProvider.class).toInstance(new HawkCredentialsProvider());
  }
}
