// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/config;
import ballerina/http;
import ballerina/jwt;

jwt:InboundJwtAuthProvider jwtAuthProvider22 = new({
    issuer: "https://localhost:9443/oauth2/token",
    audience: "vEwzbcasJVQm1jVYHUHCjhxZ4tYa",
    signatureConfig: {
        url: "https://localhost:20102/oauth2/jwks"
    }
});

http:BearerAuthHandler jwtAuthHandler22 = new(jwtAuthProvider22);

listener http:Listener listener22 = new(20029, {
    auth: {
        authHandlers: [jwtAuthHandler22]
    },
    secureSocket: {
        keyStore: {
            path: config:getAsString("keystore"),
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo"
}
service echo22 on listener22 {

    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond();
    }
}
