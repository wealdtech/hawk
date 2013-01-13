/*
 *    Copyright 2012 Weald Technology Trading Limited
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

package com.wealdtech.hawk;

import static com.wealdtech.Preconditions.*;

import java.net.URI;

import com.google.inject.Inject;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.utils.StringUtils;

public class HawkClient
{
  private final HawkClientConfiguration configuration;
  private final HawkCredentials credentials;

  @Inject
  private HawkClient(final HawkClientConfiguration configuration,
                     final HawkCredentials credentials)
  {
    if (configuration == null)
    {
      this.configuration = new HawkClientConfiguration();
    }
    else
    {
      this.configuration = configuration;
    }
    this.credentials = credentials;
    validate();
  }

  private void validate() throws DataError
  {
    checkNotNull(this.configuration, "The client configuration is required");
    checkNotNull(this.credentials, "The credentials are required");
  }

  /**
   * Generate the value for the Hawk authorization header.
   *
   * @param uri the URI for the request
   * @param method the request for the method
   * @param hash a hash of the request's payload, or <code>null</code> if payload authentication is not required
   * @param ext extra data, or <code>null</code> if none
   * @return The value for the Hawk authorization header.
   * @throws DataError If there is a problem with the data passed in which makes it impossible to generate a valid authorization header
   * @throws ServerError If there is a server problem whilst generating the authorization header
   */
  public String generateAuthorizationHeader(final URI uri,
                                            final String method,
                                            final String hash,
                                            final String ext) throws DataError, ServerError
  {
    long timestamp = System.currentTimeMillis() / 1000;
    final String nonce = StringUtils.generateRandomString(6);
    final String mac = Hawk.calculateMAC(this.credentials, Hawk.AuthType.HEADER, timestamp, uri, nonce, method, hash, ext);

    final StringBuilder sb = new StringBuilder(1024);
    sb.append("Hawk id=\"");
    sb.append(this.credentials.getKeyId());
    sb.append("\", ts=\"");
    sb.append(timestamp);
    sb.append("\", nonce=\"");
    sb.append(nonce);
    if (hash != null)
    {
      sb.append("\", hash=\"");
      sb.append(hash);
    }
    if ((ext != null) && (!"".equals(ext)))
    {
      sb.append("\", ext=\"");
      sb.append(ext);
    }
    sb.append("\", mac=\"");
    sb.append(mac);
    sb.append('"');

    return sb.toString();
  }

  public boolean isValidFor(final String path)
  {
    return ((this.configuration.getPathPrefix() == null) ||
            ((path == null) || (path.startsWith(this.configuration.getPathPrefix()))));
  }

  public static class Builder
  {
    private HawkClientConfiguration configuration;
    private HawkCredentials credentials;

    /**
     * Generate a new builder.
     */
    public Builder()
    {
    }

    /**
     * Generate build with all values set from a prior object.
     * @param prior the prior object
     */
    public Builder(final HawkClient prior)
    {
      this.configuration = prior.configuration;
      this.credentials = prior.credentials;
    }

    /**
     * Override the existing configuration.
     * @param configuration the new configuration
     * @return The builder
     */
    public Builder configuration(final HawkClientConfiguration configuration)
    {
      this.configuration = configuration;
      return this;
    }

    /**
     * Override the existing credentials.
     * @param credentials the new credentials
     * @return The builder
     */
    public Builder credentials(final HawkCredentials credentials)
    {
      this.credentials = credentials;
      return this;
    }

    /**
     * Build the client
     * @return a new client
     */
    public HawkClient build()
    {
      return new HawkClient(this.configuration, this.credentials);
    }
  }
}
