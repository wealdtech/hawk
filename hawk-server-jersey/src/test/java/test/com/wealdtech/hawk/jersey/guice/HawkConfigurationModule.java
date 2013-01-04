package test.com.wealdtech.hawk.jersey.guice;

import test.com.wealdtech.hawk.model.ExampleUser;
import test.com.wealdtech.hawk.service.ExampleUserService;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.wealdtech.hawk.jersey.HawkAuthenticator;
import com.wealdtech.jersey.auth.Authenticator;
import com.wealdtech.jersey.auth.PrincipalProvider;

public class HawkConfigurationModule extends ServletModule
{
  @Override
  public void configureServlets()
  {
    bind(new TypeLiteral<Authenticator<ExampleUser>>(){}).to(new TypeLiteral<HawkAuthenticator<ExampleUser>>(){}).in(Singleton.class);
    bind(new TypeLiteral<PrincipalProvider<ExampleUser, String>>(){}).to(ExampleUserService.class);
  }
}
