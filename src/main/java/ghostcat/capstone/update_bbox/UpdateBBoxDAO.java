package ghostcat.capstone.update_bbox;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import ghostcat.capstone.image_query.ImageQueryRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UpdateBBoxDAO {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String PROJECT_TABLE = "ProjectData";

    public ArrayList<Item> queryProjectDataOnUserIDAndProjectID(UpdateBBoxRequest request) {
        Table userDataTable = dynamoDB.getTable(PROJECT_TABLE);
        ArrayList<Item> results = new ArrayList<>();

        //Query UserData table
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID and ProjectID = :v_projectID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", request.userID)
                        .withString(":v_projectID", request.projectID)
                );
        ItemCollection<QueryOutcome> items = userDataTable.query(spec);
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) results.add(iterator.next());
        return results;
    }

    public ArrayList<Item> queryProjectDataOnUserID(UpdateBBoxRequest request) {
        Table userDataTable = dynamoDB.getTable(PROJECT_TABLE);
        ArrayList<Item> results = new ArrayList<>();

        //Query UserData table
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", request.userID)
                );
        ItemCollection<QueryOutcome> items = userDataTable.query(spec);
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) results.add(iterator.next());
        return results;
    }

    public boolean setCorrectValueForBBox(UpdateBBoxRequest request, HashMap<String, String> classNames) {
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().
                withPrimaryKey("UserID", request.userID, "BBoxID", request.bboxID)
                .withUpdateExpression("set " + classNames.get(request.correctClassName) + " = :_v")
                .withValueMap(new ValueMap().withNumber(":_v", 1));
        UpdateItemOutcome outcome = bboxTable.updateItem(updateItemSpec);

        //set all elements in classNames that are not in the request to 0
        for (String className : classNames.keySet()) {
            if (!className.equals(request.correctClassName)) {

                UpdateItemSpec uSpec = new UpdateItemSpec().
                        withPrimaryKey("UserID", request.userID, "BBoxID", request.bboxID)
                        .withUpdateExpression("set " + classNames.get(className) + " = :_v")
                        .withValueMap(new ValueMap().withNumber(":_v", 0));
                UpdateItemOutcome result = bboxTable.updateItem(uSpec);

            }
        }

        return true;
    }

    public ArrayList<Item> queryBBoxOnBBoxID(String userID, String bboxID) {
        Table userDataTable = dynamoDB.getTable(BBOX_TABLE);
        ArrayList<Item> results = new ArrayList<>();

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID and BBoxID = :v_bboxID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", userID)
                        .withString(":v_bboxID", bboxID)
                );
        ItemCollection<QueryOutcome> items = userDataTable.query(spec);
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) results.add(iterator.next());
        return results;
    }

}
