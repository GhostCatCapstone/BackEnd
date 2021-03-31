package ghostcat.capstone.get_camera_traps;


import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import ghostcat.capstone.holders.CameraTrap;
import ghostcat.capstone.holders.Factory;

import java.util.ArrayList;

public class GetCameraTrapsHandler {
    static GetCameraTrapsDAO dao;


    public static void main(String[] args) {
        dao = Factory.getCameraTrapsDAO;
        GetCameraTrapsResponse response = new GetCameraTrapsResponse();
        GetCameraTrapsRequest request = new GetCameraTrapsRequest();
        request.projectID = "projectID";

        response = getCameraTraps(request);
        return;
    }

    /**
     * Method invoked by the lambda. Queries CameraTraps table to get all camera trap data associated with a projectID.
     *
     * @param request Object that contains query parameters.
     * @return Response object containing either the CameraTrap objects, or an error message.
     */
    public GetCameraTrapsResponse handleRequest(GetCameraTrapsRequest request, Context context) {
        dao = Factory.getCameraTrapsDAO;
        GetCameraTrapsResponse response = new GetCameraTrapsResponse();

        response = errorCheckRequest(request);
        if (!response.success) return response;

        response = getCameraTraps(request);

        return response;
    }

    /**
     * Ensures that a request is valid.
     *
     * @param request Object that contains request parameters.
     * @return Response object that will contain error message if request is invalid.
     */
    public static GetCameraTrapsResponse errorCheckRequest(GetCameraTrapsRequest request) {
        GetCameraTrapsResponse response = new GetCameraTrapsResponse();
        response.success = true;

        if (request.userID == null) {
            response.success = false;
            response.errorMsg = "Null userID";
        }

        return response;
    }



    /**
     * Returns list of CameraTrap objects associated with the projectID in the given request.
     *
     * @param request Request containing projectID
     * @return Response object containing either a list of CameraTrap objects, or an error message.
     */
    public static GetCameraTrapsResponse getCameraTraps(GetCameraTrapsRequest request) {
        GetCameraTrapsResponse response = new GetCameraTrapsResponse();
        ArrayList<Item> results = dao.queryCameraTrapsOnProjectID(request.projectID);
        if (results.size() == 0) {
            response.success = false;
            response.errorMsg = "Invalid projectID: " + request.projectID;
        } else {
            for (Item i : results) {
                response.cameraTraps.add(new CameraTrap(
                        i.getString("CameraTrapID"),
                        i.getString("ProjectID"),
                        i.getDouble("camera_lat"),
                        i.getDouble("camera_lng")
                ));
            }
        }
        return response;
    }
}
