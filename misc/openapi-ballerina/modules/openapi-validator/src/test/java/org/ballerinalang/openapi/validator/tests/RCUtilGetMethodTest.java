/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResolveComponentUtil;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Unit tests for ResoloveComponentUtil with GET method.
 */
public class RCUtilGetMethodTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/componentResolve" +
            "/resources/pathItem/get/")
            .toAbsolutePath();
    private OpenAPI api;
    Collection<ApiResponse> responses;

    @Test(description = "Test06 - Test path Item GET method with path parameter")
    public void testPathItemGetwithParameter() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getwithParameter.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());

        Assert.assertEquals(ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user/{userId}")
                .getGet().getParameters().get(0).getSchema().getType().toString(), "string");
    }

    @Test(description = "Test07 - Test path Item GET method without path parameter and with response")
    public void testPathItemGetwithoutParameter() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getwithoutPathParam.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                        .getGet().getResponses().values();
        ApiResponse apiResponse = responses.iterator().next();
        MediaType mediaType = apiResponse.getContent().values().iterator().next();
        Assert.assertEquals(mediaType.getSchema().getType(), "object");
    }

    @Test(description = "Test07 - Test path Item GET method without path parameter and with response")
    public void testreferenceParameters() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getParameterswithRef.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/references")
                        .getGet().getResponses().values();
        ApiResponse apiResponse = responses.iterator().next();
        MediaType mediaType = apiResponse.getContent().values().iterator().next();
        Assert.assertEquals(mediaType.getSchema().getType(), "object");
    }

    @Test(description = "Test07 - Test path Item GET method without path parameter and with response")
    public void testreferenceResponses() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getRefResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                        .getGet().getResponses().values();
//        ApiResponse apiResponse = responses.iterator().next();
//        MediaType mediaType = apiResponse.getContent().values().iterator().next();
//        Assert.assertEquals(mediaType.getSchema().getType(), "object");
    }

    @Test(description = "Test08 - Test path Item GET method without path parameter and with multiple response")
    public void testPathItemGetwithoutPathParamAndMultipleResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getMultipleResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/multipleResponses")
                        .getGet().getResponses().values();

        Iterator<ApiResponse> apiResponseIterator = responses.iterator();

        ApiResponse apiResponse1 = apiResponseIterator.next();
        ApiResponse apiResponse2 = apiResponseIterator.next();

        MediaType mediaType1 = apiResponse1.getContent().values().iterator().next();
        MediaType mediaType2 = apiResponse2.getContent().values().iterator().next();

        Assert.assertEquals(mediaType1.getSchema().getType(), "object");
        Assert.assertEquals(mediaType2.getSchema().getType(), "object");

    }

    @Test(description = "Test09 - Test path Item GET method without path parameter and with multiple mime response")
    public void testGetMultipleMIMEResponses() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getOneResponseWithMultipleMIME.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/multipleMIMEResponses")
                        .getGet().getResponses().values();

        Iterator<ApiResponse> apiResponseIterator = responses.iterator();

        ApiResponse apiResponse1 = apiResponseIterator.next();
        MediaType mediaType1 = apiResponse1.getContent().values().iterator().next();
//        check next one also
        Assert.assertEquals(mediaType1.getSchema().getType(), "object");

    }

    @Test(description = "Test10 - Test path Item GET method with oneOf type response")
    public void testGetOneofResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getOneOfResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/oneOfResponse")
                        .getGet().getResponses().values();

        Iterator<ApiResponse> apiResponseIterator = responses.iterator();

        ApiResponse apiResponse1 = apiResponseIterator.next();
        MediaType mediaType1 = apiResponse1.getContent().values().iterator().next();
        ComposedSchema composedSchema = ((ComposedSchema) mediaType1.getSchema());
        Map<String, Schema> properties = composedSchema.getOneOf().get(0).getProperties();

        Assert.assertEquals(properties.get("id").getType(), "integer");
    }

    @Test(description = "Test11 - Test path Item GET method with anyOf type response")
    public void testGetAnyofResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getAnyOfResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/anyOfResponse")
                        .getGet().getResponses().values();

        Iterator<ApiResponse> apiResponseIterator = responses.iterator();

        ApiResponse apiResponse1 = apiResponseIterator.next();
        MediaType mediaType1 = apiResponse1.getContent().values().iterator().next();
        ComposedSchema composedSchema = ((ComposedSchema) mediaType1.getSchema());
        Map<String, Schema> properties = composedSchema.getAnyOf().get(0).getProperties();

        Assert.assertEquals(properties.get("id").getType(), "integer");
    }

    @Test(description = "Test12 - tests with nested components with response")
    public void testGETNestedcomponet() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("getwithoutPathParamNestedComponent.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        OpenAPI openAPI = ResolveComponentUtil.resolveOpeApiContract(api);

    }

}
