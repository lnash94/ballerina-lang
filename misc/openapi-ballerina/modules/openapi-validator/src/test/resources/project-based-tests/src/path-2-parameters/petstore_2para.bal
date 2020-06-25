import ballerina/http;

listener http:Listener ep0 = new(80, config = {host: "localhost"});

@openapi:ServiceInfo {
    contract: "resources/path2.yaml"
}
@http:ServiceConfig {
    basePath: "/api/v1"
}

service petstore_2para on ep0 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{param1}/{param2}"
    }
    resource function test2Params (http:Caller caller, http:Request req,  string param1,  string param2) returns error? {

    }

}