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

import java.net.URI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;
import com.wealdtech.jersey.auth.Authenticator;
import com.wealdtech.jersey.auth.PrincipalProvider;

import static com.wealdtech.Preconditions.checkNotNull;

/**
 * Authenticate a request using Hawk.
 */
public class HawkAuthenticator<T extends HawkCredentialsProvider> implements Authenticator<T>
{
  private final transient HawkServer server;
  private final transient PrincipalProvider<T, String> provider;

  /**
   * Create a new authenticator for Hawk.
   * @param server a the Hawk server
   * @param provider a provider for Hawk credentials
   */
  @Inject
  public HawkAuthenticator(final HawkServer server,
                           final PrincipalProvider<T, String> provider)
  {
    this.server = server;
    this.provider = provider;
  }

  /**
   * Authenticate a specific request.
   * <p>The request will
   */
  @Override
  public Optional<T> authenticate(final ContainerRequest request) throws DataError, ServerError
  {
    final ImmutableMap<String, String> authorizationHeaders = server.splitAuthorizationHeader(request.getHeaderValue(ContainerRequest.AUTHORIZATION));
    checkNotNull(authorizationHeaders.get("id"), "Missing required Hawk authorization header \"id\"");
    checkNotNull(authorizationHeaders.get("ts"), "Missing required Hawk authorization header \"ts\"");
    checkNotNull(authorizationHeaders.get("mac"), "Missing required Hawk authorization header \"mac\"");
    checkNotNull(authorizationHeaders.get("nonce"), "Missing required Hawk authorization header \"nonce\"");
    final URI uri = request.getRequestUri();
    final String method = request.getMethod();
    final T principal = provider.get(authorizationHeaders.get("id")).orNull();
    if (principal == null)
    {
      // Could not find the principal, reject this authentication request
      throw new DataError.Authentication("Failed to authenticate request");
    }
    final HawkCredentials credentials = principal.getHawkCredentials(authorizationHeaders.get("id"));
    authenticatePrincipal(credentials, uri, method, authorizationHeaders);
    return Optional.fromNullable(principal);
  }

  /**
   * Authenticate a user given all of the required information
   * @param credentials
   * @return
   */
  public final void authenticatePrincipal(final HawkCredentials credentials,
                                          final URI uri,
                                          final String method,
                                          final ImmutableMap<String, String>authorizationHeaders) throws DataError, ServerError
  {
    server.authenticate(credentials, uri, method, authorizationHeaders);
  }
}
