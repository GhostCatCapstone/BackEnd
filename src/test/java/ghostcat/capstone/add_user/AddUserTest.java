package ghostcat.capstone.add_user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddUserTest {
  private Context mockContext;

  @BeforeEach
  void setUp() {
    AddUserDAO mockDao = mock(AddUserDAO.class);
    Factory.addUserDAO = mockDao;
    mockContext = mock(Context.class);
    when(mockContext.getLogger()).thenReturn(mock(LambdaLogger.class));
  }

  @Test
  void handleCorrectRequest() {
    AddUserRequest request = new AddUserRequest();
    request.UserID = "userID";
    request.passwordHash = "hashedPassword";

    AddUserHandler addUserHandler = new AddUserHandler();
    AddUserResponse response = addUserHandler.handleRequest(request, mockContext);
    assertTrue(response.success);
  }
}
