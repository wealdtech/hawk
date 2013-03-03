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

package test.com.wealdtech.hawk.service;

import java.util.List;
import java.util.Map;

import test.com.wealdtech.hawk.model.ExampleUser;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wealdtech.DataError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.jersey.HawkPrincipalProvider;

/**
 * Example service to provide a user definition given a key.
 * <p>In this case, the key is the Hawk key ID as passed in
 * by every the Hawk-authenticated request.
 */
public class ExampleUserService extends HawkPrincipalProvider<ExampleUser>
{
  private transient final Map<String, ExampleUser> usermap;

  public ExampleUserService() throws DataError
  {
    this.usermap = Maps.newHashMap();
    HawkCredentials user1HawkCredentials1 = new HawkCredentials.Builder()
                                                               .keyId("dh37fgj492je")
                                                               .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                                               .build();
    HawkCredentials user1HawkCredentials2 = new HawkCredentials.Builder()
                                                               .keyId("m971bc0s34bs")
                                                               .key("l01xyW713Mmnvt7kKasGOaWelnoawgp9aq239vgilr")
                                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                                               .build();
    List<HawkCredentials> user1HawkCredentials = Lists.newArrayList(user1HawkCredentials1, user1HawkCredentials2);
    ExampleUser user1 = new ExampleUser("Steve", user1HawkCredentials);
    this.usermap.put(user1HawkCredentials1.getKeyId(), user1);
    this.usermap.put(user1HawkCredentials2.getKeyId(), user1);
    HawkCredentials user2HawkCredentials1 = new HawkCredentials.Builder()
                                                               .keyId("jns7y9824hus")
                                                               .key("mb708923nzgr87t4fsnt48ufnjt4y98zjkby98t43tw")
                                                               .algorithm(HawkCredentials.Algorithm.SHA256)
                                                               .build();
    List<HawkCredentials> user2HawkCredentials = Lists.newArrayList(user2HawkCredentials1);
    ExampleUser user2 = new ExampleUser("John", user2HawkCredentials);
    this.usermap.put(user2HawkCredentials1.getKeyId(), user2);
  }

  /**
   * Simple lookup of a local map to find an ExampleUser given a
   * Hawk key ID.  In any real-world situation this method would
   * be expected to query some sort of persistent datastore.
   * @param hawkKeyId the ID of the Hawk key that identifies the user
   * @return An Optional, containing the ExampleUser if they were found
   */
  @Override
  public Optional<ExampleUser> getFromKey(final String hawkKeyId)
  {
    return Optional.fromNullable(this.usermap.get(hawkKeyId));
  }
}
