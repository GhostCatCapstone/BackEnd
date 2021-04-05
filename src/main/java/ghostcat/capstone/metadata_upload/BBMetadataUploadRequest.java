package ghostcat.capstone.metadata_upload;

import java.util.Map;

public class BBMetadataUploadRequest {
  public String userID;
  public String imageID;
  public String bbId;
  public double bbX;
  public double bbY;
  public double bbHeight;
  public double bbWidth;
  public Map<String, Double> classes;
}
