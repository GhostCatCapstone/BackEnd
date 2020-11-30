package ghostcat.capstone.loginLambda;

public class LoginResponse {
  public String auth_token;
  public String error_message;

  public String getAuth_token() {
    return auth_token;
  }

  public String getError_message() {
    return error_message;
  }

  public LoginResponse() {
    auth_token = null;
    error_message = null;
  }

  public void setAuth_token(String auth_token) {
    this.auth_token = auth_token;
  }

  public void setError_message(String error_message) {
    this.error_message = error_message;
  }
}