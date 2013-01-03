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

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.errors.ErrorInfo;
import com.wealdtech.errors.ErrorInfoMap;

import static com.wealdtech.Preconditions.checkNotNull;

public class HawkServer
{
  private static final Splitter HAWKSPLITTER = Splitter.onPattern("\\s+").limit(2);
  private static final Pattern FIELDPATTERN = Pattern.compile("([^=]*)\\s*=\\s*\"([^\"]*)[,\"\\s]*");

  private final HawkServerConfiguration configuration;

  /**
   * Create an instance of the Hawk server
   */
  public HawkServer()
  {
    this.configuration = new HawkServerConfiguration();
  }

  /**
   * Create an instance of the Hawk server with custom configuration
   *
   * @param configuration
   *          the specific configuration
   */
  public HawkServer(final HawkServerConfiguration configuration)
  {
    this.configuration = configuration;
  }

  public void authenticate(final HawkCredentials credentials, final URI uri, final String method, final ImmutableMap<String, String> authorizationheaders) throws DataError, ServerError
  {
    // Ensure that the timestamp passed in is within suitable bounds
    // TODO

    // Ensure that this is not a replay of a previous request
    // TODO

    final String mac = Hawk.calculateMAC(credentials, Long.valueOf(authorizationheaders.get("ts")), uri, authorizationheaders.get("nonce"), method, authorizationheaders.get("ext"));
    if (!timeConstantEquals(mac, authorizationheaders.get("mac")))
    {
      throw new DataError("Failed to authenticate");
    }
  }

  /**
   * Generate text for a WWW-Authenticate header after a failed authentication.
   * <p>
   * Note that this generates the header's contents, and not the header itself.
   *
   * @return text suitable for placement in a WWW-Authenticate header
   */
  public String generateAuthenticateHeader()
  {
    StringBuilder sb = new StringBuilder(64);
    sb.append("Hawk ts=\"");
    sb.append(String.valueOf(System.currentTimeMillis() / 1000));
    sb.append("\", ntp=\"");
    sb.append(this.configuration.getNtpServer());
    sb.append('"');

    return sb.toString();
  }

  private static boolean timeConstantEquals(final String first, final String second)
  {
    if ((first == null) || (second == null))
    {
      // null always returns false
      return false;
    }
    if (first.length() != second.length())
    {
      return false;
    }
    int res = 0;
    int l = first.length();
    for (int i = 0; i < l; i++)
    {
      if (first.charAt(i) != second.charAt(i))
      {
        res++;
      }
    }
    return (res == 0);
  }

  public static ImmutableMap<String, String> splitAuthorizationHeader(final String authorizationheader) throws DataError
  {
    checkNotNull(authorizationheader);
    List<String> headerfields = Lists.newArrayList(HAWKSPLITTER.split(authorizationheader));
    if (headerfields.size() != 2)
    {
      throw new DataError("Authorization header missing");
    }
    if (!"hawk".equals(headerfields.get(0).toLowerCase(Locale.ENGLISH)))
    {
      throw new DataError("Not a Hawk authorization header");
    }

    Map<String, String> fields = new HashMap<>();
    Matcher m = FIELDPATTERN.matcher(headerfields.get(1));
    while (m.find())
    {
      String key = m.group(1);
      String value = m.group(2);
      fields.put(key, value);
    }
    return ImmutableMap.copyOf(fields);
  }

  // Error messages
  static
  {
    ErrorInfoMap.put(new ErrorInfo("errorCode",
                                   "userMessage",
                                   "developerMessage",
                                   "moreInfo"));
  }
}
