## Welcome to the GhostCat BackEnd!
This is the backend repository for the GhostCat BYU capstone project. This repository contains lambda functions that recieve requests from the frontend, perform operations on a database, and return a response.

This backend is in Java, hosted entirely on AWS, and has the following components:

[flowchart]

## API Gateway
Handles calls from the frontend, and calls the appropriate lambda.


## Lambda
Called by the API Gateway. These functions are used to read and write to the database, and process requests from the front end.
In this repository, each lambda is organized into a package that contains four files. These files are:
* **Request**: object containing the request body.
* **Response**: object containing the response body.
* **Handler**: contains the handleRequest() method invoked by the AWS lambda. Handles all server logic. Processes requests, and returns a response. 
* **DAO**: encapsulates all calls to the DynamoDB server. DAO classes can easily be swapped out if a different type of server is needed. 

[Instructions on creating and invoking a lambda function can be found here].  

## DynamoDB
Stores all GhostCat data, excluding images. This includes:
* Image metadata: date, time, location, etc. of images.
* Classifier-generated data: bounding boxes and classifier predictions.
* Project data: classifier labels, camera traps, etc. associated with each project.
* Login data: auth tokens and hashed passwords for each user.


The tables are:
* [Auth](https://console.aws.amazon.com/dynamodb/home?region=us-east-1#tables:selected=Auth;tab=items): Used to verify that a user is logged in
* [BoundingBoxes](https://console.aws.amazon.com/dynamodb/home?region=us-east-1#tables:selected=BoundingBoxes;tab=items): Holds all data related to images and bounding boxes. Queried when a user makes a search. 
* [CameraTraps](https://console.aws.amazon.com/dynamodb/home?region=us-east-1#tables:selected=CameraTraps;tab=items): Contains the camera traps associated with each projectID. 
* [Login](https://console.aws.amazon.com/dynamodb/home?region=us-east-1#tables:selected=Login;tab=items): Used to login a user. Holds all userIDs and their associated hashed passwords. 
* [ProjectData](https://console.aws.amazon.com/dynamodb/home?region=us-east-1#tables:selected=ProjectData;tab=items): Contains the classes associated with each projectID. 


## S3
Stores raw images that can be retrieved and displayed on the frontend. 
