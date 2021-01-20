package ghostcat.capstone.add_user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ghostcat.capstone.holders.Factory;

public class AddUserHandler implements RequestHandler<AddUserRequest, AddUserResponse> {
  AddUserDAO addUserDAO;

  /**
   * Method invoked by the lambda. Queries DynamoDB, and filters the results based on the request.
   *
   * @param request Object that contains query parameters.
   * @return Object that contains query results, or an error message if something went wrong.
   */
  public AddUserResponse handleRequest(AddUserRequest request, Context context) {
    AddUserResponse response = new AddUserResponse();
    addUserDAO = Factory.addUserDAO;

    if (context != null) {
      LambdaLogger logger = context.getLogger();
      logger.log("Called AddUserHandler");
    }
    response.success = false;
    response.errorMsg = "An error occurred while trying to insert to DynamoDB";
    addUserDAO.addUser(request.UserID, request.passwordHash);

    response.success = true;
    response.errorMsg = "";

    return response;
  }
}
