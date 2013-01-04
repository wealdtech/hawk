package test.com.wealdtech.hawk.providers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import test.com.wealdtech.hawk.model.ExampleUser;

import com.sun.jersey.api.core.HttpContext;
import com.wealdtech.jersey.providers.AbstractInjectableProvider;

@Provider
public class ExampleUserProvider extends AbstractInjectableProvider<ExampleUser>
{
  @Context
  private transient HttpServletRequest servletRequest;

  public ExampleUserProvider()
  {
    super(ExampleUser.class);
  }

  @Override
  public ExampleUser getValue(final HttpContext c)
  {
    return (ExampleUser)this.servletRequest.getAttribute("com.wealdtech.principal");
  }
}
