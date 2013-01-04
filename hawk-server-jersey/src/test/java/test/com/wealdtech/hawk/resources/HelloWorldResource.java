package test.com.wealdtech.hawk.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import test.com.wealdtech.hawk.model.ExampleUser;

/**
 * (Very) simple resource for testing
 */
@Path("helloworld")
public class HelloWorldResource
{
  @Context
  ExampleUser user;

  @GET
  @Produces("text/plain")
  public String getHelloWorld()
  {
    if (this.user == null)
    {
      return "Hello world";
    }
    else
    {
      return "Hello " + this.user.getName();
    }
  }
}
