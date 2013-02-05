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

package com.wealdtech.hawk;

import static com.wealdtech.Preconditions.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.wealdtech.hawk.Hawk.PayloadValidation;

/**
 * Configuration for a Hawk client. The Hawk client has a number of
 * configuration parameters. These are as follows:
 * <ul>
 * <li>pathPrefix: the path prefix for which the client should add authentication.  Defaults to <code>null</code> for everything</li>
 * <li>payloadValidation: if payload validation should take place.  Defaults to <code>NEVER</code></li>
 * </ul>
 * This is configured as a standard Jackson object and can be realized as part
 * of a ConfigurationSource.
 */
public class HawkClientConfiguration implements Comparable<HawkClientConfiguration>
{
  private String pathPrefix = null;
  private PayloadValidation payloadValidation = PayloadValidation.NEVER;

  /**
   * Create a client configuration with default values
   */
  public HawkClientConfiguration()
  {
  }

  /**
   * Create a configuration with specified values for all options.
   * Note that this should not be called directly, and the Builder should be
   * used for instantiation.
   *
   * @param pathPrefix
   *          which requests to authenticate, or <code>null</code> for the default
   * @param payloadValidation
   *          how to validate against payloads, or <code>null</code> for the default
   */
  @JsonCreator
  private HawkClientConfiguration(@JsonProperty("pathprefix") final String pathPrefix,
                                  @JsonProperty("payloadvalidation") final PayloadValidation payloadValidation)
  {
    if (pathPrefix != null)
    {
      this.pathPrefix = pathPrefix;
    }
    if (payloadValidation != null)
    {
      this.payloadValidation = payloadValidation;
    }
    validate();
  }

  private void validate()
  {
    checkNotNull(this.payloadValidation, "Payload validation setting is required");
    checkArgument(this.pathPrefix == null || this.pathPrefix.startsWith("/"), "Path prefix must start with \"/\" if present");
  }

  public String getPathPrefix()
  {
    return this.pathPrefix;
  }

  public PayloadValidation getPayloadValidation()
  {
    return this.payloadValidation;
  }

  // Standard object methods follow
  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
                  .add("pathPrefix", this.getPathPrefix())
                  .add("payloadValidation", this.getPayloadValidation())
                  .toString();
  }

  @Override
  public boolean equals(final Object that)
  {
    return (that instanceof HawkClientConfiguration) && (this.compareTo((HawkClientConfiguration)that) == 0);
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(this.getPathPrefix(), this.getPayloadValidation());
  }

  @Override
  public int compareTo(final HawkClientConfiguration that)
  {
    return ComparisonChain.start()
                          .compare(this.getPathPrefix(), that.getPathPrefix(), Ordering.<String>natural().nullsFirst())
                          .compare(this.getPayloadValidation(), that.getPayloadValidation())
                          .result();
  }

  public static class Builder
  {
    private String pathPrefix;
    private PayloadValidation payloadValidation;

    /**
     * Generate a new builder.
     */
    public Builder()
    {
    }

    /**
     * Generate build with all values set from a prior configuration.
     * @param prior the prior configuration
     */
    public Builder(final HawkClientConfiguration prior)
    {
      this.pathPrefix = prior.pathPrefix;
      this.payloadValidation = prior.payloadValidation;
    }

    /**
     * Override the default path prefix
     * @param pathPrefix the new path prefix value
     * @return The builder
     */
    public Builder pathPrefix(final String pathPrefix)
    {
      this.pathPrefix = pathPrefix;
      return this;
    }

    /**
     * Override the default handling of payload validation.
     * @param payloadValidation the new payload validation value
     * @return The builder
     */
    public Builder payloadValidation(final PayloadValidation payloadValidation)
    {
      this.payloadValidation = payloadValidation;
      return this;
    }

    /**
     * Create a new Hawk client configuration from the defaults
     * and overrides provided.
     * @return The Hawk client configuration
     * @throws com.wealdtech.DataError If the data provided is invalid for a Hawk client configuration
     */
    public HawkClientConfiguration build()
    {
      return new HawkClientConfiguration(this.pathPrefix, this.payloadValidation);
    }
  }
}
