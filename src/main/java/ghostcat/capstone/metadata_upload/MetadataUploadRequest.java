package ghostcat.capstone.metadata_upload;

import java.util.List;

public class MetadataUploadRequest {
  public ProjectMetadataUploadRequest projectMetadata;
  public List<ImageMetadataUploadRequest> imageMetadata;
  public List<BBMetadataUploadRequest> bbMetadata;
  public List<CameraTrapMetadataUploadRequest> cameraMetadata;
}
