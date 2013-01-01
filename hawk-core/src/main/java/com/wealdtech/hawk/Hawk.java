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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import com.google.common.io.BaseEncoding;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;

/**
 * The Hawk class provides helper methods for calculating the MAC, required by
 * both clients and servers.
 */
public class Hawk
{
  /**
   * Calculate and return a MAC. The MAC is used to sign the method and
   * parameters passed as part of a request. It forms the basis to allow the
   * server to verify that the request has not been tampered with.
   * <p>
   * Note that there is no validation of the parameters except to confirm that
   * mandatory parameters are not null.
   * 
   * @param credentials
   *          Hawk credentials of the requestor
   * @param timestamp
   *          timestamp of the request
   * @param url
   *          URL of the request, including query parameters if appropriate
   * @param nonce
   *          nonce a random string used to uniquely identify the request
   * @param method
   *          the HTTP method of the request
   * @param ext
   *          optional extra data, as supplied by the requestor to differentiate
   *          the request if required
   * @return the MAC
   * @throws DataError
   *           if there is an issue with the data that prevents creation of the
   *           MAC
   * @throws ServerError
   *           if there is an issue with the server that prevents creation of
   *           the MAC
   */
  public static String calculateMAC(final HawkCredentials credentials,
                                    final Long timestamp,
                                    final URL url,
                                    final String nonce,
                                    final String method,
                                    final String ext) throws DataError, ServerError
  {
    // Check that required parameters are present
    checkNotNull(timestamp);
    checkNotNull(url);
    checkNotNull(nonce);
    checkNotNull(method);

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

  /**
   * Internal method to generate the MAC given the compiled string to sign
   * 
   * @param credentials
   *          Hawk credentials of the requestor
   * @param text
   *          the compiled string
   * @return the MAC
   * @throws DataError
   *           if there is an issue with the data that prevents creation of the
   *           MAC
   * @throws ServerError
   *           if there is an issue with the server that prevents creation of
   *           the MAC
   */
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
      return BaseEncoding.base64().encode(md.digest());
    }
    catch (NoSuchAlgorithmException nsae)
    {
      throw new DataError("Unknown encryption algorithm \"" + credentials.getAlgorithm() + "\"", nsae);
    }
  }
}
