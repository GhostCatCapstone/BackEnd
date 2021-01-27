package ghostcat.capstone.holders;

import ghostcat.capstone.add_project.AddProjectDAO;
import ghostcat.capstone.image_query.ImageQueryDAO;
import ghostcat.capstone.loginLambda.LoginDAO;
import ghostcat.capstone.password_change.PasswordChangeDAO;

public class Factory {
    public static AddProjectDAO addProjectDAO;
    public static LoginDAO loginDAO;
    public static ImageQueryDAO imageQueryDAO;
    public static PasswordChangeDAO passwordChangeDAO;

    static {
      addProjectDAO = new AddProjectDAO();
      loginDAO = new LoginDAO();
      imageQueryDAO = new ImageQueryDAO();
      passwordChangeDAO = new PasswordChangeDAO();
    }

}
