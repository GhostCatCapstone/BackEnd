package ghostcat.capstone.holders;

import ghostcat.capstone.add_project.AddProjectDAO;
import ghostcat.capstone.authentication.TokenAuthenticationDAO;
import ghostcat.capstone.image_query.ImageQueryDAO;
import ghostcat.capstone.loginLambda.LoginDAO;
import ghostcat.capstone.update_bbox.UpdateBBoxDAO;

public class Factory {
    public static AddProjectDAO addProjectDAO;
    public static LoginDAO loginDAO;
    public static ImageQueryDAO imageQueryDAO;
    public static TokenAuthenticationDAO tokenAuthenticationDAO;
    public static UpdateBBoxDAO updateBBoxDAO;
    static {
      addProjectDAO = new AddProjectDAO();
      loginDAO = new LoginDAO();
      imageQueryDAO = new ImageQueryDAO();
      tokenAuthenticationDAO = new TokenAuthenticationDAO();
      updateBBoxDAO = new UpdateBBoxDAO();
    }

}
