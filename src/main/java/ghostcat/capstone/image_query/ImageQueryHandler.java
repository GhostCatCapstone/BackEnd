package ghostcat.capstone.image_query;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ImageQueryHandler {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String DATE_INDEX = "UserID-img_date-index";
    static String CAMERA_TRAP_INDEX = "UserID-camera_trap-index";

    public static void main(String[] args) {
        ImageQueryRequest request = new ImageQueryRequest();
        request.userID = "researcherID";
        request.deployment = "photos_spring2019";
        request.authToken = "helloWorld";
        request.cameraTrap = "site002";
        request.minDate = Long.valueOf("1556228937000");
        request.maxDate = Long.valueOf("1556228942000");
        request.classes.put("Cow", .99);
        handleRequest(request, null);
    }

    /**
     * Method invoked by the lambda. Queries DynamoDB, and filters the results based on the request.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains query results, or an error message if something went wrong.
     */
    public static ImageQueryResponse handleRequest(ImageQueryRequest request, Context context) {
        ImageQueryResponse response = new ImageQueryResponse();

//        LambdaLogger logger = context.getLogger();
//        logger.log("Called ImageQueryHandler");

        if (!validToken(request.authToken, request.userID)) {
            response.errorMsg = "Invalid token";
            response.success = false;
        }
        HashMap<String, String> classNames = new HashMap<>();
        int numClasses = getUserInfo(request, classNames);
        ArrayList<Image> imgResults = queryBBoxDB(request, numClasses);
        ArrayList<Image> filteredResults = filterResultsOnMetadata(imgResults, request);
        filteredResults = filterResultsOnClass(filteredResults, request, classNames);

        response.images = filteredResults;
        response.success = true;

        return response;
    }

    /**
     * Determines the user's authentication status.
     * @param authToken String passed from the front end that represents user authentication
     * @param userID User's ID
     * @return True if user is authenticated, false if user is not authenticated.
     */
    public static boolean validToken(String authToken, String userID) {
        return true;
    }

    /**
     * Filters a list of images by the image metadata parameters provided by the request body.
     * @param imgResults ArrayList of image objects that resulted from the DynamoDB query.
     * @param request Object that contains query parameters.
     * @return ArrayList of image objects that have been filtered by image metadata parameters.
     */
    public static ArrayList<Image> filterResultsOnMetadata(ArrayList<Image> imgResults, ImageQueryRequest request) {
        ArrayList<Image> filtered = new ArrayList<>();
        for (Image i : imgResults) {
            boolean addImage = true;
            if (request.cameraTrap != null) {
                if (!request.cameraTrap.equals(i.cameraTrap)) addImage = false;
            }
            if (request.deployment != null) {
                if (!request.deployment.equals(i.deployment)) addImage = false;
            }
            if (request.maxDate != null) {
                if (request.maxDate < i.date) addImage = false;
            }
            if (request.minDate != null) {
                if (request.minDate > i.date) addImage = false;
            }
            if (addImage) filtered.add(i);
        }
        return filtered;
    }

    /**
     * Filters a list of images by the label parameters provided by the request body.
     * @param imgResults ArrayList of image objects that resulted from the DynamoDB query.
     * @param request Object that contains query parameters.
     * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
     *                   each label's location in the BoundingBox database (e.g. "Cow" -> "class_1"). This is used to
     *                   look up what each label's value is for a bounding box.
     * @return
     */
    public static ArrayList<Image> filterResultsOnClass(ArrayList<Image> imgResults, ImageQueryRequest request,
                                                        HashMap<String, String> classNames) {
        ArrayList<Image> filteredImages = new ArrayList<>();
        if (request.classes == null) return imgResults;
        for (Image i : imgResults) {
            boolean addImg = true;
            for (BoundingBox b : i.boundingBoxes) {
                for (String c : request.classes.keySet()) {
                    //Step through this section with a breakpoint to get a clearer understanding of what is happening.
                    String class_index = classNames.get(c);
                    Double class_val = b.classes.get(class_index);
                    Double class_threshold = request.classes.get(c);
                    if (class_val < class_threshold) addImg = false;
                }
            }
            if (addImg) filteredImages.add(i);
        }
        return filteredImages;
    }

    /**
     * Retrieves user data from the UserData DynamoDB table. Specifically looks up the number of classes for a user's
     * data set, and the names of those classes. This data is used to correctly filter the user's data based on labels.
     * The method returns the number of classes, and changes the classNames parameter via pass-by-reference. This is so
     * the handleRequest() method will wait until this method returns before it continues.
     * @param request Object that contains query parameters.
     * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
     *      *                   each label's location in the BoundingBox database (e.g. "Cow" -> "class_1"). This is used to
     *      *                   look up what each label's value is for a bounding box.
     * @return The number of classes associated with a user's data set.
     */
    public static int getUserInfo(ImageQueryRequest request, HashMap<String, String> classNames) {

        Table userDataTable = dynamoDB.getTable("UserData");

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID and DeploymentID = :v_deploymentID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", request.userID)
                        .withString(":v_deploymentID", request.deployment)
                );

        ItemCollection<QueryOutcome> items = userDataTable.query(spec);
        Iterator<Item> iterator = items.iterator();
        Item item = null;
        int numClasses = 0;
        while (iterator.hasNext()) {
            item = iterator.next();
            numClasses = item.getInt("num_classes");
            for (int i = 1; i <= numClasses; ++i) {
                classNames.put(item.getString("class_" + i), "class_" + i);
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
    public static ArrayList<Image> queryBBoxDB(ImageQueryRequest request, int numClasses) {
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
        Index dateIndex = bboxTable.getIndex(DATE_INDEX);
        Index cameraTrapIndex = bboxTable.getIndex(CAMERA_TRAP_INDEX);

        ItemCollection<QueryOutcome> items = getBBoxItems(request, bboxTable, dateIndex, cameraTrapIndex);
        ArrayList<Image> results = formatQueryResults(numClasses, items);

        return results;
    }

    /**
     * Formats a query result into a list of image objects. Each image object contains a list of bounding boxes.
     * @param numClasses The number of classes in a user's data set.
     * @param items The results of the database query.
     * @return An ArrayList of image objects that contains the results of the query.
     */
    private static ArrayList<Image> formatQueryResults(int numClasses, ItemCollection<QueryOutcome> items) {
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
                boundingBox.classes.put("class_" + j, i.getDouble("class_" + j));
            }
            addBBox(boundingBox, formattedResults);
        }
        return formattedResults;
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
    private static ItemCollection<QueryOutcome> getBBoxItems(ImageQueryRequest request, Table bboxTable,
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
}

/*
CAPTAINS LOG:
Can't query on multiple indices at once.
Redo GSIs: have date/deployment/trap as primary, and userID as sort.
Query priority:
trap
date
deployment
Then filter based on all parameters.

TO DO:
-Move this back to the main project
-Make it not break
-Call from lambda
-Call from API
-Call from website
-Write unit tests
 */