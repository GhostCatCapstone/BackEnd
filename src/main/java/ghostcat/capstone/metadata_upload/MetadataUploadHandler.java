package ghostcat.capstone.metadata_upload;

import com.amazonaws.services.dynamodbv2.xspec.M;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import ghostcat.capstone.holders.Factory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MetadataUploadHandler implements RequestHandler<S3Event, MetadataUploadResponse> {
  @Override
  public MetadataUploadResponse handleRequest(S3Event s3event, Context context) {
    LambdaLogger logger = context.getLogger();
    MetadataUploadDAO dao = Factory.metadataUploadDAO;

    //Getting the metadata file's path
    S3EventNotification.S3EventNotificationRecord record = s3event.getRecords().get(0);
    String bucket = record.getS3().getBucket().getName();
    String key = record.getS3().getObject().getKey().replace("+", " ");
    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

    try {
      //Getting the file as a String Stream
      InputStreamReader inputStreamReader =
              new InputStreamReader(s3Client.getObject(bucket, key).getObjectContent());
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

      MetadataUploadRequest metadataUploadRequest = new MetadataUploadRequest();
      final int[] numImages = {0};
      final int[] numBB = {0};
      final int[] numCC = {0};
      bufferedReader.lines().skip(1).forEachOrdered((line) -> {
        String[] rawImageData = line.split(",");

        if (!rawImageData[0].isEmpty()) {
          metadataUploadRequest.projectMetadata = new ProjectMetadataUploadRequest();
          metadataUploadRequest.bbMetadata = new ArrayList<>();
          metadataUploadRequest.imageMetadata = new ArrayList<>();
          metadataUploadRequest.cameraMetadata = new ArrayList<>();
          numImages[0] = Integer.parseInt(rawImageData[4]);
          numBB[0] = Integer.parseInt(rawImageData[20]);
          numCC[0] = Integer.parseInt(rawImageData[16]);


          metadataUploadRequest.projectMetadata.projectID = rawImageData[0];
          metadataUploadRequest.projectMetadata.numClasses = Integer.parseInt(rawImageData[2]);
          metadataUploadRequest.projectMetadata.classes = new ArrayList<>();
          metadataUploadRequest.projectMetadata.classes.add(rawImageData[3]);
          metadataUploadRequest.projectMetadata.userID = rawImageData[1];
          if (numCC[0] > 0) {
            metadataUploadRequest.projectMetadata.usesCameraTraps = "True";
          } else {
            metadataUploadRequest.projectMetadata.usesCameraTraps = "False";
          }
        } else if (metadataUploadRequest.projectMetadata.numClasses <
                metadataUploadRequest.projectMetadata.classes.size()) {
          metadataUploadRequest.projectMetadata.classes.add(rawImageData[2]);
        }

        if (metadataUploadRequest.imageMetadata.size() < numImages[0]) {
          ImageMetadataUploadRequest imageMetadataUploadRequest = new ImageMetadataUploadRequest();
          imageMetadataUploadRequest.imageID = rawImageData[5];
          imageMetadataUploadRequest.imageLink = rawImageData[6];
          imageMetadataUploadRequest.imageWidth = Integer.parseInt(rawImageData[7]);
          imageMetadataUploadRequest.imageHeight = Integer.parseInt(rawImageData[8]);
          imageMetadataUploadRequest.flash = rawImageData[9];
          imageMetadataUploadRequest.cameraMake = rawImageData[10];
          imageMetadataUploadRequest.cameraModel = rawImageData[11];
          try {
            imageMetadataUploadRequest.imageDate = new SimpleDateFormat("MM-DD-YYYY HH:mm:ss")
                    .parse(rawImageData[12]).getTime();
          } catch (ParseException e) {
            e.printStackTrace();
          }
          imageMetadataUploadRequest.cameraTrap = rawImageData[13];
          imageMetadataUploadRequest.deployment = rawImageData[14];
          imageMetadataUploadRequest.nightImage = rawImageData[15];
          imageMetadataUploadRequest.userID = metadataUploadRequest.projectMetadata.userID;
          metadataUploadRequest.imageMetadata.add(imageMetadataUploadRequest);
        }

        if (metadataUploadRequest.cameraMetadata.size() < numCC[0]) {
          CameraTrapMetadataUploadRequest cameraTrapMetadataUploadRequest =
                  new CameraTrapMetadataUploadRequest();
          cameraTrapMetadataUploadRequest.cameraTrapID = rawImageData[17];
          cameraTrapMetadataUploadRequest.cameraLat = Double.parseDouble(rawImageData[18]);
          cameraTrapMetadataUploadRequest.cameraLng = Double.parseDouble(rawImageData[19]);
          cameraTrapMetadataUploadRequest.projectID = metadataUploadRequest.projectMetadata.projectID;
          metadataUploadRequest.cameraMetadata.add(cameraTrapMetadataUploadRequest);
        }

        if (metadataUploadRequest.bbMetadata.size() < numBB[0]) {
          BBMetadataUploadRequest bbMetadataUploadRequest = new BBMetadataUploadRequest();
          bbMetadataUploadRequest.bbId = rawImageData[21];
          bbMetadataUploadRequest.imageID = rawImageData[22];
          bbMetadataUploadRequest.bbX = Double.parseDouble(rawImageData[23]);
          bbMetadataUploadRequest.bbY = Double.parseDouble(rawImageData[24]);
          bbMetadataUploadRequest.bbWidth = Double.parseDouble(rawImageData[25]);
          bbMetadataUploadRequest.bbHeight = Double.parseDouble(rawImageData[26]);
          bbMetadataUploadRequest.classes = new HashMap<>();
          for (int i = 1; i <= metadataUploadRequest.projectMetadata.numClasses; i++) {
            bbMetadataUploadRequest.classes.put("class_" + i, Double.parseDouble(rawImageData[26+i]));
          }
          bbMetadataUploadRequest.userID = metadataUploadRequest.projectMetadata.userID;
          metadataUploadRequest.bbMetadata.add(bbMetadataUploadRequest);
        }

      });

      MetadataUploadResponse response = dao.addMetadata(metadataUploadRequest);
      bufferedReader.close();
      return response;
    } catch (Error | Exception e) {
      logger.log(e.toString());
      logger.log(Arrays.toString(e.getStackTrace()));
      MetadataUploadResponse response = new MetadataUploadResponse();
      response.success = false;
      response.errorMsg = e.toString();
      return response;
    }
  }
}
