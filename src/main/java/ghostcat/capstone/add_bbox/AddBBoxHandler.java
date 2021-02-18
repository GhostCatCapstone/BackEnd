package ghostcat.capstone.add_bbox;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AddBBoxHandler {
    static AddBBoxDAO dao;


    public static void main(String[] args) {
        dao = Factory.addBBoxDAO;

        AddBBoxRequest request = new AddBBoxRequest();
        request.imgId = "1081301";
        request.className = "Cow";
        request.authToken = "";
        request.height = .3;
        request.width = .3;
        request.projectID = "projectID";
        request.userID = "researcherID";
        request.xVal = .1;
        request.yVal = .1;


        HashMap<String, String> classNames = new HashMap<>();
        classNames.put("Cow", "class_1");
        classNames.put("Mule Deer", "class_2");
        classNames.put("Sheep", "class_3");
        classNames.put("Other", "class_4");

        AddBBoxResponse response = addBBox(request, classNames);
        return;

    }
    /**
     * Method invoked by the lambda. Updates bounding box in BoundingBoxes table.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains success/fail boolean and an optional error message.
     */
    public AddBBoxResponse handleRequest(AddBBoxRequest request, Context context) {
        dao = Factory.addBBoxDAO;

        AddBBoxResponse response = new AddBBoxResponse();
        HashMap<String, String> classNames = new HashMap<>();

        if (context != null) {
            LambdaLogger logger = context.getLogger();
            logger.log("Called ImageQueryHandler");
        }

        response = errorCheckRequest(request);
        if (!response.success) return response;

        getUserInfo(request, classNames, response);
        if (!response.success) return response;

        response = addBBox(request, classNames);

        return response;
    }

    /**
     * Ensures that a request is valid.
     *
     * @param request Object that contains request parameters.
     * @return Response object that will contain error message if request is invalid.
     */
    public static AddBBoxResponse errorCheckRequest(AddBBoxRequest request) {
        AddBBoxResponse response = new AddBBoxResponse();
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
    public static void getUserInfo(AddBBoxRequest request, HashMap<String, String> classNames, AddBBoxResponse response) {

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
        if (!classNames.containsKey(request.className)) {
            response.success = false;
            response.errorMsg = "Invalid class name: " + request.className;
        }

    }

    /**
     * Adds bounding box from the request object to the database. Retrieves the image associated with the
     * request's imgID, creates a UUID for the bounding box, and passes all of that to the DAO. The DAO
     * adds the data to the database.
     * @param request Request object containing all information about the bounding box.
     * @param classNames Hash Map that holds relationship between a class name and a class index ("Mule Deer, "class_1")
     * @return Response object indicating the success of the operation.
     */
    public static AddBBoxResponse addBBox(AddBBoxRequest request, HashMap<String, String> classNames) {
        AddBBoxResponse response = new AddBBoxResponse();
        Image bboxImage = dao.queryBBoxOnImageID(request.imgId, request.userID);
        if (bboxImage == null) {
            response.errorMsg = "Invalid imageID: " + request.imgId;
            response.success = false;
            return response;
        }
        response.success = dao.addBBox(request, classNames, bboxImage, UUID.randomUUID().toString());
        if (!response.success) response.errorMsg = "Error adding bounding box to database.";
        return response;
    }
}
