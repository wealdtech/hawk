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

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.wealdtech.DataError;

/**
 * HawkCredentials contains the information required to authenticate requests
 * requests between a client and server.
 * <p/>
 * Note that the key in the Hawk credentials is a shared secret, and should be
 * protected accordingly.
 */
public class HawkCredentials implements Comparable<HawkCredentials>
{
  public static final String HMAC_SHA_1 = "hmac-sha-256";
  public static final String HMAC_SHA_256 = "hmac-sha-256";

  public final String keyId;
  public final String key;
  public final String algorithm;

  private final ImmutableMap<String, String> SUPPORTEDALGORITHMS = new ImmutableMap.Builder<String, String>()
                                                                                   .put("hmac-sha-1", "SHA-1")
                                                                                   .put("hmac-sha-256", "SHA-256")
                                                                                   .build();

  private HawkCredentials(final String keyId, final String key, final String algorithm) throws DataError
  {
    this.keyId = keyId;
    this.key = key;
    this.algorithm = algorithm;
    validate();
  }

  /**
   * Carry out validation of the object as part of creation routine.
   *
   * @throws DataError
   *           if there is an issue with the data that prevents creation of the
   *           credentials
   */
  private void validate() throws DataError
  {
    if (this.keyId == null)
    {
      throw new DataError.Missing("The key ID is required");
    }
    if (this.key == null)
    {
      throw new DataError.Bad("The key ID does not contain any information");
    }
    if (!SUPPORTEDALGORITHMS.containsKey(algorithm))
    {
      throw new DataError.Bad("Unknown encryption algorithm");
    }
  }

  /**
   * Obtain the key ID.
   *
   * @return the key ID
   */
  public String getKeyId()
  {
    return this.keyId;
  }

  /**
   * Obtain the key.
   *
   * @return the Key ID. Note that the key ID is a shared secret, and should be
   *         protected accordingly
   */
  public String getKey()
  {
    return this.key;
  }

  /**
   * Obtain the algorithm used to calculate the MAC
   *
   * @return the algorithm used to calculate the MAC
   */
  public String getAlgorithm()
  {
    return this.algorithm;
  }

  /**
   * Obtain the algorithm used to calculate the MAC, using the name as known by
   * Java cryptography functions.
   *
   * @return the algorithm used to calculate the MAC
   */
  public String getJavaAlgorithm()
  {
    return SUPPORTEDALGORITHMS.get(this.algorithm);
  }

  // Standard object methods follow
  @Override
  public String toString()
  {
    return Objects.toStringHelper(this)
                  .add("keyId", this.getKeyId())
                  .add("key", this.getKey())
                  .add("algorithm", this.getAlgorithm())
                  .toString();
  }

  @Override
  public boolean equals(final Object that)
  {
    return (that instanceof HawkCredentials) && (this.compareTo((HawkCredentials)that) == 0);
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(this.getKeyId(), this.getKey(), this.getAlgorithm());
  }

  @Override
  public int compareTo(final HawkCredentials that)
  {
    return ComparisonChain.start()
                          .compare(this.getKeyId(), that.getKeyId())
                          .compare(this.getKey(), that.getKey())
                          .compare(this.getAlgorithm(), that.getAlgorithm())
                          .result();
  }

  /**
   * Builder class to create a set of Hawk credentials.
   */
  public static class Builder
  {
    String keyId;
    String key;
    String algorithm;

    /**
     * Start a new builder.
     */
    public Builder()
    {
    }

    /**
     * Start a new builder, initializing the values with those from an existing
     * set of credentials.
     *
     * @param prior
     *          the prior credentials
     */
    public Builder(final HawkCredentials prior)
    {
      this.keyId = prior.getKeyId();
      this.key = prior.getKey();
      this.algorithm = prior.getAlgorithm();
    }

    /**
     * Set the key ID.
     *
     * @param keyId
     *          the key ID
     * @return the builder
     */
    public Builder keyId(final String keyId)
    {
      this.keyId = keyId;
      return this;
    }

    /**
     * Set the key.
     *
     * @param key
     *          the key
     * @return the builder
     */
    public Builder key(final String key)
    {
      this.key = key;
      return this;
    }

    /**
     * Set the algorithm used to calculate the MAC.
     *
     * @param algorithm
     *          the algorithm used to calculate the MAC
     * @return the builder
     */
    public Builder algorithm(final String algorithm)
    {
      this.algorithm = algorithm;
      return this;
    }

    /**
     * Build the Hawk credentials.
     *
     * @return the Hawk credentials
     * @throws DataError
     *           if there is an issue with the data that prevents creation of
     *           the credentials
     */
    public HawkCredentials build() throws DataError
    {
      return new HawkCredentials(this.keyId, this.key, this.algorithm);
    }
  }
}
