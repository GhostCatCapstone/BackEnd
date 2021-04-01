package ghostcat.capstone.add_camera_trap;

import ghostcat.capstone.holders.Factory;

public class AddCameraTrapHandler {

    //DAO object used to access the database
    static AddCameraTrapDAO dao;


    /**
     * Method invoked by AWS lambda.
     */
    public AddCameraTrapResponse handleRequest(AddCameraTrapRequest request) {
        //Get DAO object from factory
        dao = Factory.addCameraTrapDAO;

        //Create new response object
        AddCameraTrapResponse response = new AddCameraTrapResponse();

        //Error check the request object
        response = errorCheckRequest(request);
        if (!response.success) return response;

        //Call method to perform database operation
        response = addCameraTrap(request);

        return response;
    }

    /**
     * Checks to ensure request is valid (all required fields are present, etc).
     * Will set response.success to false if there's an error.
     */
    public AddCameraTrapResponse errorCheckRequest(AddCameraTrapRequest request) {
        AddCameraTrapResponse response = new AddCameraTrapResponse();

        if (request.userID == null) {
            response.success = false;
            response.errorMsg = "Null userID";
        }
        if (request.projectID == null) {
            response.success = false;
            response.errorMsg = "Null projectID";
        }
        if (request.cameraTrapID == null) {
            response.success = false;
            response.errorMsg = "Null cameraTrapID";
        }

        return response;
    }



    /**
     * Calls DAO to perform operation on database.
     */
    public static AddCameraTrapResponse addCameraTrap(AddCameraTrapRequest request) {
        AddCameraTrapResponse response = dao.addCameraTrap(request);
        return response;
    }

}
