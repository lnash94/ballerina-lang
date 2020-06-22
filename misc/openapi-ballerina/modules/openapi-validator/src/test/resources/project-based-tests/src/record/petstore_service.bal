import ballerina/http;
//import ballerina/openapi;

listener http:Listener ep0 = new(9090, config = {host: "localhost"});
//
//@openapi:ServiceInfo {
//    contract: "project-based-tests/openapi-validator/src/record_handling/resources/recordHandlingService.yaml"
//}
@http:ServiceConfig {
        basePath: "/v2"
}
service petstore_service on ep0 {


// @http:ResourceConfig {
//     methods:["POST"],
//     path:"/pet",
//     body:"body"
// }
// resource function addPet (http:Caller caller, http:Request req,  Pet  body) returns error? {
//
// }

@http:ResourceConfig {
        methods:["POST"],
        path:"/user",
        body:"body"
        }
        resource function createUser (http:Caller caller, http:Request req,  User  body) returns error? {
        }

}