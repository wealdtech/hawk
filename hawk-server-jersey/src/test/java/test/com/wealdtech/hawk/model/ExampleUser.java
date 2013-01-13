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

package test.com.wealdtech.hawk.model;

import java.util.List;

import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.jersey.HawkCredentialsProvider;

/**
 * A simple example user class for testing Hawk.
 * <p>This class implements <code>HawkCredentialsProvider</code>,
 * which allows us to use it in authenticators.
 */
public class ExampleUser implements HawkCredentialsProvider
{
  private final String name;
  private final List<HawkCredentials> hawkCredentials;

  public ExampleUser(final String name, final List<HawkCredentials> hawkCredentials)
  {
    this.name = name;
    this.hawkCredentials = hawkCredentials;
  }

  public String getName()
  {
    return this.name;
  }

  @Override
  public HawkCredentials getHawkCredentials(final String keyId)
  {
    for (HawkCredentials credentials : this.hawkCredentials)
    {
      if (credentials.getKeyId().equals(keyId))
      {
        return credentials;
      }
    }
    return null;
  }
}
