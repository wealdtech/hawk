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
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.io.BaseEncoding;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;

import static com.wealdtech.Preconditions.*;

/**
 * The Hawk class provides helper methods for calculating the MAC, required by
 * both clients and servers.
 */
public class Hawk
{
  public static final String HAWKVERSION = "1";

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
   * @param authType
   *          The type of the MAC to calculate
   * @param timestamp
   *          timestamp of the request
   * @param uri
   *          URI of the request, including query parameters if appropriate
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
                                    final AuthType authType,
                                    final Long timestamp,
                                    final URI uri,
                                    final String nonce,
                                    final String method,
                                    final String ext) throws DataError, ServerError
  {
    // Check that required parameters are present
    checkNotNull(credentials, "Credentials are required but not supplied");
    checkNotNull(timestamp, "Timestamp is required but not supplied");
    checkNotNull(uri, "URI is required but not supplied");
    checkNotNull(authType, "Authentication type is required but not supplied");

    if (authType.equals(AuthType.CORE))
    {
      // Additional parameters for core authentications
      checkNotNull(nonce, "Nonce is required but not supplied");
      checkNotNull(method, "Method is required but not supplied");
    }

    final StringBuilder sb = new StringBuilder(1024);
    sb.append(authType.toString());
    sb.append('.');
    sb.append(HAWKVERSION);
    sb.append('\n');
    sb.append(timestamp);
    sb.append('\n');
    if (authType.equals(AuthType.CORE))
    {
      sb.append(nonce);
    }
    sb.append('\n');
    if (authType.equals(AuthType.BEWIT))
    {
      sb.append("GET");
    }
    else
    {
      sb.append(method.toUpperCase(Locale.ENGLISH));
    }
    sb.append('\n');
    sb.append(uri.getRawPath());
    if (uri.getQuery() != null)
    {
      sb.append('?');
      sb.append(uri.getRawQuery());
    }
    sb.append('\n');
    sb.append(uri.getHost().toLowerCase(Locale.ENGLISH));
    sb.append('\n');
    sb.append(uri.getPort());
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
  private static String calculateMac(final HawkCredentials credentials, final String text) throws DataError, ServerError
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
        throw new ServerError("Unable to encode with UTF-8", uee);
      }
      return BaseEncoding.base64().encode(md.digest(credentials.getKey().getBytes()));
    }
    catch (NoSuchAlgorithmException nsae)
    {
      throw new DataError.Bad("Unknown encryption algorithm", nsae);
    }
  }

  /**
   * Calculate and return a bewit. The bewit is used to allow access to a resource
   * when passed to a suitable Hawk server.
   *
   * @param credentials
   *          Hawk credentials of the requestor
   * @param uri
   *          URI of the request, including query parameters if appropriate
   * @param ttl
   *          the time to live for the bewit, in seconds
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
  public static String generateBewit(final HawkCredentials credentials,
                                     final URI uri,
                                     final Long ttl,
                                     final String ext) throws DataError, ServerError
  {
    checkNotNull(credentials, "Credentials are required but not supplied");
    checkNotNull(uri, "URI is required but not supplied");
    checkNotNull(ttl, "TTL is required but not supplied");
    checkState((ttl > 0), "TTL must be a positive value");

    // Calculate expiry from ttl and current time
    Long expiry = System.currentTimeMillis() / 1000L + ttl;
    final String mac = Hawk.calculateMAC(credentials, Hawk.AuthType.BEWIT, expiry, uri, null, null, ext);

    final StringBuffer sb = new StringBuffer(256);
    sb.append(credentials.getKeyId());
    sb.append('\\');
    sb.append(String.valueOf(expiry));
    sb.append('\\');
    sb.append(mac);
    sb.append('\\');
    if (ext != null)
    {
      sb.append(ext);
    }
    return BaseEncoding.base64().encode(sb.toString().getBytes());
  }

  public enum AuthType
  {
    CORE,
    BEWIT;

    @Override
    @JsonValue
    public String toString()
    {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }

    @JsonCreator
    public static AuthType parse(final String authType) throws DataError
    {
      try
      {
        return valueOf(authType.toUpperCase(Locale.ENGLISH));
      }
      catch (IllegalArgumentException iae)
      {
        // N.B. we don't pass the iae as the cause of this exception because
        // this happens during invocation, and in that case the enum handler
        // will report the root cause exception rather than the one we throw.
        throw new DataError.Bad("Hawk authentication type \"" + authType + "\" is invalid");
      }
    }
  }
}
