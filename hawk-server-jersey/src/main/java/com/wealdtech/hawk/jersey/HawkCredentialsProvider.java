package com.wealdtech.hawk.jersey;

import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;

import static com.wealdtech.Preconditions.checkNotNull;

/**
 * Provide Hawk credentials to aid in authentication.
 * <p/>
 * This class should be overridden to allow obtaining of
 * your own credentials from wherever you may store them.
 *
 */
public class HawkCredentialsProvider
{
  /**
   * Obtain credentials from a datastore.
   * @param id an identifier for the credentials to be obtained
   * @return A set of Hawk credentials, or <code>null</code> if the credentials do not exist
   * @throws DataError if there is a problem with the ID
   * @throws ServerError if there is a problem obtaining the credentials
   */
  public HawkCredentials getCredentials(final String id) throws DataError, ServerError
  {
    checkNotNull(id);
    if (id.equals("dh37fgj492je"))
    {
      return new HawkCredentials.Builder()
                                .keyId("dh37fgj492je")
                                .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                .algorithm("hmac-sha-256")
                                .build();
    }
    else
    {
      return null;
    }
  }
}
