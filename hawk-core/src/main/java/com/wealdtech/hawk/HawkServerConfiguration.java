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

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class HawkServerConfiguration
{
  private String ntpServer = "pool.ntp.org";
  private Long timestampSkew = 60L;

  /**
   * Create a configuration using default values for all options.
   */
  public HawkServerConfiguration()
  {
  }

  /**
   * Create a configuration with values for all options.
   *
   * @param ntpServer
   *          the name of an NTP server, or <code>null</code> for the default
   * @param timestampSkew
   *          the maximum number of seconds of skew to allow between client and
   *          server, or <code>null</code> for the default
   */
  public HawkServerConfiguration(@JsonProperty("ntpserver") final String ntpServer,
                                 @JsonProperty("timestampskew") final Long timestampSkew)
  {
    if (ntpServer != null)
    {
      this.ntpServer = ntpServer;
    }
    if (timestampSkew != null)
    {
      this.timestampSkew = timestampSkew;
    }
  }

  public String getNtpServer()
  {
    return this.ntpServer;
  }

  public Long getTimestampSkew()
  {
    return this.timestampSkew;
  }
}
