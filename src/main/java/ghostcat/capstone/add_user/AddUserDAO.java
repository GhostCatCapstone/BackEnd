package ghostcat.capstone.add_user;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class AddUserDAO {

  static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_EAST_1)
          .build();
  static DynamoDB dynamoDB = new DynamoDB(client);
  static String LOGIN_TABLE = "Login";

  public boolean addUser(String userID, String passwordHash) {
    Table loginTable = dynamoDB.getTable(LOGIN_TABLE);
    Item userItem = new Item();

    userItem.withPrimaryKey("UserID", userID);
    userItem.withString("passwordHash", passwordHash);

    loginTable.putItem(userItem);
    return true;
  }
}
