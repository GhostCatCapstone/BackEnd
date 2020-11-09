package ghostcat.loginLambda;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest {

  @Test
  public void testCorrectPassword() {
    LoginRequest request = new LoginRequest();
    request.setUserID("researcherID");
    request.setPassword("r4greog5httr");

    Login login = new Login();
    LoginResult result = login.handleRequest(request);

    Assertions.assertNotNull(result.getAuth_token());
    Assertions.assertNull(result.getError_message());
  }

  @Test
  public void testIncorrectPassword() {
    LoginRequest request = new LoginRequest();
    request.setUserID("researcherID");
    request.setPassword("r4grsfwebs");

    Login login = new Login();
    LoginResult result = login.handleRequest(request);

    Assertions.assertNotNull(result.getError_message());
    Assertions.assertNull(result.getAuth_token());
  }


}