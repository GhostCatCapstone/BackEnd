package ghostcat.capstone.metadata_upload;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

import java.util.Arrays;


public class MetadataUploadDAO {

  static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_EAST_2)
          .build();
  static DynamoDB dynamoDB = new DynamoDB(client);
  static String PROJECT_TABLE = "ProjectData";
  static String BB_TABLE = "BoundingBoxes";
  static String IMAGE_TABLE = "Images";
  static String CAMERA_TABLE = "CameraTraps";

  public MetadataUploadResponse addMetadata(MetadataUploadRequest request) {
    MetadataUploadResponse response = new MetadataUploadResponse();
    response.success = true;
    Table projectTable = dynamoDB.getTable(PROJECT_TABLE);
    Item projectItem = new Item();
    projectItem.withPrimaryKey("UserID", request.projectMetadata.userID);
    projectItem.withString("ProjectID", request.projectMetadata.projectID);
    projectItem.withString("uses_camera_traps", request.projectMetadata.usesCameraTraps);
    projectItem.withInt("num_classes", request.projectMetadata.numClasses);
    for (int i = 0; i < request.projectMetadata.classes.size(); i++) {
      projectItem.withString("class_" + (i+1), request.projectMetadata.classes.get(i));
    }
    projectTable.putItem(projectItem);

    Table imageTable = dynamoDB.getTable(IMAGE_TABLE);
    for (ImageMetadataUploadRequest imageRequest : request.imageMetadata) {
      Item bbItem = new Item();
      bbItem.withPrimaryKey("UserID", imageRequest.userID);
      bbItem.withString("ImageID", imageRequest.imageID);
      bbItem.withString("camera_make", imageRequest.cameraMake);
      bbItem.withString("camera_model", imageRequest.cameraModel);
      bbItem.withString("camera_trap", imageRequest.cameraTrap);
      bbItem.withString("deployment", imageRequest.deployment);
      bbItem.withString("flash_on", imageRequest.flash);
      bbItem.withLong("img_date", imageRequest.imageDate);
      bbItem.withInt("img_height", imageRequest.imageHeight);
      bbItem.withInt("img_width", imageRequest.imageWidth);
      bbItem.withString("img_link", imageRequest.imageLink);
      bbItem.withString("night_img", imageRequest.nightImage);
      imageTable.putItem(bbItem);
    }

    Table cameraTable = dynamoDB.getTable(CAMERA_TABLE);
    for (CameraTrapMetadataUploadRequest cameraRequest : request.cameraMetadata) {
      Item cameraItem = new Item();
      cameraItem.withPrimaryKey("ProjectID", cameraRequest.projectID);
      cameraItem.withString("CameraTrapID", cameraRequest.cameraTrapID);
      cameraItem.withDouble("camera_lat", cameraRequest.cameraLat);
      cameraItem.withDouble("camera_lng", cameraRequest.cameraLng);
      cameraTable.putItem(cameraItem);
    }

    Table bbTable = dynamoDB.getTable(BB_TABLE);
    for (BBMetadataUploadRequest bbRequest : request.bbMetadata) {
      Item bbItem = new Item();
      bbItem.withPrimaryKey("UserID", bbRequest.userID);
      bbItem.withString("img_id", bbRequest.imageID);
      bbItem.withString("BBoxID", bbRequest.bbId);
      bbItem.withDouble("bbox_X", bbRequest.bbX);
      bbItem.withDouble("bbox_Y", bbRequest.bbY);
      bbItem.withDouble("bbox_height", bbRequest.bbHeight);
      bbItem.withDouble("bbox_width", bbRequest.bbWidth);
      for (String key : bbRequest.classes.keySet()) {
        bbItem.withDouble(key, bbRequest.classes.get(key));
      }
      bbTable.putItem(bbItem);
    }

    return response;
  }
}
