package ghostcat.capstone.password_change;

import ghostcat.capstone.holders.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordChangeHandlerTest {

  @BeforeEach
  void setUp() {
    Factory.passwordChangeDAO = mock(PasswordChangeDAO.class);
  }

  @Test
  public void testCorrectPassword() {
    when(Factory.passwordChangeDAO.validRequest(any())).thenReturn(true);

    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setUserID("researcherID");
    request.setOldPasswordHash("password");
    request.setNewPasswordHash("new_password");

    PasswordChangeHandler passwordChange = new PasswordChangeHandler();
    PasswordChangeResponse response = passwordChange.handleRequest(request);

    Assertions.assertNotNull(response.getSuccess_message());
    Assertions.assertNull(response.getError_message());
  }

  @Test
  void wrongPassword() {
    when(Factory.passwordChangeDAO.getError_message()).thenReturn("incorrect password");
    when(Factory.passwordChangeDAO.validRequest(any())).thenReturn(false);

    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setUserID("researcherID");
    request.setOldPasswordHash("not_password");
    request.setNewPasswordHash("new_password");

    PasswordChangeHandler passwordChange = new PasswordChangeHandler();
    PasswordChangeResponse response = passwordChange.handleRequest(request);

    Assertions.assertNull(response.getSuccess_message());
    Assertions.assertEquals("incorrect password", response.getError_message());
  }

  @Test
  void wrongUserID() {
    when(Factory.passwordChangeDAO.getError_message()).thenReturn("user not found");
    when(Factory.passwordChangeDAO.validRequest(any())).thenReturn(false);

    PasswordChangeRequest request = new PasswordChangeRequest();
    request.setUserID("not_a_user");
    request.setNewPasswordHash("password");
    request.setNewPasswordHash("new_password");

    PasswordChangeHandler passwordChange = new PasswordChangeHandler();
    PasswordChangeResponse response = passwordChange.handleRequest(request);

    Assertions.assertNull(response.getSuccess_message());
    Assertions.assertEquals("user not found", response.getError_message());
  }



}