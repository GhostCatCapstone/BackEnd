package ghostcat.capstone.add_project;

public class AddProjectService {
  private AddProjectDAO addProjectDAO;

  public AddProjectService(AddProjectDAO addProjectDAO) {
    this.addProjectDAO = addProjectDAO;
  }

  /**
   * Adds a new project to the ProjectData table in DynamoDB
   * @param request Object that contains request parameters.
   * @return The response object for the given request
   */
  public AddProjectResponse addProject(AddProjectRequest request) {
    AddProjectResponse response = errorCheckRequest(request);
    if (!response.success) return response;

    response.success = false;
    response.errorMsg = "An error occurred while trying to insert to DynamoDB";
    String projectID = addProjectDAO.addProject(request);

    response.success = true;
    response.errorMsg = "";
    response.projectID = projectID;
    return response;
  }

  /**
   * Ensures that a request is valid.
   * @param request Object that contains request parameters.
   * @return response object that will contain error message if request is invalid.
   */
  public static AddProjectResponse errorCheckRequest(AddProjectRequest request) {
    AddProjectResponse response = new AddProjectResponse();
    response.success = true;

    if (request.userID == null) {
      response.success = false;
      response.errorMsg = "Null userID";
    }

    if (request.classNames.isEmpty()) {
      response.success = false;
      response.errorMsg = "Empty class name list";
    }

    return response;
  }
}
