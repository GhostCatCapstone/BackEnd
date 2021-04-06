package ghostcat.capstone.get_project_data;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import ghostcat.capstone.get_camera_traps.GetCameraTrapsRequest;
import ghostcat.capstone.get_camera_traps.GetCameraTrapsResponse;
import ghostcat.capstone.holders.CameraTrap;
import ghostcat.capstone.holders.Factory;
import ghostcat.capstone.holders.Project;

import java.util.ArrayList;

public class GetProjectDataHandler {
    static GetProjectDataDAO dao;

    public static void main(String[] args) {
        dao = Factory.getProjectDataDAO;
        GetProjectDataResponse response = new GetProjectDataResponse();
        GetProjectDataRequest request = new GetProjectDataRequest();
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

        ArrayList<Item> results = dao.queryProjectDataOnUserID(request.userID);
        if (results.size() == 0) {
            response.success = false;
            response.errorMsg = "No project data found for userID: " + request.userID;
        } else {
            for (Item item : results) {
                Project p = new Project();
                int numClasses = item.getInt("num_classes");
                for (int i = 1; i <= numClasses; ++i) {
                    p.classes.add(item.getString("class_" + String.valueOf(i)));
                }
                p.projectID = item.getString("ProjectID");
                p.cameraTraps = getCameraTraps(p.projectID);
                response.projects.add(p);
            }
        }
        return response;
    }


    /**
     * Returns list of CameraTrap objects associated with the projectID in the given request.
     *
     * @param projectID String containing projectID
     * @return Response object containing either a list of CameraTrap objects, or an error message.
     */
    public static ArrayList<CameraTrap> getCameraTraps(String projectID) {
        ArrayList<Item> results = dao.queryCameraTrapsOnProjectID(projectID);
        ArrayList<CameraTrap> traps = new ArrayList<>();
        for (Item i : results) {
            traps.add(new CameraTrap(
                    i.getString("CameraTrapID"),
                    i.getString("ProjectID"),
                    i.getDouble("camera_lat"),
                    i.getDouble("camera_lng")
            ));
        }
        return traps;
    }
}

