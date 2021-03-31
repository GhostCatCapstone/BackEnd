package ghostcat.capstone.get_project_data;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
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
                p.usesCameraTraps = item.getBoolean("uses_camera_traps");
                int numClasses = item.getInt("num_classes");
                for (int i = 1; i <= numClasses; ++i) {
                    p.classes.add(item.getString("class_" + String.valueOf(i)));
                }
                p.projectID = item.getString("ProjectID");
                response.projects.add(p);
            }
        }
        return response;
    }
}
