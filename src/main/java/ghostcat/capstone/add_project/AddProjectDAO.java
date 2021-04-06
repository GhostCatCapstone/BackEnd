package ghostcat.capstone.add_project;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

import java.util.UUID;

public class AddProjectDAO {

  static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_EAST_2)
          .build();
  static DynamoDB dynamoDB = new DynamoDB(client);
  static String PROJECT_TABLE = "ProjectData";

  public String addProject(AddProjectRequest request) {
    Table projectDataTable = dynamoDB.getTable(PROJECT_TABLE);
    Item projectItem = new Item();
    projectItem.withPrimaryKey("UserID", request.userID);

    String projectID = UUID.randomUUID().toString();
    projectItem.withString("ProjectID", projectID);

    for(int i = 0; i < request.classNames.size(); i++) {
      String columnName = "class_" + (i+1);
      projectItem.withString(columnName, request.classNames.get(i));
    }

    projectItem.withInt("num_classes", request.classNames.size());

    projectDataTable.putItem(projectItem);
    return projectID;
  }
}
