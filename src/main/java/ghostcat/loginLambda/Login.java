package ghostcat.loginLambda;

import java.security.SecureRandom;
import java.time.Instant;

public class Login {
  public LoginResult handleRequest(LoginRequest request) {
    LoginResult result = new LoginResult();
    LoginDAO dao = new LoginDAO();

    if (dao.valid(request.getUserID(), request.getPasswordHash())) {
      String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
      SecureRandom rnd = new SecureRandom();
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < 10; ++i) {
        stringBuilder.append( AB.charAt( rnd.nextInt(AB.length()) ) );
      }
      StringBuilder time = new StringBuilder();
      time.append(Instant.now().toEpochMilli());
      stringBuilder.append(time.toString());
      String auth = stringBuilder.toString();
      dao.addAuth(request.userID, auth, time.toString());
      result.setAuth_token(auth);
    }
    else {
      result.setError_message(dao.getError_message());
    }
    return result;
  }

}
