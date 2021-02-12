package ghostcat.capstone.holders;

import ghostcat.capstone.add_project.AddProjectDAO;
import ghostcat.capstone.authentication.TokenAuthenticationDAO;
import ghostcat.capstone.image_query.ImageQueryDAO;
import ghostcat.capstone.loginLambda.LoginDAO;
import ghostcat.capstone.update_bbox.UpdateBBoxDAO;
import ghostcat.capstone.add_user.AddUserDAO;
import ghostcat.capstone.password_change.PasswordChangeDAO;

public class Factory {
    public static AddProjectDAO addProjectDAO;
    public static AddUserDAO addUserDAO;
    public static LoginDAO loginDAO;
    public static ImageQueryDAO imageQueryDAO;
    public static TokenAuthenticationDAO tokenAuthenticationDAO;
    public static UpdateBBoxDAO updateBBoxDAO;
    public static PasswordChangeDAO passwordChangeDAO;

    static {
      addProjectDAO = new AddProjectDAO();
      addUserDAO = new AddUserDAO();
      loginDAO = new LoginDAO();
      imageQueryDAO = new ImageQueryDAO();
      tokenAuthenticationDAO = new TokenAuthenticationDAO();
      updateBBoxDAO = new UpdateBBoxDAO();
      passwordChangeDAO = new PasswordChangeDAO();
    }

}
