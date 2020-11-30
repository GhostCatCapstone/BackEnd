package ghostcat.capstone.holders;

import ghostcat.capstone.add_project.AddProjectDAO;
import ghostcat.capstone.image_query.ImageQueryDAO;
import ghostcat.capstone.loginLambda.LoginDAO;

public class Factory {
    public static AddProjectDAO addProjectDAO;
    public static LoginDAO loginDAO;
    public static ImageQueryDAO imageQueryDAO;
    static {
      addProjectDAO = new AddProjectDAO();
      loginDAO = new LoginDAO();
      imageQueryDAO = new ImageQueryDAO();
    }

}