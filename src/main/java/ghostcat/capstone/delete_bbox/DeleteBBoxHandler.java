package ghostcat.capstone.delete_bbox;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import ghostcat.capstone.holders.Factory;

import java.util.ArrayList;
import java.util.HashMap;

public class DeleteBBoxHandler {
    static DeleteBBoxDAO dao;

    /**
     * Method invoked by the lambda. Deletes bounding box in BoundingBoxes table.
     *
     * @param request Object that contains query parameters.
     * @return Object that contains success/fail boolean and an optional error message.
     */
    public DeleteBBoxResponse handleRequest(DeleteBBoxRequest request, Context context) {
        dao = Factory.deleteBBoxDAO;
        DeleteBBoxResponse response = new DeleteBBoxResponse();
        HashMap<String, String> classNames = new HashMap<>();

        if (context != null) {
            LambdaLogger logger = context.getLogger();
            logger.log("Called ImageQueryHandler");
        }

        response = errorCheckRequest(request);
        if (!response.success) return response;

        response = deleteBBox(request);

        return response;
    }

    /**
     * Ensures that a request is valid.
     *
     * @param request Object that contains request parameters.
     * @return Response object that will contain error message if request is invalid.
     */
    public static DeleteBBoxResponse errorCheckRequest(DeleteBBoxRequest request) {
        DeleteBBoxResponse response = new DeleteBBoxResponse();
        response.success = true;

        if (request.userID == null) {
            response.success = false;
            response.errorMsg = "Null userID";
        }

        if (request.bboxID == null) {
            response.success = false;
            response.errorMsg = "Null bboxID";
        }

        return response;
    }


    /**
     * Deletes bounding box associated with given ID.
     * @param request Request containing ID of bounding box
     * @return Response object with success/failure boolean and optional error message.
     */
    public static DeleteBBoxResponse deleteBBox(DeleteBBoxRequest request) {
        DeleteBBoxResponse response = new DeleteBBoxResponse();
        ArrayList<Item> results = dao.queryBBoxOnBBoxID(request.userID, request.bboxID);
        if (results.size() == 0) {
            response.success = false;
            response.errorMsg = "Invalid boundingBox ID: " + request.bboxID;
            return response;
        }
        response.success = dao.deleteBBox(request);
        return response;
    }
}
