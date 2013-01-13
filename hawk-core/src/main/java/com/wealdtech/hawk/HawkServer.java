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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.Hawk.PayloadValidation;

/**
 * The Hawk server. Note that this is not an HTTP server in itself, but provides
 * the backbone of any Hawk implementation within an HTTP server.
 */
public class HawkServer
{
  private static final Splitter WHITESPACESPLITTER = Splitter.onPattern("\\s+").limit(2);
  private static final Pattern FIELDPATTERN = Pattern.compile("([^=]*)\\s*=\\s*\"([^\"]*)[,\"\\s]*");
  private static final Pattern BEWITPATTERN = Pattern.compile("bewit=([^&]*)");
  private static final Splitter BEWITSPLITTER = Splitter.on('\\');
  private static final String BEWITREMOVEALMATCH = "bewit=[^&]*";

  private final HawkServerConfiguration configuration;
  private LoadingCache<String, Boolean> nonces;

//  /**
//   * Create an instance of the Hawk server with default configuration.
//   */
//  public HawkServer()
//  {
//    this.configuration = new HawkServerConfiguration.Builder().build();
//    initializeCache();
//  }
//
  /**
   * Create an instance of the Hawk server with custom configuration.
   *
   * @param configuration
   *          the specific configuration
   */
  @Inject
  private HawkServer(final HawkServerConfiguration configuration)
  {
    if (configuration == null)
    {
      this.configuration = new HawkServerConfiguration.Builder().build();
    }
    else
    {
      this.configuration = configuration;
    }
    initializeCache();
  }

  private final void initializeCache()
  {
    this.nonces = CacheBuilder.newBuilder()
                              .expireAfterWrite(this.configuration.getTimestampSkew() * 2, TimeUnit.SECONDS)
                              .maximumSize(this.configuration.getNonceCacheSize())
                              .build(new CacheLoader<String, Boolean>() {
                                @Override
                                public Boolean load(String key) {
                                  return false;
                                }
                              });
  }

  /**
   * Authenticate a request using Hawk.
   * @param credentials the Hawk credentials against which to authenticate
   * @param uri the URI of the request
   * @param method the method of the request
   * @param authorizationHeaders the Hawk authentication headers
   * @param hash the hash of the body, if available
   * @throws DataError if the authentication fails due to incorrect or missing data
   * @throws ServerError if there is a problem with the server whilst authenticating
   */
  public void authenticate(final HawkCredentials credentials, final URI uri, final String method, final ImmutableMap<String, String> authorizationHeaders, final String hash) throws DataError, ServerError
  {
    // Ensure that the required fields are present
    checkNotNull(authorizationHeaders.get("ts"), "The timestamp was not supplied");
    checkNotNull(authorizationHeaders.get("nonce"), "The nonce was not supplied");
    checkNotNull(authorizationHeaders.get("id"), "The id was not supplied");
    checkNotNull(authorizationHeaders.get("mac"), "The mac was not supplied");
    if (this.configuration.getPayloadValidation().equals(PayloadValidation.MANDATORY))
    {
      checkNotNull(authorizationHeaders.get("hash"), "The payload hash was not supplied");
      checkNotNull(hash, "The payload hash could not be calculated");
    }

    // Ensure that the timestamp passed in is within suitable bounds
    confirmTimestampWithinBounds(authorizationHeaders.get("ts"));

    // Ensure that this is not a replay of a previous request
    confirmUniqueNonce(authorizationHeaders.get("nonce"));

    // Ensure that the MAC is correct
    final String mac = Hawk.calculateMAC(credentials, Hawk.AuthType.HEADER, Long.valueOf(authorizationHeaders.get("ts")), uri, authorizationHeaders.get("nonce"), method, hash, authorizationHeaders.get("ext"));
    if (!timeConstantEquals(mac, authorizationHeaders.get("mac")))
    {
      throw new DataError.Authentication("The MAC in the request does not match the server-calculated MAC");
    }
  }

  /**
   * Authenticate a request using a Hawk bewit.
   * @param credentials the Hawk credentials against which to authenticate
   * @param uri the URI of the request
   */
  public void authenticate(final HawkCredentials credentials, final URI uri)
  {
    final String bewit = extractBewit(uri);
    final ImmutableMap<String, String> bewitFields = splitBewit(bewit);
    checkState((credentials.getKeyId().equals(bewitFields.get("id"))), "The id in the bewit is not recognised");
    final Long expiry = Long.parseLong(bewitFields.get("expiry"));

    final URI strippedUri = stripBewit(uri);

    final String calculatedMac = Hawk.calculateMAC(credentials, Hawk.AuthType.BEWIT, expiry, strippedUri, null, null, null, bewitFields.get("ext"));
    if (!timeConstantEquals(calculatedMac, bewitFields.get("mac")))
    {
      throw new DataError.Authentication("The MAC in the request does not match the server-calculated MAC");
    }
  }

