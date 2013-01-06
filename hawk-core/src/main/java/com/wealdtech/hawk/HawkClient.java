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

import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.utils.StringUtils;

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
   * @param uri
   * @param method
   * @param ext
   * @return
   * @throws DataError
   * @throws ServerError
   */
  public String generateAuthorizationHeader(final URI uri,
                                            final String method,
                                            final String ext) throws DataError, ServerError
  {
    long timestamp = System.currentTimeMillis() / 1000;
    final String nonce = StringUtils.generateRandomString(6);
    final String mac = Hawk.calculateMAC(this.credentials, timestamp, uri, nonce, method, ext);

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
