package ghostcat.capstone.holders;

import java.util.HashMap;

public class BoundingBox {
    public String id;
    public String imgId;
    public double xVal;
    public double yVal;
    public double width;
    public double height;
    public HashMap<String, Double> classes = new HashMap<>();


    public BoundingBox(String id, String imgId, double xVal, double yVal, double width, double height) {
        this.id = id;
        this.imgId = imgId;
        this.xVal = xVal;
        this.yVal = yVal;
        this.width = width;
        this.height = height;
    }
}

