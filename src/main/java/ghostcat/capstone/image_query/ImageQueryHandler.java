package ghostcat.capstone.image_query;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ghostcat.capstone.holders.BoundingBox;
import ghostcat.capstone.holders.ClassValue;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ImageQueryHandler implements RequestHandler<ImageQueryRequest, ImageQueryResponse> {

    /**
     * Method invoked by the lambda. Queries DynamoDB, and filters the results based on the request.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains query results, or an error message if something went wrong.
     */
    public ImageQueryResponse handleRequest(ImageQueryRequest request, Context context) {
        ImageQueryResponse response = new ImageQueryResponse();
        HashMap<String, String> classNames = new HashMap<>();
        int numClasses = 0;
        ImageQueryDAO imageQueryDAO = Factory.imageQueryDAO;

        if (context != null) {
            LambdaLogger logger = context.getLogger();
            logger.log("Called ImageQueryHandler");
        }

        response = errorCheckRequest(request);
        if (!response.success) return response;

        numClasses = imageQueryDAO.getUserInfo(request, classNames, response);
        if (!response.success) return response;

        ArrayList<Image> imgResults = imageQueryDAO.queryBBoxDB(request, classNames, numClasses);
        ArrayList<Image> filteredResults = filterResultsOnMetadata(imgResults, request);
        filteredResults = filterResultsOnClass(filteredResults, request, classNames);

        response.images = filteredResults;
        response.success = true;

        return response;
    }

    /**
     * Ensures that a request is valid.
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
                        " and maxDate = " + request.maxDate + "." ;
            }
        }
        if (request.classes != null) {
            for (ClassValue c : request.classes) {
                double confidenceValue = c.classValue;
                if (confidenceValue > 1 || confidenceValue < 0) {
                    response.success = false;
                    response.errorMsg = "Invalid confidence value: " + c.className + " = " + confidenceValue ;
                }
            }
        }

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
        if (request.classes == null || request.classes.size() == 0) return imgResults;
        for (Image i : imgResults) {
            boolean addImg = true;
            for (BoundingBox b : i.boundingBoxes) {
                for (ClassValue c : request.classes) {
                    Double classVal = b.classes.get(c.className);
                    Double classThreshold = c.classValue;
                    if (classVal < classThreshold) addImg = false;
                }
            }
            if (addImg) filteredImages.add(i);
        }
        return filteredImages;
    }
}