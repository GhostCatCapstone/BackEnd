package ghostcat.loginLambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class LoginDAO {

  private String error_message;
  private AmazonDynamoDB client;
  private DynamoDB dynamoDB;

  public LoginDAO() {
    client = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    dynamoDB = new DynamoDB(client);
  }

  public String getError_message() {
    return error_message;
  }

  public boolean valid(String userID, String passwordHash) {
    Table loginTable = dynamoDB.getTable("Login");
    Item user = loginTable.getItem("UserID", userID);
    if (user == null) {
      error_message = "user not found";
      return false;
    }
    String user_password_hash = user.getString("passwordHash");
    if (!(user_password_hash.equals(passwordHash))) {
      error_message = "incorrect password";
      return false;
    }
    return true;
  }

  public void addAuth(String userID, String authToken, String StartTime) {
    Table authTable = dynamoDB.getTable("Auth");
    Item auth = new Item().withPrimaryKey("authToken", authToken)
            .withString("time", StartTime).withString("userID", userID);
    authTable.putItem(auth);
  }

}
