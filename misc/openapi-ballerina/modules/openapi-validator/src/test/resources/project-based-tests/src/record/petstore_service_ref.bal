import ballerina/http;

listener http:Listener ep1 = new(9090, config = {host: "localhost"});

@http:ServiceConfig {
        basePath: "/v2"
}
service petstore_service_bal on ep1 {

 @http:ResourceConfig {
     methods:["POST"],
     path:"/pet",
     body:"body"
 }
 resource function addPet (http:Caller caller, http:Request req,  Pet  body) returns error? {

 }

//@http:ResourceConfig {
//        methods:["POST"],
//        path:"/user",
//        body:"body"
//        }
//        resource function createUser (http:Caller caller, http:Request req,  User  body) returns error? {
//        }

}