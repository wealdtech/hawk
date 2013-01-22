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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.Hawk;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;
import com.wealdtech.jersey.auth.Authenticator;
import com.wealdtech.jersey.auth.PrincipalProvider;

import static com.wealdtech.Preconditions.*;

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
   * Authenticate a request.
   * <p>Authentication can be with an authentication header or a query string, so
   * decide which it is and handle it appropriately.
   * @param request the HTTP request
   * @return the authenticated principal, or <code>Optional.absent()</code> if the request was not authenticated
   * @throws DataError if there is a problem with the data that prevents the authentication attempt
   * @throws ServerError if there is an internal problem during the authentication attempt
   */
  @Override
  public Optional<T> authenticate(final ContainerRequest request) throws DataError, ServerError
  {
    if (request.getQueryParameters().containsKey("bewit"))
    {
      return authenticateFromBewit(request);
    }
    else
    {
      return authenticateFromHeader(request);
    }
  }

  /**
   * Authenticate a request from a bewit.
   * @param request the HTTP request
   * @return the authenticated principal, or <code>Optional.absent()</code> if the request was not authenticated
   * @throws DataError if there is a problem with the data that prevents the authentication attempt
   * @throws ServerError if there is an internal problem during the authentication attempt
   */
  private Optional<T> authenticateFromBewit(final ContainerRequest request) throws DataError, ServerError
  {
    checkState((request.getMethod().equals("GET")), "HTTP method %s not supported with bewit", request.getMethod());
    final String bewit = server.extractBewit(request.getRequestUri());
    final ImmutableMap<String, String> bewitFields = server.splitBewit(bewit);
    final Optional<T> principal = provider.get(bewitFields.get("id"));
    if (!principal.isPresent())
    {
      // Could not find the principal, reject this authentication request
      return Optional.absent();
    }
    final HawkCredentials credentials = principal.get().getHawkCredentials(bewitFields.get("id"));
    this.server.authenticate(credentials, request.getRequestUri());
    return principal;
  }

  /**
   * Authenticate a request from an authentication header.
   * @param request the HTTP request
   * @return the authenticated principal, or <code>Optional.absent()</code> if the request was not authenticated
   * @throws DataError if there is a problem with the data that prevents the authentication attempt
   * @throws ServerError if there is an internal problem during the authentication attempt
   */
  private Optional<T> authenticateFromHeader(final ContainerRequest request) throws DataError, ServerError
  {
    final ImmutableMap<String, String> authorizationHeaders = server.splitAuthorizationHeader(request.getHeaderValue(ContainerRequest.AUTHORIZATION));
    checkNotNull(authorizationHeaders.get("id"), "Missing required Hawk authorization header \"id\"");
    checkNotNull(authorizationHeaders.get("ts"), "Missing required Hawk authorization header \"ts\"");
    checkNotNull(authorizationHeaders.get("mac"), "Missing required Hawk authorization header \"mac\"");
    checkNotNull(authorizationHeaders.get("nonce"), "Missing required Hawk authorization header \"nonce\"");
    String hash = null;
    final URI uri = request.getRequestUri();
    final String method = request.getMethod();
    final Optional<T> principal = provider.get(authorizationHeaders.get("id"));
    if (!principal.isPresent())
    {
      // Could not find the principal, reject this authentication request
      return Optional.absent();
    }
    final HawkCredentials credentials = principal.get().getHawkCredentials(authorizationHeaders.get("id"));
    if (authorizationHeaders.get("hash") != null)
    {
//      throw new ServerError("Authentication using body hashes is not supported");
      try
      {
        hash = Hawk.calculateMac(credentials, CharStreams.toString(new InputStreamReader(request.getEntityInputStream(), "UTF-8")));
      }
      catch (IOException ioe)
      {
        throw new DataError.Bad("Failed to read the message body to calculate hash");
      }
    }
    boolean hasBody = request.getHeaderValue(ContainerRequest.CONTENT_LENGTH) != null ? true : false;
    this.server.authenticate(credentials, uri, method, authorizationHeaders, hash, hasBody);
    return principal;
  }
}
