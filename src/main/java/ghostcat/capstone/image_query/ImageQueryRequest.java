package ghostcat.capstone.image_query;

import ghostcat.capstone.holders.ClassNameValue;

import java.util.ArrayList;

public class ImageQueryRequest {
    public String userID;
    public String authToken;
    public String projectID;
    public Long minDate;
    public Long maxDate;
    public String deployment;
    public String cameraTrap;
    public ArrayList<ClassNameValue> classes = new ArrayList<>();
}
