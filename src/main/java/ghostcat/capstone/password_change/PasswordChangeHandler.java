package ghostcat.capstone.password_change;

public class PasswordChangeHandler {
  public PasswordChangeResponse handleRequest(PasswordChangeRequest request) {
    PasswordChangeResponse response = new PasswordChangeResponse();
    PasswordChangeDAO dao = new PasswordChangeDAO();

    if (!dao.validRequest(request)) {
      response.setError_message(dao.getError_message());
      return response;
    }

    dao.changePassword(request);
    response.setSuccess_message("password changed");

    return response;
  }
}
