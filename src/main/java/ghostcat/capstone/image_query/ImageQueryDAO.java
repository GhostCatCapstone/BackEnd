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
import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.ClassValue;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ImageQueryDAO {

  static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_EAST_1)
          .build();
  static DynamoDB dynamoDB = new DynamoDB(client);
  static String BBOX_TABLE = "BoundingBoxes";
  static String DATE_INDEX = "UserID-img_date-index";
  static String CAMERA_TRAP_INDEX = "UserID-camera_trap-index";

  /**
   * Retrieves user data from the UserData DynamoDB table. Specifically looks up the number of classes for a user's
   * data set, and the names of those classes. This data is used to correctly filter the user's data based on labels.
   * The method returns the number of classes, and changes the classNames parameter via pass-by-reference. This is so
   * the handleRequest() method will wait until this method returns before it continues.
   * @param request Object that contains query parameters.
   * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
   *      *                   each label's location in the BoundingBox database (e.g. "Cow" -> "class_1"). This is used to
   *      *                   look up what each label's value is for a bounding box.
   * @param response Response object that will contain error message if an error occurs.
   * @return The number of classes associated with a user's data set.
   */
  public int getUserInfo(ImageQueryRequest request, HashMap<String, String> classNames, ImageQueryResponse response) {

    Table userDataTable = dynamoDB.getTable("ProjectData");

    //Query UserData table
    QuerySpec spec = new QuerySpec()
            .withKeyConditionExpression("UserID = :v_userID and ProjectID = :v_projectID")
            .withValueMap(new ValueMap()
                    .withString(":v_userID", request.userID)
                    .withString(":v_projectID", request.projectID)
            );

    ItemCollection<QueryOutcome> items = userDataTable.query(spec);
    Iterator<Item> iterator = items.iterator();

    //Error handling-  query returns no items, check if userID is invalid or if deployment is invalid.
    if (!iterator.hasNext()) {
      response.success = false;

      //Query only on userID. If any items are returned, then the userID is valid and the deployment is invalid.
      spec = new QuerySpec().withKeyConditionExpression("UserID = :v_userID")
              .withValueMap(new ValueMap().withString(":v_userID", request.userID));
      items = userDataTable.query(spec);
      iterator = items.iterator();
      if (!iterator.hasNext()) {
        response.errorMsg = "Invalid userID: " + request.userID;
      }
      else {
        response.errorMsg = "Invalid projectID: " + request.projectID;
      }
      return 0;
    }

    //Format classes as HashMap
    Item item = null;
    int numClasses = 0;
    while (iterator.hasNext()) {
      item = iterator.next();
      numClasses = item.getInt("num_classes");
      for (int i = 1; i <= numClasses; ++i) {
        classNames.put(item.getString("class_" + i), "class_" + i);
      }
    }

    //Error handling- if a request contains a class name that isn't in classNames, then the request is invalid.
    for (ClassValue c : request.classes) {
      if (!classNames.containsKey(c.className)) {
        response.success = false;
        response.errorMsg = "Invalid class name: " + c.className;
        return 0;
      }
    }

    return numClasses;
  }

  /**
   * Queries the BoundingBoxes DynamoDB table and returns a formatted list of the results.
   * @param request Object that contains query parameters.
   * @param numClasses The number of classes in a user's data set.
   * @return An ArrayList of image objects that contains the results of the query.
   */
  public ArrayList<Image> queryBBoxDB(ImageQueryRequest request, HashMap<String, String> classNames,
                                             int numClasses) {
    Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
    Index dateIndex = bboxTable.getIndex(DATE_INDEX);
    Index cameraTrapIndex = bboxTable.getIndex(CAMERA_TRAP_INDEX);

    ItemCollection<QueryOutcome> items = getBBoxItems(request, bboxTable, dateIndex, cameraTrapIndex);
    ArrayList<Image> results = formatQueryResults(numClasses, classNames, items);

    return results;
  }

  /**
   * Constructs a query based on the request, and then uses that query on the BoundingBox table.
   * The BoundingBox table has indexes that can be used to add request parameters to the query.
   * The database can only be queried on one index at a time, so priority is given to the request parameters that might yield
   * the most specific results.
   * @param request Object that contains query parameters.
   * @param bboxTable BoundingBox table to be queried.
   * @param dateIndex Index for date-specific queries.
   * @param cameraTrapIndex Index for camera trap-specific queries.
   * @return The outcome of the database query.
   */
  private ItemCollection<QueryOutcome> getBBoxItems(ImageQueryRequest request, Table bboxTable,
                                                           Index dateIndex, Index cameraTrapIndex) {

    String keyExp = "UserID = :v_userID";
    ValueMap values = new ValueMap()
            .withString(":v_userID", request.userID);

    ItemCollection<QueryOutcome> items;
    if (request.cameraTrap != null) {
      keyExp += " and camera_trap = :v_cameraTrap";
      values.withString(":v_cameraTrap", request.cameraTrap);
      QuerySpec spec = new QuerySpec()
              .withKeyConditionExpression(keyExp)
              .withValueMap(values);

      items = cameraTrapIndex.query(spec);
    }
    else if (request.minDate != null) {
      keyExp += " and img_date >= :v_minDate";
      values.withLong(":v_minDate", request.minDate);
      QuerySpec spec = new QuerySpec()
              .withKeyConditionExpression(keyExp)
              .withValueMap(values);

      items = dateIndex.query(spec);
    }
    else if (request.maxDate != null) {
      keyExp += " and img_date <= :v_maxDate";
      values.withLong(":v_maxDate", request.maxDate);
      QuerySpec spec = new QuerySpec()
              .withKeyConditionExpression(keyExp)
              .withValueMap(values);

      items = dateIndex.query(spec);
    }
    else {
      QuerySpec spec = new QuerySpec()
              .withKeyConditionExpression(keyExp)
              .withValueMap(values);
      items = bboxTable.query(spec);
    }
    return items;
  }

  /**
   * Formats a query result into a list of image objects. Each image object contains a list of bounding boxes.
   * @param numClasses The number of classes in a user's data set.
   * @param items The results of the database query.
   * @return An ArrayList of image objects that contains the results of the query.
   */
  private static ArrayList<Image> formatQueryResults(int numClasses, HashMap<String, String> classNames,
                                                     ItemCollection<QueryOutcome> items) {
    ArrayList<Image> formattedResults = new ArrayList<>();
    Iterator<Item> iter = items.iterator();
    while (iter.hasNext()) {
      Item i = iter.next();
      if (!imgInList(i.getString("img_id"), formattedResults)) {
        Image image = new Image(
                i.getString("img_id"),
                i.getInt("img_height"),
                i.getInt("img_width"),
                i.getBoolean("flash_on"),
                i.getString("camera_make"),
                i.getString("camera_model"),
                i.getLong("img_date"),
                i.getString("camera_trap"),
                i.getString("deployment"),
                i.getBoolean("night_img"),
                i.getString("img_link")
        );
        formattedResults.add(image);
      }

      BoundingBox boundingBox = new BoundingBox(
              i.getString("BBoxID"),
              i.getString("img_id"),
              i.getDouble("bbox_X"),
              i.getDouble("bbox_Y"),
              i.getDouble("bbox_width"),
              i.getDouble("bbox_height"));

      for (int j = 1; j <= numClasses; ++j) {
        boundingBox.classes.put(getNameFromIndex("class_" + j, classNames), i.getDouble("class_" + j));
      }
      addBBox(boundingBox, formattedResults);
    }
    return formattedResults;
  }

  /**
   * Checks if a given image is contained in an ArrayList of images.
   * @param id Image id being checked.
   * @param images ArrayList of images to check the image against
   * @return True if image is in the list, and false if it isn't.
   */
  public static boolean imgInList(String id, ArrayList<Image> images) {
    for (Image i : images) if (id.equals(i.id)) return true;
    return false;
  }

  /**
   * Adds BoundingBox object to the Image object with the correct ID.
   * @param bbox BoundingBox object to be added.
   * @param images Arraylist of images to sort through.
   */
  public static void addBBox(BoundingBox bbox, ArrayList<Image> images) {
    for (Image i : images) if (i.id.equals(bbox.imgId)) i.boundingBoxes.add(bbox);
  }

  public static String getNameFromIndex(String index, HashMap<String, String> classNames) {
    for (String s : classNames.keySet()) {
      if (index.equals(classNames.get(s))) {
        return s;
      }
    }
    return null;
  }
}
