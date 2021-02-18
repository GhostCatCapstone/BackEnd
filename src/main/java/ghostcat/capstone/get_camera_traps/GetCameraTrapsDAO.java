package ghostcat.capstone.get_camera_traps;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import ghostcat.capstone.delete_bbox.DeleteBBoxRequest;

import java.util.ArrayList;
import java.util.Iterator;

public class GetCameraTrapsDAO {
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String CAMERA_TRAP_TABLE = "CameraTraps";

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
