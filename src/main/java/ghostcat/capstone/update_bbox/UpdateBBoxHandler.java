package ghostcat.capstone.update_bbox;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ghostcat.capstone.authentication.TokenAuthentication;
import ghostcat.capstone.holders.ClassNameValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UpdateBBoxHandler implements RequestHandler<UpdateBBoxRequest, UpdateBBoxResponse> {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static String BBOX_TABLE = "BoundingBoxes";
    static String PROJECT_TABLE = "ProjectData";

    /**
     * Method invoked by the lambda. Updates bounding box in BoundingBoxes table.
     * @param request Object that contains query parameters.
     * @return Object that contains success/fail boolean and an optional error message.
     */
    public UpdateBBoxResponse handleRequest(UpdateBBoxRequest request, Context context) {
        UpdateBBoxResponse response = new UpdateBBoxResponse();
        HashMap<String, String> classNames = new HashMap<>();
        int numClasses = 0;

        if (context != null) {
            LambdaLogger logger = context.getLogger();
            logger.log("Called ImageQueryHandler");
        }

        response = errorCheckRequest(request);
        if (!response.success) return response;

        numClasses = getUserInfo(request, classNames, response);
        if (!response.success) return response;

        response = updateBBox(request, classNames);

        return response;
    }

    /**
     * Ensures that a request is valid.
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
        if (request.classNameValue.classValue > 1) {
            response.errorMsg = "Invalid value: " + request.classNameValue.classValue + " must be < 1";
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
     * @param request Object that contains request parameters.
     * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
     *      *                   each label's location in the BoundingBox database (e.g. "Mule Deer", "class_1"). This is used to
     *      *                   look up what each label's value is for a bounding box.
     * @param response Response object that will contain error message if an error occurs.
     * @return The number of classes associated with a user's data set.
     */
    public static int getUserInfo(UpdateBBoxRequest request, HashMap<String, String> classNames, UpdateBBoxResponse response) {

        Table projectTable = dynamoDB.getTable(PROJECT_TABLE);

        //Query UserData table
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("UserID = :v_userID and ProjectID = :v_projectID")
                .withValueMap(new ValueMap()
                        .withString(":v_userID", request.userID)
                        .withString(":v_projectID", request.projectID)
                );

        ItemCollection<QueryOutcome> items = projectTable.query(spec);
        Iterator<Item> iterator = items.iterator();

        //Error handling-  query returns no items, check if userID is invalid or if deployment is invalid.
        if (!iterator.hasNext()) {
            response.success = false;

            //Query only on userID. If any items are returned, then the userID is valid and the deployment is invalid.
            spec = new QuerySpec().withKeyConditionExpression("UserID = :v_userID")
                    .withValueMap(new ValueMap().withString(":v_userID", request.userID));
            items = projectTable.query(spec);
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
            if (!classNames.containsKey(request.classNameValue.className)) {
                response.success = false;
                response.errorMsg = "Invalid class name: " + request.classNameValue.className;
                return 0;
            }

        return numClasses;
    }


    /**
     * Updates item in table.
     * @param request Object that contains request parameters.
     * @param classNames  Hash Map that holds relationship between a class index and a class name ("Mule Deer, "class_1")
     * @return Response object with success/failure boolean and optional error message.
     */
    public static UpdateBBoxResponse updateBBox(UpdateBBoxRequest request, HashMap<String, String> classNames) {
        UpdateBBoxResponse response = new UpdateBBoxResponse();
        Table bboxTable = dynamoDB.getTable(BBOX_TABLE);
;
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().
                withPrimaryKey("UserID", request.userID, "BBoxID", request.bboxID)
                .withUpdateExpression("set " + classNames.get(request.classNameValue.className) + " = :_v")
                .withValueMap(new ValueMap().withNumber(":_v", request.classNameValue.classValue));

        UpdateItemOutcome outcome = bboxTable.updateItem(updateItemSpec);

        return response;
    }

    /**
     *
     * @param index number in list
     * @param classNameValues list of class names ("Mule Deer") associated with class values (.998)
     * @param classNames  Hash Map that holds relationship between a class index and a class name ("Mule Deer, "class_1")
     * @return value associated with the class name associated with the given class index.
     */
    public static Double getValueFromIndex(String index, ArrayList<ClassNameValue> classNameValues, HashMap<String, String> classNames) {
        for (String s : classNames.keySet()) {
            if (classNames.get(s).equals(index)) {
                for (ClassNameValue c : classNameValues) {
                    if (c.className.equals(s)) return c.classValue;
                }
            }
        }
        return null;
    }
}

/*
CAPTAINS LOG:
Write unit tests
Fix what breaks
Submit pull request
Create lambda and API gateway on AWS
 */