package ghostcat.capstone.image_query;

import ghostcat.capstone.holders.Image;

import java.util.ArrayList;
import java.util.List;


public class ImageQueryResponse {
   public List<Image> images = new ArrayList<>();
   public String errorMsg = "";
   public boolean success = true;
}
