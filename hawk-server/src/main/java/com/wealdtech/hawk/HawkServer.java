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

import java.net.URL;
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

public class HawkServer<T>
{
  private static final Splitter HAWKSPLITTER = Splitter.onPattern("\\s+").limit(2);
  private static final Pattern FIELDPATTERN = Pattern.compile("([^=]*)\\s*=\\s*\"([^\"]*)[,\"\\s]*");

  public static void authenticate(final HawkCredentials credentials, final URL url, final String method, final String authorizationheader) throws DataError, ServerError
  {
    // First off ensure that we are a Hawk authorization
    final String fieldsstr = splitAuthorizationHeader(authorizationheader);

    // Now obtain the various fields
    ImmutableMap<String, String> fields = splitAuthFields(fieldsstr);

    final String mac = Hawk.calculateMAC(credentials, Long.valueOf(fields.get("ts")), url, fields.get("nonce"), method, fields.get("ext"));
    if (!timeConstantEquals(mac, fields.get("mac")))
    {
      throw new DataError("Failed to authenticate");
    }
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

  private static String splitAuthorizationHeader(final String authorizationheader) throws DataError
  {
    List<String> headerfields = Lists.newArrayList(HAWKSPLITTER.split(authorizationheader));
    if (headerfields.size() != 2)
    {
      throw new DataError("Authorization header missing");
    }
    if (!"hawk".equals(headerfields.get(0).toLowerCase(Locale.ENGLISH)))
    {
      throw new DataError("Not a Hawk authorization header");
    }
    return headerfields.get(1);
  }

  private static ImmutableMap<String, String> splitAuthFields(final String fieldstr)
  {
    Map<String, String> fields = new HashMap<>();
    Matcher m = FIELDPATTERN.matcher(fieldstr);
    while (m.find())
    {
      String key = m.group(1);
      String value = m.group(2);
      fields.put(key, value);
    }
    return ImmutableMap.copyOf(fields);
  }
}
