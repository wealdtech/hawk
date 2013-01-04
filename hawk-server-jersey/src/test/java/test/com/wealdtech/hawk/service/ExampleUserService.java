package test.com.wealdtech.hawk.service;

import java.util.Map;

import test.com.wealdtech.hawk.model.ExampleUser;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.wealdtech.DataError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.jersey.auth.PrincipalProvider;

/**
 * Example service to provide users given a key, in this
 * case the Hawk key ID.
 */
public class ExampleUserService implements PrincipalProvider<ExampleUser, String>
{
  private transient final Map<String, ExampleUser> usermap;

  public ExampleUserService() throws DataError
  {
    this.usermap = Maps.newHashMap();
    HawkCredentials user1hawkcredentials = new HawkCredentials.Builder()
                                                              .keyId("dh37fgj492je")
                                                              .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                                              .algorithm(HawkCredentials.HMAC_SHA_256).build();
    ExampleUser user1 = new ExampleUser("Steve", user1hawkcredentials);
    this.usermap.put(user1hawkcredentials.getKeyId(), user1);
//    HawkCredentials user2hawkcredentials = new HawkCredentials.Builder()
//                                                              .keyId("")
//                                                              .key("")
//                                                              .algorithm(HawkCredentials.HMAC_SHA_256).build();
//    ExampleUser user2 = new ExampleUser("John", user2hawkcredentials);
//    this.usermap.put(user2hawkcredentials.getKeyId(), user2);

  }

  @Override
  public Optional<ExampleUser> get(final String hawkKeyId)
  {
    return Optional.fromNullable(this.usermap.get(hawkKeyId));
  }
}
