package ghostcat.capstone.password_change;

public class PasswordChangeRequest {
  public String userID;
  public String oldPasswordHash;
  public String newPasswordHash;

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public void setOldPasswordHash(String oldPasswordHash) {
    this.oldPasswordHash = oldPasswordHash;
  }

  public void setNewPasswordHash(String newPasswordHash) {
    this.newPasswordHash = newPasswordHash;
  }

  public String getUserID() {
    return userID;
  }

  public String getOldPasswordHash() {
    return oldPasswordHash;
  }

  public String getNewPasswordHash() {
    return newPasswordHash;
  }
}
