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

package test.com.wealdtech.hawk.jersey.guice;

import test.com.wealdtech.hawk.model.ExampleUser;
import test.com.wealdtech.hawk.service.ExampleUserService;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.wealdtech.hawk.jersey.HawkAuthenticator;
import com.wealdtech.jersey.auth.Authenticator;
import com.wealdtech.jersey.auth.PrincipalProvider;

/**
 * A Guice module to configure Hawk authentication with the ExampleUser as the
 * underlying principal.
 */
public class HawkConfigurationModule extends ServletModule
{
  @Override
  public void configureServlets()
  {
    bind(new TypeLiteral<Authenticator<ExampleUser>>(){}).to(new TypeLiteral<HawkAuthenticator<ExampleUser>>(){}).in(Singleton.class);
    bind(new TypeLiteral<PrincipalProvider<ExampleUser, String>>(){}).to(ExampleUserService.class);
  }
}
