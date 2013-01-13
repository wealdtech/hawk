/*
 *    Copyright 2013 Weald Technology Trading Limited
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package test.com.wealdtech.hawk.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import test.com.wealdtech.hawk.model.ExampleUser;

/**
 * Simple resource for testing Hawk authentication and
 * authenticated user injection.
 */
@Path("helloworld")
public class HelloWorldResource
{
  @Context
  ExampleUser authenticatedUser;

  @GET
  @Produces("text/plain")
  public String getHelloWorld()
  {
    if (this.authenticatedUser == null)
    {
      return "Hello world";
    }
    else
    {
      return "Hello " + this.authenticatedUser.getName();
    }
  }

  @POST
  @Produces("text/plain")
  public String getHelloWorldPost()
  {
    if (this.authenticatedUser == null)
    {
      return "Hello world";
    }
    else
    {
      return "Hello " + this.authenticatedUser.getName();
    }
  }
}
