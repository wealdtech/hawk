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

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.inject.Inject;
import com.wealdtech.DataError;
import com.wealdtech.utils.StringUtils;

public final class HawkClient implements Comparable<HawkClient>
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

  private void validate()
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
   * @param app application ID, or <code>null</code> if none
   * @param dlg delegator, or <code>null</code> if none
   * @return The value for the Hawk authorization header.
   * @throws DataError If there is a problem with the data passed in which makes it impossible to generate a valid authorization header
   */
  public String generateAuthorizationHeader(final URI uri,
                                            final String method,
                                            final String hash,
                                            final String ext,
                                            final String app,
                                            final String dlg)
  {
    long timestamp = System.currentTimeMillis() / Hawk.MILLISECONDS_IN_SECONDS;
    final String nonce = StringUtils.generateRandomString(6);
    final String mac = Hawk.calculateMAC(this.credentials, Hawk.AuthType.HEADER, timestamp, uri, nonce, method, hash, ext, app, dlg);

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

  // Standard object methods follow
  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
                  .add("configuration", this.configuration)
                  .add("credentials", this.credentials)
                  .toString();
  }

  @Override
  public boolean equals(final Object that)
  {
    return (that instanceof HawkClient) && (this.compareTo((HawkClient)that) == 0);
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(this.configuration, this.credentials);
  }

  @Override
  public int compareTo(final HawkClient that)
  {
    return ComparisonChain.start()
                          .compare(this.configuration, that.configuration)
                          .compare(this.credentials, that.credentials)
                          .result();
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
