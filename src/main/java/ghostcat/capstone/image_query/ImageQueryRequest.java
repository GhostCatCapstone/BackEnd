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
    public ArrayList<String> cameraTraps = new ArrayList<>();
    public ArrayList<ClassNameValue> classes = new ArrayList<>();

    public String str() {

        String s = "";
        s += "userID: ";
        if (userID != null) s += userID; else s += "null";
        s += " authToken: ";
        if (authToken != null) s += authToken; else s += "null";
        s += " projectID: ";
        if (projectID != null) s += projectID; else s += "null";
        s += " minDate: ";
        if (minDate != null) s += minDate; else s += "null";
        s += " maxDate: ";
        if (maxDate != null) s += maxDate; else s += "null";
        s += " deployment: ";
        if (deployment != null) s += deployment; else s += "null";
        s += " cameraTraps: ";
        if (cameraTraps != null) s += cameraTraps.toString(); else s += "[]";
        s += " classes: ";
        if (classes != null)  {
            s += "[";
            for (ClassNameValue c : classes) {
                s += c.className + " > " + c.classValue ;
            }
            s += "]";
        } else s += "[]";

        return s;
    }
}
