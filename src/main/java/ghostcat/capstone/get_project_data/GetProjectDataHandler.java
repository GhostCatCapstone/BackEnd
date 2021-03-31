package ghostcat.capstone.get_project_data;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import ghostcat.capstone.get_camera_traps.GetCameraTrapsResponse;
import ghostcat.capstone.holders.CameraTrap;
import ghostcat.capstone.holders.Factory;

import java.util.ArrayList;

public class GetProjectDataHandler {
    static GetProjectDataDAO dao;

    public static void main(String[] args) {
        dao = Factory.getProjectDataDAO;
        GetProjectDataResponse response = new GetProjectDataResponse();
        GetProjectDataRequest request = new GetProjectDataRequest();
        request.projectID = "projectID";
        request.userID = "researcherID";
        response = getProjectData(request);
        return;
    }

    public GetProjectDataResponse handleRequest(GetProjectDataRequest request, Context context) {
        dao = Factory.getProjectDataDAO;
        GetProjectDataResponse response = new GetProjectDataResponse();

        response = errorCheckRequest(request);
        if (!response.success) return response;

        response = getProjectData(request);

        return response;
    }

    public static GetProjectDataResponse errorCheckRequest(GetProjectDataRequest request) {
        GetProjectDataResponse response = new GetProjectDataResponse();
        response.success = true;

        if (request.userID == null) {
            response.success = false;
            response.errorMsg = "Null userID";
        }
        return response;
    }
    


    public static GetProjectDataResponse getProjectData(GetProjectDataRequest request) {
        GetProjectDataResponse response = new GetProjectDataResponse();

        ArrayList<Item> results = dao.queryProjectDataOnUserIDAndProjectID(request);
        if (results.size() == 0) {
            response.success = false;
            response.errorMsg = "Invalid projectID: " + request.projectID;
        } else {
            Item item = results.get(0);
            response.cameraTraps = item.getBoolean("uses_camera_traps");
            int numClasses = item.getInt("num_classes");
            for (int i = 1; i <= numClasses; ++i) {
                response.classes.add(item.getString("class_" + String.valueOf(i)));
            }
        }
        return response;
    }
}
