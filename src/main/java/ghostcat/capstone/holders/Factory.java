package ghostcat.capstone.holders;

import ghostcat.capstone.add_bbox.AddBBoxDAO;
import ghostcat.capstone.add_camera_trap.AddCameraTrapDAO;
import ghostcat.capstone.add_project.AddProjectDAO;
import ghostcat.capstone.delete_bbox.DeleteBBoxDAO;
import ghostcat.capstone.get_camera_traps.GetCameraTrapsDAO;
import ghostcat.capstone.get_project_data.GetProjectDataDAO;
import ghostcat.capstone.image_query.ImageQueryDAO;
import ghostcat.capstone.metadata_upload.MetadataUploadDAO;
import ghostcat.capstone.update_bbox.UpdateBBoxDAO;

public class Factory {
    public static AddProjectDAO addProjectDAO;
    public static ImageQueryDAO imageQueryDAO;
    public static UpdateBBoxDAO updateBBoxDAO;
    public static DeleteBBoxDAO deleteBBoxDAO;
    public static GetCameraTrapsDAO getCameraTrapsDAO;
    public static AddBBoxDAO addBBoxDAO;
    public static AddCameraTrapDAO addCameraTrapDAO;
    public static GetProjectDataDAO getProjectDataDAO;
    public static MetadataUploadDAO metadataUploadDAO;

    static {
      addProjectDAO = new AddProjectDAO();
      imageQueryDAO = new ImageQueryDAO();
      updateBBoxDAO = new UpdateBBoxDAO();
      deleteBBoxDAO = new DeleteBBoxDAO();
      getCameraTrapsDAO = new GetCameraTrapsDAO();
      addBBoxDAO = new AddBBoxDAO();
      addCameraTrapDAO = new AddCameraTrapDAO();
      getProjectDataDAO = new GetProjectDataDAO();
      metadataUploadDAO = new MetadataUploadDAO();
    }

}
