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

package test.com.wealdtech.hawk.providers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import test.com.wealdtech.hawk.model.ExampleUser;

import com.sun.jersey.api.core.HttpContext;
import com.wealdtech.jersey.providers.AbstractInjectableProvider;

/**
 * A sample provider using the information obtained during the authentication
 * process.
 * <p>To access the user add the following as either a method argument or class field:
 * <p><code>@Context ExampleUser authenticatedUser</code>
 */
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
    return (ExampleUser)this.servletRequest.getAttribute("com.wealdtech.authenticatedprincipal");
  }
}
