package ghostcat.capstone.update_bbox;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ghostcat.capstone.holders.Factory;

import java.util.ArrayList;
import java.util.HashMap;

public class UpdateBBoxHandler implements RequestHandler<UpdateBBoxRequest, UpdateBBoxResponse> {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String PROJECT_TABLE = "ProjectData";
    static UpdateBBoxDAO dao;


    public static void main(String[] args) {
        dao = Factory.updateBBoxDAO;

        UpdateBBoxRequest request = new UpdateBBoxRequest();
        request.userID = "researcherID";
        request.authToken = "";
        request.bboxID = "021a821f-67b6-360d-813b-ed02e6a73f1d";
        request.correctClassName = "Sheep";

        HashMap<String, String> classNames = new HashMap<>();
        classNames.put("Cow", "class_1");
        classNames.put("Mule Deer", "class_2");
        classNames.put("Sheep", "class_3");
        classNames.put("Other", "class_4");

        UpdateBBoxResponse response = updateBBox(request, classNames);
        return;

    }
    /**
     * Method invoked by the lambda. Updates bounding box in BoundingBoxes table.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains success/fail boolean and an optional error message.
     */
    public UpdateBBoxResponse handleRequest(UpdateBBoxRequest request, Context context) {
        dao = Factory.updateBBoxDAO;
        UpdateBBoxResponse response = new UpdateBBoxResponse();
        HashMap<String, String> classNames = new HashMap<>();

        if (context != null) {
            LambdaLogger logger = context.getLogger();
            logger.log("Called ImageQueryHandler");
        }

        response = errorCheckRequest(request);
        if (!response.success) return response;

        getUserInfo(request, classNames, response);
        if (!response.success) return response;

        response = updateBBox(request, classNames);

        return response;
    }

    /**
     * Ensures that a request is valid.
     *
     * @param request Object that contains request parameters.
     * @return Response object that will contain error message if request is invalid.
     */
    public static UpdateBBoxResponse errorCheckRequest(UpdateBBoxRequest request) {
        UpdateBBoxResponse response = new UpdateBBoxResponse();
        response.success = true;

        if (request.userID == null) {
            response.success = false;
            response.errorMsg = "Null userID";
        }
        if (request.authToken == null) {
            response.success = false;
            response.errorMsg = "Null authToken";
        }
        if (!validToken(request.authToken, request.userID)) {
            response.errorMsg = "Invalid authToken: " + request.authToken;
            response.success = false;
        }
        if (request.bboxID == null) {
            response.success = false;
            response.errorMsg = "Null bboxID";
        }

        return response;
    }

    public static boolean validToken(String authToken, String userID) {
        //return TokenAuthentication.authenticateToken(authToken, userID);
        return true;
    }

    /**
     * Retrieves user data from the ProjectData DynamoDB table. Specifically looks up the number of classes for a user's
     * data set, and the names of those classes.
     * The method returns the number of classes, and changes the classNames parameter via pass-by-reference. This is so
     * the handleRequest() method will wait until this method returns before it continues.
     *
     * @param request    Object that contains request parameters.
     * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
     *                   *                   each label's location in the BoundingBox database (e.g. "Mule Deer", "class_1"). This is used to
     *                   *                   look up what each label's value is for a bounding box.
     * @param response   Response object that will contain error message if an error occurs.
     * @return The number of classes associated with a user's data set.
     */
    public static void getUserInfo(UpdateBBoxRequest request, HashMap<String, String> classNames, UpdateBBoxResponse response) {

        ArrayList<Item> results = dao.queryProjectDataOnUserIDAndProjectID(request);

        //Error handling-  query returns no items, check if userID is invalid or if deployment is invalid.
        if (results.size() == 0) {
            response.success = false;

            //Query only on userID. If any items are returned, then the userID is valid and the deployment is invalid.
            ArrayList<Item> usrResults = dao.queryProjectDataOnUserID(request);
            if (results.size() == 0) {
                response.errorMsg = "Invalid userID: " + request.userID;
            } else {
                response.errorMsg = "Invalid projectID: " + request.projectID;
            }
            return;
        }

        //Format classes as HashMap
        int numClasses = 0;
        for (Item item : results) {
            numClasses = item.getInt("num_classes");
            for (int i = 1; i <= numClasses; ++i) {
                classNames.put(item.getString("class_" + i), "class_" + i);
            }
        }

        //Error handling- if a request contains a class name that isn't in classNames, then the request is invalid.
        if (!classNames.containsKey(request.correctClassName)) {
            response.success = false;
            response.errorMsg = "Invalid class name: " + request.correctClassName;
        }

    }


    /**
     * Updates item in table with "1" as the class value for the class in the request, and "0" for all others.
     *
     * @param request    Object that contains request parameters.
     * @param classNames Hash Map that holds relationship between a class index and a class name ("Mule Deer, "class_1")
     * @return Response object with success/failure boolean and optional error message.
     */
    public static UpdateBBoxResponse updateBBox(UpdateBBoxRequest request, HashMap<String, String> classNames) {
        UpdateBBoxResponse response = new UpdateBBoxResponse();
        ArrayList<Item> results = dao.queryBBoxOnBBoxID(request.userID, request.bboxID);
        if (results.size() == 0) {
            response.success = false;
            response.errorMsg = "Invalid boundingBox ID: " + request.bboxID;
            return response;
        }
        response.success = dao.setCorrectValueForBBox(request, classNames);
        return response;
    }
}
