package ghostcat.capstone.add_user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddUserTest {
  private Context mockContext;

  @BeforeEach
  void setUp() {
    Factory.addUserDAO = mock(AddUserDAO.class);
    when(Factory.addUserDAO.addUser(anyString(), anyString())).thenReturn(true);
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

  @Test
  void handleNullRequest() {
    AddUserRequest request = new AddUserRequest();
    request.UserID = null;
    request.passwordHash = "hashedPassword";

    AddUserHandler addUserHandler = new AddUserHandler();
    AddUserResponse response = addUserHandler.handleRequest(request, mockContext);
    assertFalse(response.success);
  }

  @Test
  void handleBadRequest() {
    when(Factory.addUserDAO.addUser("UserID", "hashedPassword")).thenReturn(false);
    AddUserRequest request = new AddUserRequest();
    request.UserID = "UserID";
    request.passwordHash = "hashedPassword";

    AddUserHandler addUserHandler = new AddUserHandler();
    AddUserResponse response = addUserHandler.handleRequest(request, mockContext);
    assertFalse(response.success);
    assertEquals("Error occurred while trying to insert into DynamoDB", response.errorMsg);
  }
}
