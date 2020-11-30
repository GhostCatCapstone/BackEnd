package ghostcat.capstone.loginLambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;

import java.time.Instant;
import java.util.UUID;

public class LoginHandler {
  public LoginResponse handleRequest(LoginRequest request, Context context) {
    LoginResponse result = new LoginResponse();
    LoginDAO dao = Factory.loginDAO;

    if (context != null) {
      LambdaLogger logger = context.getLogger();
      logger.log("Called Login handler");
    }

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
