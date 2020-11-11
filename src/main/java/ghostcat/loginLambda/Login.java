package ghostcat.loginLambda;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

public class Login {
  public LoginResult handleRequest(LoginRequest request) {
    LoginResult result = new LoginResult();
    LoginDAO dao = new LoginDAO();

    if (dao.valid(request.getUserID(), request.getPasswordHash())) {
      UUID uuid = UUID.randomUUID();
      StringBuilder time = new StringBuilder();
      time.append(Instant.now().toEpochMilli());
      dao.addAuth(request.userID, uuid.toString(), time.toString());
      result.setAuth_token(uuid.toString());
    }
    else {
      result.setError_message(dao.getError_message());
    }
    return result;
  }

}
