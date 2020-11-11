package ghostcat.loginLambda;

public class LoginRequest {
  public String userID;
  public String passwordHash;

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public void setPassword(String password_encoded) {
    this.passwordHash = password_encoded;
  }

  public String getUserID() {
    return userID;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
}
