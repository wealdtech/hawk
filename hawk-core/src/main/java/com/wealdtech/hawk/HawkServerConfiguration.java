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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.wealdtech.DataError;

import static com.wealdtech.Preconditions.*;

/**
 * Configuration for a Hawk server. The Hawk server has a number of
 * configuration parameters. These are as follows:
 * <ul>
 * <li>ntpServer: the name of an NTP server to send to the client in the case of
 * a bad request. Defaults to 'pool.ntp.org'</li>
 * <li>timestampSkew: the maximum difference between client and server
 * timestamps, in seconds, for a request to be considered valid. Defaults to 60</li>
 * This is configured as a standard Jackson object and can be realized as part
 * of a {@link com.wealdtech.configuration.ConfigurationSource}.
 */
public class HawkServerConfiguration implements Comparable<HawkServerConfiguration>
{
  private String ntpServer = "pool.ntp.org";
  private Long timestampSkew = 60L;

  /**
   * Create a configuration with values for all options.
   *
   * @param ntpServer
   *          the name of an NTP server, or <code>null</code> for the default
   * @param timestampSkew
   *          the maximum number of seconds of skew to allow between client and
   *          server, or <code>null</code> for the default
   */
  @JsonCreator
  private HawkServerConfiguration(@JsonProperty("ntpserver") final String ntpServer,
                                  @JsonProperty("timestampskew") final Long timestampSkew) throws DataError
  {
    if (ntpServer != null)
    {
      this.ntpServer = ntpServer;
    }
    if (timestampSkew != null)
    {
      this.timestampSkew = timestampSkew;
    }
    validate();
  }

  private void validate() throws DataError
  {
    checkNotNull(this.ntpServer, "The NTP server is required");
    checkNotNull(this.timestampSkew, "The timestamp skew is required");
    checkArgument((this.timestampSkew >= 0), "The timestamp may not be negative");
  }

  public String getNtpServer()
  {
    return this.ntpServer;
  }

  public Long getTimestampSkew()
  {
    return this.timestampSkew;
  }

  // Standard object methods follow
  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
                  .add("ntpServer", this.getNtpServer())
                  .add("timestampSkew", this.getTimestampSkew())
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
    return Objects.hashCode(this.getNtpServer(), this.getTimestampSkew());
  }

  @Override
  public int compareTo(final HawkServerConfiguration that)
  {
    return ComparisonChain.start()
                          .compare(this.getNtpServer(), that.getNtpServer())
                          .compare(this.getTimestampSkew(), that.getTimestampSkew())
                          .result();
  }

  public static class Builder
  {
    String ntpServer;
    Long timestampSkew;

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
    public Builder(final HawkServerConfiguration prior)
    {
      this.ntpServer = prior.ntpServer;
      this.timestampSkew = prior.timestampSkew;
    }

    public Builder ntpServer(final String ntpServer)
    {
      this.ntpServer = ntpServer;
      return this;
    }

    public Builder timestampSkew(final Long timestampSkew)
    {
      this.timestampSkew = timestampSkew;
      return this;

    }
    public HawkServerConfiguration build() throws DataError
    {
      return new HawkServerConfiguration(this.ntpServer, this.timestampSkew);
    }
  }
}
