package ghostcat.capstone.add_camera_trap;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class AddCameraTrapDAO {

    //Objects used to access DynamoDB
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);

    //Name of table in DynamoDB
    static String CAMERA_TABLE = "CameraTraps";

    /**
     * Performs operation on database
     */
    public AddCameraTrapResponse addCameraTrap(AddCameraTrapRequest request) {
        //Create new response object
        AddCameraTrapResponse response = new AddCameraTrapResponse();

        //Get dynamoDB table
        Table cameraTable = dynamoDB.getTable(CAMERA_TABLE);

        //Create item to add to table
        Item item = new Item().withPrimaryKey("ProjectID", request.projectID)
                .withString("CameraTrapID", request.cameraTrapID)
                .withDouble("camera_lat", request.cameraLat)
                .withDouble("camera_lng", request.cameraLong);

        //Add item to table
        cameraTable.putItem(item);

        return response;
    }

}
