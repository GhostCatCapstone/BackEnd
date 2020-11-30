package ghostcat.capstone.add_project;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AddProjectTest {
  private Context mockContext;

  @BeforeEach
  void setUp() {
    Factory.addProjectDAO = mock(AddProjectDAO.class);
    mockContext = mock(Context.class);
    when(mockContext.getLogger()).thenReturn(mock(LambdaLogger.class));
  }

  @Test
  void handleCorrectRequest() {
    AddProjectRequest request = new AddProjectRequest();
    request.userID = "userID";
    request.classNames = new ArrayList<String>(Arrays.asList("leopard", "cheetah", "lion"));

    AddProjectHandler addProjectHandler = new AddProjectHandler();
    AddProjectResponse response = addProjectHandler.handleRequest(request, mockContext);
    assertTrue(response.success);
  }

  @Test
  void handleBadRequests() {
    AddProjectRequest request = new AddProjectRequest();
    request.userID = null;
    request.classNames = new ArrayList<String>(Arrays.asList("leopard", "cheetah", "lion"));

    AddProjectHandler addProjectHandler = new AddProjectHandler();
    AddProjectResponse response = addProjectHandler.handleRequest(request, mockContext);
    assertFalse(response.success);
    assertEquals("Null userID", response.errorMsg);

    response.success = true;
    request.userID = "userID";
    request.classNames = new ArrayList<>();

    response = addProjectHandler.handleRequest(request, mockContext);
    assertFalse(response.success);
    assertEquals("Empty class name list", response.errorMsg);
  }
}