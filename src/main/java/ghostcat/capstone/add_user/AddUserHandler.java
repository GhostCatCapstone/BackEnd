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

    if (request.UserID == null || request.passwordHash == null) {
      response.success = false;
      response.errorMsg = "Null UserID or password";
      return response;
    }

    response.success = addUserDAO.addUser(request.UserID, request.passwordHash);

    if (response.success) {
      response.errorMsg = "";
    } else {
      response.errorMsg = "Error occurred while trying to insert into DynamoDB";
    }

    return response;
  }
}
