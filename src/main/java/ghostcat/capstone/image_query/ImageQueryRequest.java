package ghostcat.capstone.image_query;

import java.util.HashMap;
import java.util.HashSet;

public class ImageQueryRequest {
    public String userID;
    public String authToken;
    public Long minDate;
    public Long maxDate;
    public String deployment;
    public String cameraTrap;
    public HashMap<String, Double> classes = new HashMap<>();
}
