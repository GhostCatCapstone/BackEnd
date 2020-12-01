package ghostcat.capstone.image_query;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.ClassNameValue;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageQueryHandler implements RequestHandler<ImageQueryRequest, ImageQueryResponse> {

    ImageQueryDAO dao;

    /**
     * Method invoked by the lambda. Queries DynamoDB, and filters the results based on the request.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains query results, or an error message if something went wrong.
     */
    public ImageQueryResponse handleRequest(ImageQueryRequest request, Context context) {
        ImageQueryResponse response = new ImageQueryResponse();
        dao = Factory.imageQueryDAO;
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

        ArrayList<Image> imgResults = queryBBoxDB(request, classNames, numClasses);
        ArrayList<Image> filteredResults = filterResultsOnMetadata(imgResults, request);
        filteredResults = filterResultsOnClass(filteredResults, request, classNames);

        response.images = filteredResults;
        response.success = true;

        return response;
    }

    /**
     * Ensures that a request is valid.
     *
     * @param request Object that contains query parameters.
     * @return Response object that will contain error message if request is invalid.
     */
    public static ImageQueryResponse errorCheckRequest(ImageQueryRequest request) {
        ImageQueryResponse response = new ImageQueryResponse();
        response.success = true;

        if (request.userID == null) {
            response.success = false;
            response.errorMsg = "Null userID";
        }
        if (request.projectID == null) {
            response.success = false;
            response.errorMsg = "Null projectID";
        }
        if (request.authToken == null) {
            response.success = false;
            response.errorMsg = "Null authToken";
        }
        if (!validToken(request.authToken, request.userID)) {
            response.errorMsg = "Invalid authToken: " + request.authToken;
            response.success = false;
        }
        if (request.minDate != null && request.maxDate != null) {
            if (request.maxDate <= request.minDate) {
                response.success = false;
                response.errorMsg = "Invalid date range: minDate = " + request.minDate +
                        " and maxDate = " + request.maxDate + ".";
            }
        }
        if (request.classes != null) {
            for (ClassNameValue c : request.classes) {
                double confidenceValue = c.classValue;
                if (confidenceValue > 1 || confidenceValue < 0) {
                    response.success = false;
                    response.errorMsg = "Invalid confidence value: " + c.className + " = " + confidenceValue;
                }
            }
        }

        return response;
    }

    /**
     * Determines the user's authentication status.
     *
     * @param authToken String passed from the front end that represents user authentication
     * @param userID    User's ID
     * @return True if user is authenticated, false if user is not authenticated.
     */
    public static boolean validToken(String authToken, String userID) {
        return true;
    }

    /**
     * Filters a list of images by the image metadata parameters provided by the request body.
     *
     * @param imgResults ArrayList of image objects that resulted from the DynamoDB query.
     * @param request    Object that contains query parameters.
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
     *
     * @param imgResults ArrayList of image objects that resulted from the DynamoDB query.
     * @param request    Object that contains query parameters.
     * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
     *                   each label's location in the BoundingBox database (e.g. "Cow" -> "class_1"). This is used to
     *                   look up what each label's value is for a bounding box.
     * @return
     */
    public static ArrayList<Image> filterResultsOnClass(ArrayList<Image> imgResults, ImageQueryRequest request,
                                                        HashMap<String, String> classNames) {
        ArrayList<Image> filteredImages = new ArrayList<>();
        if (request.classes == null || request.classes.size() == 0) return imgResults;
        for (Image i : imgResults) {
            boolean addImg = true;
            for (BoundingBox b : i.boundingBoxes) {
                for (ClassNameValue c : request.classes) {
                    Double classVal = b.classes.get(c.className);
                    Double classThreshold = c.classValue;
                    if (classVal < classThreshold) addImg = false;
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
     *
     * @param request    Object that contains query parameters.
     * @param classNames HashMap<String, String> object that holds the association between a user's set of labels and
     *                   *                   each label's location in the BoundingBox database (e.g. "Cow" -> "class_1"). This is used to
     *                   *                   look up what each label's value is for a bounding box.
     * @param response   Response object that will contain error message if an error occurs.
     * @return The number of classes associated with a user's data set.
     */
    public int getUserInfo(ImageQueryRequest request, HashMap<String, String> classNames, ImageQueryResponse response) {


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
            return 0;
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
        for (ClassNameValue c : request.classes) {
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
     *
     * @param request    Object that contains query parameters.
     * @param numClasses The number of classes in a user's data set.
     * @return An ArrayList of image objects that contains the results of the query.
     */
    public ArrayList<Image> queryBBoxDB(ImageQueryRequest request, HashMap<String, String> classNames,
                                        int numClasses) {

        ArrayList<Item> dbResults = getBBoxItems(request);
        ArrayList<Image> formattedResults = formatQueryResults(numClasses, classNames, dbResults);

        return formattedResults;
    }

    /**
     * Constructs a query based on the request, and then uses that query on the BoundingBox table.
     * The BoundingBox table has indexes that can be used to add request parameters to the query.
     * The database can only be queried on one index at a time, so priority is given to the request parameters that might yield
     * the most specific results.
     *
     * @param request         Object that contains query parameters.
     * @return The outcome of the database query.
     */
    private ArrayList<Item> getBBoxItems(ImageQueryRequest request) {

        if (request.cameraTrap != null) return dao.queryBBoxOnCameraTrap(request);
        else if (request.minDate != null) return dao.queryBBoxOnMinDate(request);
        else if (request.maxDate != null) return dao.queryBBoxOnMaxDate(request);
        else return dao.queryBBoxOnUserID(request);
    }

    /**
     * Formats a query result into a list of image objects. Each image object contains a list of bounding boxes.
     *
     * @param numClasses The number of classes in a user's data set.
     * @param dbResults The results of the database query.
     * @return An ArrayList of image objects that contains the results of the query.
     */
    private static ArrayList<Image> formatQueryResults(int numClasses, HashMap<String, String> classNames,
                                                       ArrayList<Item> dbResults) {
        ArrayList<Image> formattedResults = new ArrayList<>();
        for (Item i : dbResults) {
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
     *
     * @param id     Image id being checked.
     * @param images ArrayList of images to check the image against
     * @return True if image is in the list, and false if it isn't.
     */
    public static boolean imgInList(String id, ArrayList<Image> images) {
        for (Image i : images) if (id.equals(i.id)) return true;
        return false;
    }

    /**
     * Adds BoundingBox object to the Image object with the correct ID.
     *
     * @param bbox   BoundingBox object to be added.
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

/*
FIXME: Change bbox table to be projectID -> bboxID

3. add authentication package
4. add change bbox data package
5. redo dynamodb table
=======
}

/*
TO DO:
Write DAOs and unit tests for authentication and update bbox
Fix problems with update bbox

>>>>>>> Created UpdateBBox and Authentication classes
 */