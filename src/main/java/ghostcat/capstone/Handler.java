package ghostcat.capstone;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

// Handler value: ghostcat.capstone.Handler
public class Handler implements RequestHandler<SQSEvent, String>{
  @Override
  public String handleRequest(SQSEvent event, Context context) {
    String response = "";
    // call Lambda API
    LambdaLogger logger = context.getLogger();
    logger.log("Running the lambda function with event:\n" + event.toString() +
            "\nAnd context:\n" + context.toString());

    response = "Success";
    return response;
  }
}