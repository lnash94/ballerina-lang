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
 * Unit tests for ResoloveComponentUtil.
 */
public class RCUtilPostMethodTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/componentResolve" +
            "/resources/")
            .toAbsolutePath();
    private OpenAPI api;
    Components components;
    Collection<ApiResponse> responses;

    @Test(description = "Test12 -  path Item POST method single field")
    public void testResolveComponentType() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/post/post.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());

        Assert.assertEquals(ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                .getPost().getParameters().get(1).getSchema().getType().toString(), "string");
    }

    @Test(description = "Test13 - path Item POST method object type reference")
    public void testPathItemPostMultipleFields() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/post/postObjectRef.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());

        Assert.assertEquals(ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                .getPost().getParameters().get(1).getSchema().getType().toString(), "object");
    }

    @Test(description = "Test07 - Test path Item GET method without path parameter and with response")
    public void testPostWithResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("postWithResponse.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                        .getGet().getResponses().values();
        ApiResponse apiResponse = responses.iterator().next();
        MediaType mediaType = apiResponse.getContent().values().iterator().next();
        Assert.assertEquals(mediaType.getSchema().getType(), "object");
    }



}
