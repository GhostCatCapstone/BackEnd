package ghostcat.capstone.loginLambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginHandlerTest {
  private Context mockContext;

  @BeforeEach
  void setUp() {
    Factory.loginDAO = mock(LoginDAO.class);
    when(Factory.loginDAO.getError_message()).thenReturn("Error");
    mockContext = mock(Context.class);
    when(mockContext.getLogger()).thenReturn(mock(LambdaLogger.class));
  }

  @Test
  public void testCorrectPassword() {
    when(Factory.loginDAO.valid(any(), any())).thenReturn(true);

    LoginRequest request = new LoginRequest();
    request.setUserID("researcherID");
    request.setPassword("r4greog5httr");

    LoginHandler login = new LoginHandler();
    LoginResponse result = login.handleRequest(request, mockContext);

    assertNotNull(result.getAuth_token());
    assertNull(result.getError_message());
  }

  @Test
  public void testIncorrectPassword() {
    when(Factory.loginDAO.valid(any(), any())).thenReturn(false);

    LoginRequest request = new LoginRequest();
    request.setUserID("researcherID");
    request.setPassword("r4grsfwebs");

    LoginHandler login = new LoginHandler();
    LoginResponse result = login.handleRequest(request, mockContext);

    assertNotNull(result.getError_message());
    assertNull(result.getAuth_token());
  }
}