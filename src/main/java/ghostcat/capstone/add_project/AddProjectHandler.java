package ghostcat.capstone.add_project;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ghostcat.capstone.holders.Factory;

public class AddProjectHandler implements RequestHandler<AddProjectRequest, AddProjectResponse> {

    /**
     * Method invoked by the lambda. Adds a new project for the given user.
     *
     * @param request Object that contains data for the new project.
     * @return Object that contains a success message, or an error message if something went wrong.
     */
    public AddProjectResponse handleRequest(AddProjectRequest request, Context context) {
      if (context != null) {
        LambdaLogger logger = context.getLogger();
        logger.log("Called Add Project Handler");
      }

      AddProjectService addProjectService = new AddProjectService(Factory.addProjectDAO);
      return addProjectService.addProject(request);
    }
}
