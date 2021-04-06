package ghostcat.capstone.get_project_data;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;

public class GetProjectDataDAO {
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String PROJECT_TABLE = "ProjectData";
    static String CAMERA_TRAP_TABLE = "CameraTraps";

    public ArrayList<Item> queryProjectDataOnUserID(String userID) {
        Table userDataTable = dynamoDB.getTable(PROJECT_TABLE);
        ArrayList<Item> results = new ArrayList<>();

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", userID)
                );
        ItemCollection<QueryOutcome> items = userDataTable.query(spec);
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) results.add(iterator.next());
        return results;
    }

    public ArrayList<Item> queryCameraTrapsOnProjectID(String projectID) {
        Table cameraTrapTable = dynamoDB.getTable(CAMERA_TRAP_TABLE);
        ArrayList<Item> results = new ArrayList<>();
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("ProjectID = :v_projectID")
                .withValueMap(new ValueMap()
                        .withString(":v_projectID", projectID)
                );
        ItemCollection<QueryOutcome> items = cameraTrapTable.query(spec);
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) results.add(iterator.next());
        return results;
    }
}
