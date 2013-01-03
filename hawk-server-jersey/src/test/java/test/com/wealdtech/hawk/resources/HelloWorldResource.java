package test.com.wealdtech.hawk.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * (Very) simple resource for testing
 */
@Path("helloworld")
public class HelloWorldResource
{
  @GET
  @Produces("text/plain")
  public String getHelloWorld()
  {
    return "Hello world";
  }
}
