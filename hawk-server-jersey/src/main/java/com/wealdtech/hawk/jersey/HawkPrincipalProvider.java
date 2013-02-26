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

import com.google.common.base.Optional;
import com.wealdtech.jersey.auth.PrincipalProvider;

/**
 * Abstract class to provide a principal given a Hawk key ID.
 */
public abstract class HawkPrincipalProvider<T> implements PrincipalProvider<T, String>
{
  @Override
  public abstract Optional<T> getFromKey(String key);
}
