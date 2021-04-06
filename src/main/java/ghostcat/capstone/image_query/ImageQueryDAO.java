package ghostcat.capstone.image_query;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImageQueryDAO {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String PROJECT_TABLE = "ProjectData";
    static String BBOX_IMG_ID_INDEX = "UserID-img_id-index";
    static String IMAGE_TABLE = "Images";
    static String IMAGE_DATE_INDEX = "UserID-img_date-index";
    static String IMAGE_CAMERA_TRAP_INDEX = "UserID-camera_trap-index";

    public ArrayList<Item> queryProjectDataOnUserIDAndProjectID(ImageQueryRequest request) {
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

    public ArrayList<Item> queryProjectDataOnUserID(ImageQueryRequest request) {
        Table userDataTable = dynamoDB.getTable(PROJECT_TABLE);
        ArrayList<Item> results = new ArrayList<>();

        //Query UserData table
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", request.userID)
                );
        ItemCollection<QueryOutcome> items = userDataTable.query(spec);
        for (Item item : items) {
            results.add(item);
        }
        return results;
    }

    public List<Item> queryBBoxOnImageId(String userID, String imageId) {
        List<Item> results = new ArrayList<>();

        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
        Index imgIdIndex = bboxTable.getIndex(BBOX_IMG_ID_INDEX);


        String keyExp = "UserID = :v_userID and img_id = :v_imageId";
        ValueMap values = new ValueMap()
                .withString(":v_userID", userID)
                .withString(":v_img_id", imageId);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression(keyExp)
                .withValueMap(values);

        ItemCollection<QueryOutcome> items = imgIdIndex.query(spec);
        for (Item item : items) {
            results.add(item);
        }

        return results;
    }

    public ArrayList<Item> queryBBoxOnUserID(String userID) {
        ArrayList<Item> results = new ArrayList<>();
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);

        String keyExp = "UserID = :v_userID";
        ValueMap values = new ValueMap()
                .withString(":v_userID", userID);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression(keyExp)
                .withValueMap(values);

        ItemCollection<QueryOutcome> items = bboxTable.query(spec);
        for (Item item : items) {
            results.add(item);
        }
        return results;
    }

    public ArrayList<Item> queryImagesOnMaxDate(ImageQueryRequest request) {
        ArrayList<Item> results = new ArrayList<>();
        Table imgTable = dynamoDB.getTable(IMAGE_TABLE);
        Index dateIndex = imgTable.getIndex(IMAGE_DATE_INDEX);

        String keyExp = "UserID = :v_userID and img_date <= :v_maxDate";
        ValueMap values = new ValueMap()
                .withString(":v_userID", request.userID)
                .withLong(":v_maxDate", request.maxDate);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression(keyExp)
                .withValueMap(values);

        ItemCollection<QueryOutcome> items = dateIndex.query(spec);
        for (Item item : items) {
            results.add(item);
        }
        return results;
    }

    public ArrayList<Item> queryImagesOnMinDate(ImageQueryRequest request) {
        ArrayList<Item> results = new ArrayList<>();
        Table imgTable = dynamoDB.getTable(IMAGE_TABLE);
        Index dateIndex = imgTable.getIndex(IMAGE_DATE_INDEX);

        String keyExp = "UserID = :v_userID and img_date >= :v_minDate";
        ValueMap values = new ValueMap()
                .withString(":v_userID", request.userID)
                .withLong(":v_minDate", request.minDate);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression(keyExp)
                .withValueMap(values);

        ItemCollection<QueryOutcome> items = dateIndex.query(spec);
        for (Item item : items) {
            results.add(item);
        }
        return results;
    }

    public ArrayList<Item> queryImagesOnCameraTrap(ImageQueryRequest request) {
        ArrayList<Item> results = new ArrayList<>();

        Table imgTable = dynamoDB.getTable(IMAGE_TABLE);
        Index cameraTrapIndex = imgTable.getIndex(IMAGE_CAMERA_TRAP_INDEX);

        for (String cameraTrap : request.cameraTraps) {
            String keyExp = "UserID = :v_userID and camera_trap = :v_cameraTrap";
            ValueMap values = new ValueMap()
                    .withString(":v_userID", request.userID)
                    .withString(":v_cameraTrap", cameraTrap);
            QuerySpec spec = new QuerySpec()
                    .withKeyConditionExpression(keyExp)
                    .withValueMap(values);

            ItemCollection<QueryOutcome> items = cameraTrapIndex.query(spec);
            for (Item item : items) {
                results.add(item);
            }
        }

        return results;
    }

    public ArrayList<Item> queryImagesOnUserID(String userID){
        ArrayList<Item> results = new ArrayList<>();
        Table imgTable = dynamoDB.getTable(IMAGE_TABLE);

        String keyExp = "UserID = :v_userID";
        ValueMap values = new ValueMap()
                .withString(":v_userID", userID);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression(keyExp)
                .withValueMap(values);

        ItemCollection<QueryOutcome> items = imgTable.query(spec);
        for (Item item : items) {
            results.add(item);
        }
        return results;
    }
}
