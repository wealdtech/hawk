package com.wealdtech.hawk.jersey;

import java.net.URI;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;
import com.wealdtech.hawk.HawkServerConfiguration;

/**
 * Authentication filter using the Hawk protocol.
 * This filter authenticates a request,
 * @param <T>
 */
public class HawkAuthenticationFilter<T> implements ContainerRequestFilter
{
  private transient final HawkServer server;

//  private transient final HawkCredentialsProvider provider;

//  private transient final Authenticator<HawkCredentials, T> authenticator;

  public HawkAuthenticationFilter(final HawkServerConfiguration configuration)
  {
    this.server = new HawkServer(configuration);
  }

  @Override
  public ContainerRequest filter(final ContainerRequest request)
  {
    T result = null;
    try
    {
      result = authenticate(request);
    }
    catch (DataError de)
    {
      // A data error means that the request was not authenticated successfully using the supplied information
    }
    catch (ServerError se)
    {
      // A server error means that there was a server-side problem whilst attempting to authenticate the request
    }

    // At this point the request has been authenticated successfully.

    return request;
  }

  private T authenticate(final ContainerRequest request) throws DataError, ServerError
  {
    // Obtain parameters available from the request
    final URI uri = request.getRequestUri();
    final String method = request.getMethod();
    final ImmutableMap<String, String> authorizationheaders = HawkServer.splitAuthorizationHeader(request.getHeaderValue(ContainerRequest.AUTHORIZATION));

    // We need to obtain our own stored copy of the requestor's credentials
    // given the keyId parameter passed in as part of the authorization header
    final HawkCredentials credentials = fetchCredentials(authorizationheaders.get("keyId"));

    server.authenticate(credentials, uri, method, authorizationheaders);
    return null;
  }

  private HawkCredentials fetchCredentials(final String keyId) throws DataError
  {
    return new HawkCredentials.Builder()
                              .keyId("dh37fgj492je")
                              .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                              .algorithm("hmac-sha-256")
                              .build();
  }
}
