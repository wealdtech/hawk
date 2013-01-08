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

import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.wealdtech.hawk.HawkClient;

import static com.wealdtech.Preconditions.checkNotNull;

/**
 * Request filter providing an Authorization header
 * for requests to Hawk applications.
 */
public class HawkAuthorizationFilter extends ClientFilter
{
  private transient final HawkClient client;

  @Inject
  public HawkAuthorizationFilter(final HawkClient client)
  {
    checkNotNull(client, "Hawk athorization filter requires a hawk client");
    this.client = client;
  }

  @Override
  public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException
  {
    if ((!cr.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) &&
        (client.isValidFor(cr.getURI().getRawPath())))
    {
      final URI uri = cr.getURI();
      final String method = cr.getMethod();
      cr.getHeaders().add(HttpHeaders.AUTHORIZATION, this.client.generateAuthorizationHeader(uri, method, null));
    }
    return getNext().handle(cr);}
  }
