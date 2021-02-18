package ghostcat.capstone.add_bbox;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AddBBoxDAO {
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String PROJECT_TABLE = "ProjectData";
    static String IMAGE_ID_INDEX = "UserID-img_id-index";

    public ArrayList<Item> queryProjectDataOnUserIDAndProjectID(AddBBoxRequest request) {
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

    public ArrayList<Item> queryProjectDataOnUserID(AddBBoxRequest request) {
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

    public boolean addBBox(AddBBoxRequest request, HashMap<String, String> classNames, Image im, String bboxID) {
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
        Item bbItem = new Item()
                .withPrimaryKey("UserID", "researcherID")
                .withString("BBoxID", bboxID)
                .withDouble("bbox_X", request.xVal)
                .withDouble("bbox_Y", request.yVal)
                .withDouble("bbox_width", request.width)
                .withDouble("bbox_height", request.height)
                .withInt("img_height", im.imgHeight)
                .withInt("img_width", im.imgWidth)
                .withBoolean("flash_on", im.flash)
                .withBoolean("night_img", im.night_im)
                .withString("camera_make", im.make)
                .withString("camera_model", im.model)
                .withLong("img_date", im.date)
                .withString("img_id", im.id)
                .withString("camera_trap", im.cameraTrap)
                .withString("img_link", im.imgLink)
                .withString("deployment", im.deployment);
        for (String key : classNames.keySet()) {
            if (key.equals(request.className)) bbItem.withDouble(classNames.get(key), 1);
            else bbItem.withDouble(classNames.get(key), 0);
        }

        bboxTable.putItem(bbItem);
        return true;
    }

    public Image queryBBoxOnImageID(String imageID, String userID) {
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
        if (!iterator.hasNext()) return null;
        Item i = iterator.next();
        Image image = new Image(
                i.getString("img_id"),
                i.getInt("img_width"),
                i.getInt("img_height"),
                i.getBoolean("flash_on"),
                i.getString("camera_make"),
                i.getString("camera_model"),
                i.getLong("img_date"),
                i.getString("camera_trap"),
                i.getString("deployment"),
                i.getBoolean("night_img"),
                i.getString("img_link")
        );
        return image;
    }
}
