package com.wealdtech.hawk.jersey;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;
import com.wealdtech.jersey.exceptions.InternalServerException;
import com.wealdtech.jersey.exceptions.UnauthorizedException;

/**
 * Authentication filter using the Hawk protocol.
 * @param <T>
 */
public class HawkAuthenticationFilter<T> implements ContainerRequestFilter
{
  private transient final HawkCredentialsProvider provider;

  private transient final HawkAuthenticator<T> authenticator;

  @Context
  private transient HttpServletRequest servletrequest;

  @Inject
  public HawkAuthenticationFilter(final HawkAuthenticator<T> authenticator,
                                  final HawkCredentialsProvider provider)
  {
    this.authenticator = authenticator;
    this.provider = provider;
  }

  @Override
  public ContainerRequest filter(final ContainerRequest request)
  {
    Optional<T> result;
    try
    {
      // Obtain parameters available from the request
      final ImmutableMap<String, String> authorizationheaders = HawkServer.splitAuthorizationHeader(request.getHeaderValue(ContainerRequest.AUTHORIZATION));
      // We need to obtain our own stored copy of the requestor's credentials
      // given the keyId parameter passed in as part of the authorization header
      final HawkCredentials credentials = this.provider.getCredentials(authorizationheaders.get("id"));
      // Now that we have the credentials we can authenticate the request
      result = authenticator.authenticate(request, credentials);
    }
    catch (DataError de)
    {
      // A data error means that the request was not authenticated successfully using the supplied information
      throw new UnauthorizedException(de);
    }
    catch (ServerError se)
    {
      // A server error means that there was a server-side problem whilst attempting to authenticate the request
      throw new InternalServerException(se);
    }

    // At this point authentication is complete and we can check the result
    if (!result.isPresent())
    {
      // No result returned; authentication did not result in a valid principal
      throw new UnauthorizedException("Invalid user");
    }

    // At this point the request has been authenticated successfully.
    // Stash the object returned form the authenticator so that it can be
    // accessed by resources, providerd etc.
    this.servletrequest.setAttribute("com.wealdtech.principal", result.get());

    return request;
  }
}
