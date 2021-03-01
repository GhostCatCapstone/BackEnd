package ghostcat.capstone.holders;

import ghostcat.capstone.add_bbox.AddBBoxDAO;
import ghostcat.capstone.add_camera_trap.AddCameraTrapDAO;
import ghostcat.capstone.add_project.AddProjectDAO;
import ghostcat.capstone.authentication.TokenAuthenticationDAO;
import ghostcat.capstone.delete_bbox.DeleteBBoxDAO;
import ghostcat.capstone.get_camera_traps.GetCameraTrapsDAO;
import ghostcat.capstone.image_query.ImageQueryDAO;
import ghostcat.capstone.loginLambda.LoginDAO;
import ghostcat.capstone.update_bbox.UpdateBBoxDAO;

public class Factory {
    public static AddProjectDAO addProjectDAO;
    public static LoginDAO loginDAO;
    public static ImageQueryDAO imageQueryDAO;
    public static TokenAuthenticationDAO tokenAuthenticationDAO;
    public static UpdateBBoxDAO updateBBoxDAO;
    public static DeleteBBoxDAO deleteBBoxDAO;
    public static GetCameraTrapsDAO getCameraTrapsDAO;
    public static AddBBoxDAO addBBoxDAO;
    public static AddCameraTrapDAO addCameraTrapDAO;
    static {
      addProjectDAO = new AddProjectDAO();
      loginDAO = new LoginDAO();
      imageQueryDAO = new ImageQueryDAO();
      tokenAuthenticationDAO = new TokenAuthenticationDAO();
      updateBBoxDAO = new UpdateBBoxDAO();
      deleteBBoxDAO = new DeleteBBoxDAO();
      getCameraTrapsDAO = new GetCameraTrapsDAO();
      addBBoxDAO = new AddBBoxDAO();
      addCameraTrapDAO = new AddCameraTrapDAO();
    }

}
