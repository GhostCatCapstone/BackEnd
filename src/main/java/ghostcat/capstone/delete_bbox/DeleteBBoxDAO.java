package ghostcat.capstone.delete_bbox;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import ghostcat.capstone.holders.Image;
import ghostcat.capstone.update_bbox.UpdateBBoxRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DeleteBBoxDAO {
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String PROJECT_TABLE = "ProjectData";
    static String IMAGE_ID_INDEX = "UserID-img_id-index";

    public int getBBoxCountInImage(String imageID, String userID) {
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
        Index imgIdIndex = bboxTable.getIndex(IMAGE_ID_INDEX);


        String keyExp = "UserID = :v_userID and img_id = :v_imgID";
        ValueMap values = new ValueMap()
                .withString(":v_userID", userID)
                .withString(":v_imgID", imageID);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression(keyExp)
                .withValueMap(values);

        ItemCollection<QueryOutcome> items = imgIdIndex.query(spec);
        Iterator<Item> iterator = items.iterator();
        int count = 0;
        while(iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        return count;
    }

    public boolean deleteBBox(DeleteBBoxRequest request) {
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);

        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey("UserID", request.userID, "BBoxID", request.bboxID);
        DeleteItemOutcome outcome = bboxTable.deleteItem(deleteItemSpec);
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
