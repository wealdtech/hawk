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

package com.wealdtech.hawk.jersey;

import com.wealdtech.hawk.HawkCredentials;

public interface HawkCredentialsProvider
{
  /**
   * Obtain the Hawk credentials from a principal
   * @param keyId the Hawk key ID
   * @return the Hawk credentials, or <code>null</code> if they could not be found
   */
  HawkCredentials getHawkCredentials(final String keyId);
}
