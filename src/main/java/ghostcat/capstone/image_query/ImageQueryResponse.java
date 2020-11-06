package ghostcat.capstone.image_query;

import ghostcat.capstone.holders.Image;

import java.util.ArrayList;


public class ImageQueryResponse {
   public ArrayList<Image> images = new ArrayList<>();
   public String errorMsg = "";
   public boolean success = true;
}