  // Strip the bewit query parameter from a URI
  private URI stripBewit(final URI uri)
  {
    String uristr = uri.toString().replaceAll(BEWITREMOVEALMATCH, "");
    // If the bewit was the first parameter...
    uristr = uristr.replaceAll("\\?&", "?");
    // If the bewit was the only parameter...
    uristr = uristr.replaceAll("\\?$", "");
    // If the bewit was a middle parameter...
    uristr = uristr.replaceAll("&&", "&");
    // If the bewit was the last parameter...
    uristr = uristr.replaceAll("&$", "");
    try
    {
      return new URI(uristr);
    }
    catch (URISyntaxException use)
    {
      throw new ServerError("Failed to remove bewit from query string");
    }
  }

  // Confirm that the request nonce has not already been seen within the allowable time period
  private void confirmUniqueNonce(final String nonce) throws DataError
  {
    checkState((this.nonces.getUnchecked(nonce) == false), "The nonce supplied is the same as one seen previously");
    this.nonces.put(nonce, true);
  }

  // Confirm that the request timestamp is within an acceptable range of current time
  private void confirmTimestampWithinBounds(final String ts) throws DataError
  {
    Long timestamp;
    try
    {
      timestamp = Long.valueOf(ts);
    }
    catch (Exception e)
    {
      throw new DataError.Bad("The timestamp is in the wrong format; we expect seconds since the epoch");
    }
    long now = System.currentTimeMillis() / 1000;
    checkState((Math.abs(now - timestamp) <= configuration.getTimestampSkew()), "The timestamp is too far from the current time to be acceptable");
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

  // Avoid any weakness through fast-path comparison of strings
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

  /**
   * Split an authorization header into individual fields.
   * @param authorizationheader the Hawk authorization header
   * @return A map of authorization parameters
   * @throws DataError If the authorization header is invalid in some way
   */
  public ImmutableMap<String, String> splitAuthorizationHeader(final String authorizationheader) throws DataError
  {
    checkNotNull(authorizationheader, "No authorization header");
    List<String> headerfields = Lists.newArrayList(WHITESPACESPLITTER.split(authorizationheader));
    checkState((headerfields.size() == 2), "The authorization header does not contain the expected number of fields");
    checkState(("hawk".equals(headerfields.get(0).toLowerCase(Locale.ENGLISH))), "The authorization header is not a Hawk authorization header");

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

  /**
   * Split a base64-encoded bewit into individual fields.
   * @param bewit the base64-encoded bewit
   * @return A map of bewit parameters
   * @throws DataError If the bewit is invalid in some way
   */
  public ImmutableMap<String, String> splitBewit(final String bewit) throws DataError
  {
    checkNotNull(bewit, "No bewit");
    final String decodedBewit = new String(BaseEncoding.base64().decode(bewit));
    List<String> bewitfields = Lists.newArrayList(BEWITSPLITTER.split(decodedBewit));
    checkState((bewitfields.size() == 4), "The bewit did not contain the correct number of values");
    checkState((System.currentTimeMillis() / 1000 <= Long.parseLong(bewitfields.get(1))), "The bewit has expired");

    Map<String, String> bewitMap = Maps.newHashMap();
    bewitMap.put("id", bewitfields.get(0));
    bewitMap.put("expiry", bewitfields.get(1));
    bewitMap.put("mac", bewitfields.get(2));
    bewitMap.put("ext", bewitfields.get(3));
    return ImmutableMap.copyOf(bewitMap);
  }

  /**
   * Extract a bewit from a URI.
   * @param uri the URI from which to pull the bewit
   * @return The bewit
   * @throws DataError if there is an issue with the data that prevents obtaining the bewit
   */
  public String extractBewit(final URI uri)
  {
    checkNotNull(uri, "URI is required but not supplied");
    final String uristr = uri.toString();

    Matcher m = BEWITPATTERN.matcher(uristr);
    checkState(m.find(), "The query string did not contain a bewit");
    final String bewit = m.group(1);
    return  bewit;
  }

  public static class Builder
  {
    private HawkServerConfiguration configuration;

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
    public Builder(final HawkServer prior)
    {
      this.configuration = prior.configuration;
    }

    /**
     * Override the existing configuration.
     * @param configuration the new configuration
     * @return The builder
     */
    public Builder configuration(final HawkServerConfiguration configuration)
    {
      this.configuration = configuration;
      return this;
    }

    /**
     * Build the server
     * @return a new server
     */
    public HawkServer build()
    {
      return new HawkServer(this.configuration);
    }
  }
}
