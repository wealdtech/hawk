package test.com.wealdtech.hawk.providers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import test.com.wealdtech.hawk.TestUser;

import com.sun.jersey.api.core.HttpContext;
import com.wealdtech.jersey.providers.AbstractInjectableProvider;

//@Provider
//public class TestUserProvider extends AuthenticatedPrincipalProvider<TestUser>
//{
//  public TestUserProvider()
//  {
//    super(TestUser.class);
//  }
//}
@Provider
public class TestUserProvider extends AbstractInjectableProvider<TestUser>
{
  @Context
  private transient HttpServletRequest servletRequest;

  public TestUserProvider()
  {
    super(TestUser.class);
  }

  @Override
  public TestUser getValue(final HttpContext c)
  {
    return (TestUser)this.servletRequest.getAttribute("com.wealdtech.principal");
  }
}
