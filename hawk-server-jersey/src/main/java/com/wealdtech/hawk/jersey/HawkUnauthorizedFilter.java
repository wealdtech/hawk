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

import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.wealdtech.hawk.HawkServer;

/**
 * Response filter providing a WWW-Authenticate header
 * for unauthorized requests with the server's current
 * time as well as an NTP server for time synchronization,
 * as per the Hawk specification.
 */
public class HawkUnauthorizedFilter implements ContainerResponseFilter
{
  private final transient HawkServer server;

  @Inject
  public HawkUnauthorizedFilter(final HawkServer server)
  {
    this.server = server;
  }

  @Override
  public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
  {
    if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode())
    {
      response.getHttpHeaders().add("WWW-Authenticate", this.server.generateAuthenticateHeader());
    }
    return response;
  }
}
