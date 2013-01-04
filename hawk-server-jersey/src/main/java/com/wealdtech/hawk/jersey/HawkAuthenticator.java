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
import com.wealdtech.hawk.HawkServerConfiguration;
import com.wealdtech.jersey.auth.Authenticator;
import com.wealdtech.jersey.auth.PrincipalProvider;

/**
 * Authenticate a request using Hawk.
 */
public class HawkAuthenticator<T extends HawkCredentialsProvider> implements Authenticator<T>
{
  private final transient HawkServer server;
  private final transient PrincipalProvider<T, String> provider;

  /**
   * Create a new authenticator for Hawk.
   * @param configuration configuration of the Hawk server
   * @param provider a provider for Hawk credentials
   */
  @Inject
  public HawkAuthenticator(final HawkServerConfiguration configuration,
                           final PrincipalProvider<T, String> provider)
  {
    this.server = new HawkServer(configuration);
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
    final URI uri = request.getRequestUri();
    final String method = request.getMethod();
    final T principal = provider.get(authorizationHeaders.get("id")).orNull();
    if (principal == null)
    {
      // Could not find the principal, reject this authentication request
      throw new DataError("Failed to authenticate request");
    }
    final HawkCredentials credentials = principal.getHawkCredentials();
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
