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
import com.google.inject.Inject;
import com.wealdtech.DataError;
import com.wealdtech.hawk.Hawk.PayloadValidation;

/**
 * Configuration for a Hawk server. The Hawk server has a number of
 * configuration parameters. These are as follows:
 * <ul>
 * <li>ntpServer: the name of an NTP server to send to the client in the case of
 * a bad request. Defaults to <code>pool.ntp.org</code></li>
 * <li>timestampSkew: the maximum difference between client and server
 * timestamps, in seconds, for a request to be considered valid. Defaults to <code>60</code></li>
 * <li>bewitAllowed: if authentication of URLs using bewits is allowed.  Defaults to <code>true</code></li>
 * <li>payloadValidation: how to handle payload validation.  Defaults to <code>IFPRESENT</code></li>
 * <li>nonceCacheSize: the maximum number of nonces to hold in cache.  Defaults to <code>10000</code></li>
 * </ul>
 * This is configured as a standard Jackson object and can be realized as part
 * of a ConfigurationSource.
 */
public class HawkServerConfiguration implements Comparable<HawkServerConfiguration>
{
  private String ntpServer = "pool.ntp.org";
  private Long timestampSkew = 60L;
  private Boolean bewitAllowed = true;
  private PayloadValidation payloadValidation = PayloadValidation.IFPRESENT;
  private Long nonceCacheSize = 10000L;

  /**
   * Inject a default configuration if none supplied elsewhere
   */
  @Inject
  private HawkServerConfiguration()
  {
  }

  /**
   * Create a configuration with specified values for all options.
   * Used by builders and ConfigurationSource.
   *
   * @param ntpServer
   *          the name of an NTP server, or <code>null</code> for the default
   * @param timestampSkew
   *          the maximum number of seconds of skew to allow between client and
   *          server, or <code>null</code> for the default
   * @param bewitAllowed
   *          whether or not to allow bewits, or <code>null</code> for the default
   * @param payloadValidation
   *          how to validate against payloads, or <code>null</code> for the default
   * @param nonceCacheSize
   *          the maximum nubmer of nonces to hold in cache, or <code>null</code> for the default
   */
  @JsonCreator
  private HawkServerConfiguration(@JsonProperty("ntpserver") final String ntpServer,
                                  @JsonProperty("timestampskew") final Long timestampSkew,
                                  @JsonProperty("bewitallowed") final Boolean bewitAllowed,
                                  @JsonProperty("payloadvalidation") final PayloadValidation payloadValidation,
                                  @JsonProperty("noncecachesize") final Long nonceCacheSize) throws DataError
  {
    if (ntpServer != null)
    {
      this.ntpServer = ntpServer;
    }
    if (timestampSkew != null)
    {
      this.timestampSkew = timestampSkew;
    }
    if (bewitAllowed != null)
    {
      this.bewitAllowed = bewitAllowed;
    }
    if (payloadValidation != null)
    {
      this.payloadValidation = payloadValidation;
    }
    if (nonceCacheSize != null)
    {
      this.nonceCacheSize = nonceCacheSize;
    }
    validate();
  }

  private void validate() throws DataError
  {
    checkNotNull(this.ntpServer, "The NTP server is required");
    checkNotNull(this.timestampSkew, "The timestamp skew is required");
    checkArgument((this.timestampSkew >= 0), "The timestamp may not be negative");
    checkNotNull(this.bewitAllowed, "Allowance of bewits is required");
    checkNotNull(this.payloadValidation, "Payload validation setting is required");
    checkNotNull(this.nonceCacheSize, "The nonce cache size is required");
    checkArgument((this.nonceCacheSize >= 0), "The nonce cache size may not be negative");
  }

  public String getNtpServer()
  {
    return this.ntpServer;
  }

  public Long getTimestampSkew()
  {
    return this.timestampSkew;
  }

  public Boolean isBewitAllowed()
  {
    return this.bewitAllowed;
  }

  public PayloadValidation getPayloadValidation()
  {
    return this.payloadValidation;
  }

  public Long getNonceCacheSize()
  {
    return this.nonceCacheSize;
  }

  // Standard object methods follow
  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
                  .add("ntpServer", this.getNtpServer())
                  .add("timestampSkew", this.getTimestampSkew())
                  .add("bewitAllowed", this.isBewitAllowed())
                  .add("payloadValidation", this.getPayloadValidation())
                  .add("nonceCacheSize", this.getNonceCacheSize())
                  .toString();
  }

  @Override
  public boolean equals(final Object that)
  {
    return (that instanceof HawkServerConfiguration) && (this.compareTo((HawkServerConfiguration)that) == 0);
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(this.getNtpServer(), this.getTimestampSkew(), this.isBewitAllowed(), this.getPayloadValidation(), this.getNonceCacheSize());
  }

  @Override
  public int compareTo(final HawkServerConfiguration that)
  {
    return ComparisonChain.start()
                          .compare(this.getNtpServer(), that.getNtpServer())
                          .compare(this.getTimestampSkew(), that.getTimestampSkew())
                          .compare(this.isBewitAllowed(), that.isBewitAllowed())
                          .compare(this.getPayloadValidation(), that.getPayloadValidation())
                          .compare(this.getNonceCacheSize(), that.getNonceCacheSize())
                          .result();
  }

  public static class Builder
  {
    String ntpServer;
    Long timestampSkew;
    Boolean bewitAllowed;
    PayloadValidation payloadValidation;
    Long nonceCacheSize;

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
    public Builder(final HawkServerConfiguration prior)
    {
      this.ntpServer = prior.ntpServer;
      this.timestampSkew = prior.timestampSkew;
      this.bewitAllowed = prior.bewitAllowed;
      this.payloadValidation = prior.payloadValidation;
      this.nonceCacheSize = prior.nonceCacheSize;
    }

    /**
     * Override the existing NTP server.
     * @param ntpServer the new NTP server
     * @return The builder
     */
    public Builder ntpServer(final String ntpServer)
    {
      this.ntpServer = ntpServer;
      return this;
    }

    /**
     * Override the existing timestamp skew.
     * @param timestampSkew the new timestamp skew
     * @return The builder
     */
    public Builder timestampSkew(final Long timestampSkew)
    {
      this.timestampSkew = timestampSkew;
      return this;
    }

    /**
     * Override the existing allowance of bewits.
     * @param bewitAllowed if bewits are allowed
     * @return The builder
     */
    public Builder bewitAllowed(final Boolean bewitAllowed)
    {
      this.bewitAllowed = bewitAllowed;
      return this;
    }

    /**
     * Override the existing handling of payload validation.
     * @param payloadValidation
     * @return The builder
     */
    public Builder payloadValidation(final PayloadValidation payloadValidation)
    {
      this.payloadValidation = payloadValidation;
      return this;
    }

    /**
     * Override the existing nonce cache size.
     * @param nonceCacheSize the new nonce cache size
     * @return The builder
     */
    public Builder nonceCacheSize(final Long nonceCacheSize)
    {
      this.nonceCacheSize = nonceCacheSize;
      return this;
    }

    /**
     * Create a new Hawk server configuration from the defaults
     * and overrides provided.
     * @return The Hawk server configuration
     * @throws DataError If the data provided is invalid for a Hawk server configuration
     */
    public HawkServerConfiguration build() throws DataError
    {
      return new HawkServerConfiguration(this.ntpServer, this.timestampSkew, this.bewitAllowed, this.payloadValidation, this.nonceCacheSize);
    }
  }
}
