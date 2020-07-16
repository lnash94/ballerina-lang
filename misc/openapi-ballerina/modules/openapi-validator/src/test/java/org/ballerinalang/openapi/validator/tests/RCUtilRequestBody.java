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
import java.util.Map;

/**
 * Unit tests for ResoloveComponentUtil with requestBody.
 */
public class RCUtilRequestBody {

    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/componentResolve" +
            "/resources/pathItem/post/requestBody")
            .toAbsolutePath();
    private OpenAPI api;
    Components components;
    Collection<ApiResponse> responses;

    @Test(description = "Test01 - Test reusable request body")
    public void testReusableRB() throws OpenApiValidatorException {

        Path contractPath = RES_DIR.resolve("postReusableRequestBody.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();
        System.out.println(api);
    }
    @Test(description = "Test02 - Test reusable request body")
    public void testReusableRefRB() throws OpenApiValidatorException {

        Path contractPath = RES_DIR.resolve("postReusableRequestBody2.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();
        System.out.println(api);
    }
    @Test(description = "Test03 - Test reusable request body")
    public void testComponents() throws OpenApiValidatorException {

        Path contractPath = RES_DIR.resolve("postRequestBody.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();
        System.out.println(api);
    }
    @Test(description = "Test03 - Test reusable request body")
    public void testOneOfRB() throws OpenApiValidatorException {

        Path contractPath = RES_DIR.resolve("postOneOfRequestBody.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();
        System.out.println(api);
    }

    @Test(description = "Test03 - Test reusable request body")
    public void testOneOfRBArray() throws OpenApiValidatorException {

        Path contractPath = RES_DIR.resolve("postOneOfRBArray.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();
        System.out.println(api);
    }
    @Test(description = "Test03 - Test  request body with example with reference")
    public void testRBwithRefExample() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("postRBwithExampleref.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();
        System.out.println(api);
    }
}