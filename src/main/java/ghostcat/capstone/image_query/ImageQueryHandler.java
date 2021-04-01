package ghostcat.capstone.image_query;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.ClassNameValue;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class ImageQueryHandler implements RequestHandler<ImageQueryRequest, ImageQueryResponse> {

    static ImageQueryDAO dao;

    public static void main(String[] args) {
        ImageQueryRequest request = new ImageQueryRequest();
        request.userID = "researcherID";
        request.projectID = "projectID";
        doQuery(request);

    }

    /**
     * Method invoked by the lambda. Queries DynamoDB, and filters the results based on the request.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains query results, or an error message if something went wrong.
     */
    public ImageQueryResponse handleRequest(ImageQueryRequest request, Context context) {
        return doQuery(request);
    }

    public static ImageQueryResponse doQuery(ImageQueryRequest request) {
        ImageQueryResponse response = new ImageQueryResponse();
        dao = Factory.imageQueryDAO;
        HashMap<String, String> classNames = new HashMap<>();
        int numClasses = 0;

        response = errorCheckRequest(request);
        if (!response.success) return response;

        numClasses = getUserInfo(request, classNames, response);
        if (!response.success) return response;

        ArrayList<Image> dbResults = queryBBoxDB(request, classNames, numClasses);
        ArrayList<Image> filteredResults = filterResultsOnMetadata(dbResults, request);
        filteredResults = filterResultsOnClass(filteredResults, request, classNames);

        response.images = filteredResults;

        Collections.sort(response.images);

        response.success = true;
        response.errorMsg = "Request: " + request.str();

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
            if (request.cameraTraps != null && request.cameraTraps.size() > 0) {
                if (!request.cameraTraps.contains(i.cameraTrap)) addImage = false;
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

        //remove all images that have no bounding boxes
        Iterator<Image> iter = imgResults.iterator();
        while(iter.hasNext()){
            if(iter.next().boundingBoxes.size() == 0)
                iter.remove();
        }

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
    public static int getUserInfo(ImageQueryRequest request, HashMap<String, String> classNames, ImageQueryResponse response) {


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
     * Queries the BoundingBoxes and Images DynamoDB tables and returns a formatted list of the results.
     *
     * @param request    Object that contains query parameters.
     * @param numClasses The number of classes in a user's data set.
     * @return An ArrayList of image objects that contains the results of the query.
     */
    public static ArrayList<Image> queryBBoxDB(ImageQueryRequest request, HashMap<String, String> classNames,
                                        int numClasses) {
        ArrayList<Item> bboxResults = new ArrayList<>();
        ArrayList<Item> imageResults = new ArrayList<>();

        ArrayList<Item>[] results = getDBItems(request);

        ArrayList<Image> formattedBBoxResults = formatBBoxResults(numClasses, classNames, results[0]);
        ArrayList<Image> formattedImgResults = formatImageResults(numClasses, classNames, results[1]);

        ArrayList<Image> combinedList = combineImageResultsWithBBoxResults(formattedImgResults, formattedBBoxResults);
        return combinedList;
    }

    /**
     * Constructs a query based on the request, and then uses that query on the BoundingBox table.
     * The BoundingBoxes table has indexes that can be used to add request parameters to the query.
     * The database can only be queried on one index at a time, so priority is given to the request parameters that might yield
     * the most specific results.
     * Identical queries are also performed on the Images table, which holds all information about images.
     * The query results from the Images table are checked against the results from the BoundingBoxes table.
     * If an Image result is not found in the list of BoundingBoxes results, it is added to the list.
     * This is because we want the user to be able to access images that have no bounding boxes associated with them.
     *
     * @param request Object that contains query parameters.
     * @return The outcome of the database query.
     */
    private static ArrayList<Item>[] getDBItems(ImageQueryRequest request) {
        ArrayList<Item>[] results = new ArrayList[2];
        if (request.cameraTraps != null && request.cameraTraps.size() > 0) {
            results[0] = dao.queryBBoxOnCameraTraps(request);
            results[1] = dao.queryImagesOnCameraTrap(request);
        }
        else if (request.minDate != null) {
            results[0] = dao.queryBBoxOnMinDate(request);
            results[1] = dao.queryImagesOnMinDate(request);
        }
        else if (request.maxDate != null)  {
            results[0] = dao.queryBBoxOnMaxDate(request);
            results[1] = dao.queryImagesOnMaxDate(request);
        }
        else {
            results[0] = dao.queryBBoxOnUserID(request);
            results[1] = dao.queryImagesOnUserID(request);
        }
        return results;
    }

    private static ArrayList<Image> combineImageResultsWithBBoxResults(ArrayList<Image> imgResults, ArrayList<Image> bboxResults) {
        ArrayList<Image> combinedList = new ArrayList<>(bboxResults);
        for (Image i : imgResults) {
            boolean addImg = true;
            for (Image j : bboxResults) {
                if (i.id.equals(j.id)) addImg = false;
            }
            if (addImg) combinedList.add(i);
        }
        return combinedList;
    }

    /**
     * Formats a query result into a list of image objects. Each image object contains a list of bounding boxes.
     *
     * @param numClasses The number of classes in a user's data set.
     * @param dbResults The results of the database query.
     * @return An ArrayList of image objects that contains the results of the query.
     */
    private static ArrayList<Image> formatBBoxResults(int numClasses, HashMap<String, String> classNames,
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
     * Formats a query result into a list of image objects. Each image object contains a list of bounding boxes.
     *
     * @param numClasses The number of classes in a user's data set.
     * @param dbResults The results of the database query.
     * @return An ArrayList of image objects that contains the results of the query.
     */
    private static ArrayList<Image> formatImageResults(int numClasses, HashMap<String, String> classNames,
                                                      ArrayList<Item> dbResults) {
        ArrayList<Image> formattedResults = new ArrayList<>();
        for (Item i : dbResults) {
            if (!imgInList(i.getString("ImageID"), formattedResults)) {
                Image image = new Image(
                        i.getString("ImageID"),
                        i.getInt("image_height"),
                        i.getInt("image_width"),
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
        for (Image i : images) {
            if (id.equals(i.id)) return true;
        }
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