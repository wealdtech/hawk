package com.wealdtech.hawk;

import com.google.common.collect.ImmutableMap;
import com.wealdtech.DataError;

public class HawkCredentials
{
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

  private void validate() throws DataError
  {
    if (this.keyId == null)
    {
      throw new DataError("Key ID is NULL");
    }
    if (this.key == null)
    {
      throw new DataError("Key is NULL");
    }
    if (!SUPPORTEDALGORITHMS.containsKey(algorithm))
    {
      throw new DataError("Unknown algorithm \"" + algorithm + "\"");
    }
  }

  public String getKeyId()
  {
    return this.keyId;
  }

  public String getKey()
  {
    return this.key;
  }

  public String getAlgorithm()
  {
    return this.algorithm;
  }

  /**
   * Obtain the name of the algorithm as known by Java.
   * 
   * @return
   */
  public String getJavaAlgorithm()
  {
    return SUPPORTEDALGORITHMS.get(this.algorithm);
  }

  public static class Builder
  {
    String keyId;
    String key;
    String algorithm;

    public Builder()
    {
    }

    public Builder(final HawkCredentials prior)
    {
      this.keyId = prior.getKeyId();
      this.key = prior.getKey();
      this.algorithm = prior.getAlgorithm();
    }

    public Builder keyId(final String keyId)
    {
      this.keyId = keyId;
      return this;
    }

    public Builder key(final String key)
    {
      this.key = key;
      return this;
    }

    public Builder algorithm(final String algorithm)
    {
      this.algorithm = algorithm;
      return this;
    }

    public HawkCredentials build() throws DataError
    {
      return new HawkCredentials(this.keyId, this.key, this.algorithm);
    }
  }
}
