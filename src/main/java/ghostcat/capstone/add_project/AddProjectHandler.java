package ghostcat.capstone.add_project;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.UUID;

public class AddProjectHandler implements RequestHandler<AddProjectRequest, AddProjectResponse> {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String PROJECT_TABLE = "ProjectData";

    /**
     * Method invoked by the lambda. Adds a new project for the given user.
     *
     * @param request Object that contains data for the new project.
     * @return Object that contains a success message, or an error message if something went wrong.
     */
    public AddProjectResponse handleRequest(AddProjectRequest request, Context context) {
      AddProjectResponse response = new AddProjectResponse();

      if (context != null) {
        LambdaLogger logger = context.getLogger();
        logger.log("Called Add Project Handler");
      }

      response = errorCheckRequest(request);
      if (!response.success) return response;

      addProject(request, response);

      return response;
    }

    /**
     * Ensures that a request is valid.
     * @param request Object that contains request parameters.
     * @return response object that will contain error message if request is invalid.
     */
    public static AddProjectResponse errorCheckRequest(AddProjectRequest request) {
      AddProjectResponse response = new AddProjectResponse();
      response.success = true;

      if (request.userID == null) {
        response.success = false;
        response.errorMsg = "Null userID";
      }

      if (request.classNames.isEmpty()) {
        response.success = false;
        response.errorMsg = "Empty class name list";
      }

      return response;
    }

    /**
     * Adds a new project to the ProjectData table in DynamoDB
     * @param request Object that contains request parameters.
     * @param response Response object that will contain error message if an error occurs.
     * @return The number of classes associated with a user's data set.
     */
    public static AddProjectResponse addProject(AddProjectRequest request, AddProjectResponse response) {
      response.success = false;
      response.errorMsg = "An error occurred while trying to insert to DynamoDB";

      Table projectDataTable = dynamoDB.getTable(PROJECT_TABLE);
      Item projectItem = new Item();
      projectItem.withPrimaryKey("UserID", request.userID);

      String projectID = UUID.randomUUID().toString();
      projectItem.withString("ProjectID", projectID);
      response.projectID = projectID;

      for(int i = 0; i < request.classNames.size(); i++) {
        String columnName = "class_" + (i+1);
        projectItem.withString(columnName, request.classNames.get(i));
      }

      projectItem.withInt("num_classes", request.classNames.size());

      projectDataTable.putItem(projectItem);

      response.success = true;
      response.errorMsg = "";
      return response;
    }
}
