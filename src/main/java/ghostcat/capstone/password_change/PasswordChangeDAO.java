package ghostcat.capstone.password_change;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

public class PasswordChangeDAO {
  private AmazonDynamoDB client;
  private DynamoDB dynamoDB;
  private String error_message;

  public PasswordChangeDAO() {
    error_message = null;
    client = AmazonDynamoDBClientBuilder
        .standard()
        .withRegion(Regions.US_EAST_1)
        .build();
    dynamoDB = new DynamoDB(client);
  }

  public String getError_message() {
    return error_message;
  }

  public boolean validRequest(PasswordChangeRequest request) {
    Table loginTable = dynamoDB.getTable("Login");
    Item user = loginTable.getItem("UserID", request.getUserID());
    if (user == null) {
      error_message = "user not found";
      return false;
    }
    String user_password_hash = user.getString("passwordHash");
    if (!(user_password_hash.equals(request.getOldPasswordHash()))) {
      error_message = "incorrect password";
      return false;
    }
    return true;
  }

  public void changePassword(PasswordChangeRequest request) {
    Table loginTable = dynamoDB.getTable("Login");
    UpdateItemSpec updateItemSpec = new UpdateItemSpec()
        .withPrimaryKey("UserID", request.getUserID())
        .withUpdateExpression("set passwordHash = :_v")
        .withValueMap(new ValueMap().withString(":_v", request.newPasswordHash));
    loginTable.updateItem(updateItemSpec);
  }
}
