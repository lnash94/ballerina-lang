import ballerina/http;

listener http:Listener ep0 = new(9090, config = {host: "localhost"});

@http:ServiceConfig {
        basePath: "/v2"
}
service petstore_service on ep0 {

 @http:ResourceConfig {
         methods:["GET"],
         path:"/user/{username}"
     }
     resource function getUserByName (http:Caller caller, http:Request req,  string username1) returns error? {

     }


}