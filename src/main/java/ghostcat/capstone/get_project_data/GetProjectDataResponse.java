package ghostcat.capstone.get_project_data;

import ghostcat.capstone.holders.Project;

import java.util.ArrayList;

public class GetProjectDataResponse {
    public ArrayList<Project> projects = new ArrayList<>();
    public boolean success = true;
    public String errorMsg = "";

}
