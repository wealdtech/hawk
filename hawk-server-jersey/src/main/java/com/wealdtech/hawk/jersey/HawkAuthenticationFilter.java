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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.jersey.auth.Authenticator;
import com.wealdtech.jersey.exceptions.InternalServerException;
import com.wealdtech.jersey.exceptions.UnauthorizedException;

/**
 * Authentication filter using the Hawk protocol.
 * @param <T>
 */
public class HawkAuthenticationFilter<T> implements ContainerRequestFilter
{
  private final transient Authenticator<T> authenticator;

  @Context
  private transient HttpServletRequest servletrequest;

  @Inject
  public HawkAuthenticationFilter(final Authenticator<T> authenticator)
  {
    this.authenticator = authenticator;
  }

  @Override
  public ContainerRequest filter(final ContainerRequest request)
  {
    Optional<T> result;
    try
    {
      result = authenticator.authenticate(request);
    }
    catch (DataError de)
    {
      // A data error means that the authentication attempt failed due to bad data
      throw new UnauthorizedException(de);
    }
    catch (ServerError se)
    {
      // A server error means that the authentication attempt failed due to a server problem
      throw new InternalServerException(se);
    }

    // At this point authentication is complete and we can check the result
    if (!result.isPresent())
    {
      // No result returned; authentication did not result in a valid principal
      throw new UnauthorizedException("Unknown or invalid authentication", "Authentication failed");
    }

    // At this point the request has been authenticated successfully.
    // Store the object returned form the authenticator so that it can be
    // accessed by resources, providers etc.
    this.servletrequest.setAttribute("com.wealdtech.authenticatedprincipal", result.get());

    return request;
  }
}
