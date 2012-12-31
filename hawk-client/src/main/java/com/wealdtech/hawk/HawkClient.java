package com.wealdtech.hawk;

import java.net.URL;

import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.WealdUtils;

public class HawkClient
{
  private final HawkCredentials credentials;

  public HawkClient(final HawkCredentials credentials)
  {
    this.credentials = credentials;
  }

  /**
   * Generate the value for the Hawk authorization header.
   * 
   * @param url
   * @param method
   * @param ext
   * @return
   * @throws DataError
   * @throws ServerError
   */
  public String generateAuthorizationHeader(final URL url,
                                            final String method,
                                            final String ext) throws DataError, ServerError
  {
    long timestamp = System.currentTimeMillis() / 1000;
    final String nonce = WealdUtils.generateRandomString(6);
    final String mac = Hawk.calculateMAC(this.credentials, timestamp, url, nonce, method, ext);

    final StringBuilder sb = new StringBuilder(1024);
    sb.append("Hawk id=\"");
    sb.append(this.credentials.getKeyId());
    sb.append("\", ts=\"");
    sb.append(timestamp);
    sb.append("\", nonce=\"");
    sb.append(nonce);
    if ((ext != null) && (!"".equals(ext)))
    {
      sb.append("\", ext=\"");
      sb.append(ext);
    }
    sb.append("\", mac=\"");
    sb.append(mac);
    sb.append('"');

    return sb.toString();
  }
}
