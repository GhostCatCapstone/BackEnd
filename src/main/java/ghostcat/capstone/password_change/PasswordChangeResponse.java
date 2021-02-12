package ghostcat.capstone.password_change;

public class PasswordChangeResponse {
  private String success_message;
  private String error_message;

  public PasswordChangeResponse() {
    success_message = null;
    error_message = null;
  }

  public String getSuccess_message() {
    return success_message;
  }

  public void setSuccess_message(String success_message) {
    this.success_message = success_message;
  }

  public String getError_message() {
    return error_message;
  }

  public void setError_message(String error_message) {
    this.error_message = error_message;
  }
}
