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

package test.com.wealdtech.hawk.jersey;

import test.com.wealdtech.hawk.model.ExampleUser;

import com.google.inject.Inject;
import com.wealdtech.hawk.jersey.HawkAuthenticationFilter;
import com.wealdtech.jersey.auth.Authenticator;

/**
 * A filter to intercept all incoming requests and authenticate them using Hawk.
 */
public class HawkExampleUserAuthenticationFilter extends HawkAuthenticationFilter<ExampleUser>
{

  /**
   * Set up the filter with a suitable authenticator
   * @param authenticator An authenticator
   */
  @Inject
  public HawkExampleUserAuthenticationFilter(final Authenticator<ExampleUser> authenticator)
  {
    super(authenticator);
  }
}
