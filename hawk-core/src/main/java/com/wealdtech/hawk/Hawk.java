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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import sun.misc.BASE64Encoder;

import com.wealdtech.DataError;
import com.wealdtech.ServerError;

public class Hawk
{
  /**
   * Calculate a MAC
   * 
   * @param credentials
   * @param timestamp
   * @param uri
   * @param nonce
   * @param method
   * @param ext
   * @return
   */
  public static String calculateMAC(final HawkCredentials credentials,
                                    final Long timestamp,
                                    final URL url,
                                    final String nonce,
                                    final String method,
                                    final String ext) throws DataError, ServerError
  {
    final StringBuilder sb = new StringBuilder(1024);
    sb.append(timestamp);
    sb.append('\n');
    sb.append(nonce);
    sb.append('\n');
    sb.append(method.toUpperCase(Locale.ENGLISH));
    sb.append('\n');
    sb.append(url.getPath());
    if (url.getQuery() != null)
    {
      sb.append('?');
      sb.append(url.getQuery());
    }
    sb.append('\n');
    sb.append(url.getHost().toLowerCase(Locale.ENGLISH));
    sb.append('\n');
    sb.append(url.getPort());
    sb.append('\n');
    if (ext != null)
    {
      sb.append(ext);
    }
    sb.append('\n');

    return calculateMac(credentials, sb.toString());
  }

  private static String calculateMac(final HawkCredentials credentials,
                                     final String text) throws DataError, ServerError
  {
    try
    {
      MessageDigest md = MessageDigest.getInstance(credentials.getJavaAlgorithm());
      try
      {
        md.update(text.getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException uee)
      {
        throw new ServerError("Unable to encode with UTF-8!", uee);
      }
      return new BASE64Encoder().encode(md.digest());
    }
    catch (NoSuchAlgorithmException nsae)
    {
      throw new DataError("Unknown encryption algorithm \"" + credentials.getAlgorithm() + "\"", nsae);
    }
  }
}
