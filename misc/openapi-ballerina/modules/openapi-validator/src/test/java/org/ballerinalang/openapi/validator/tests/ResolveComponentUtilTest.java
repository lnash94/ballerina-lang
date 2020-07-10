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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResolveComponentUtil;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Unit tests for ResoloveComponentUtil.
 */
public class ResolveComponentUtilTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/componentResolve" +
            "/resources/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BVarSymbol extractBVarSymbol;

    @Test(description = "Test01 - Test path Item get method with path parameter")
    public void testPathItemGetwithParameter() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/getwithParameter.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());

        Assert.assertEquals(ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user/{userId}")
                .getGet().getParameters().get(0).getSchema().getType().toString(), "string");
    }

    @Test(description = "Test02 - Test path Item get method without path parameter and with response")
    public void testPathItemGetwithoutParameter() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/getwithoutPathParam.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        System.out.println(ResolveComponentUtil.resolveOpeApiContract(api));
        Collection<ApiResponse> responses =
                (Collection<ApiResponse>) ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                        .getGet().getResponses().values();
        ApiResponse apiResponse = responses.iterator().next();
        MediaType mediaType = apiResponse.getContent().values().iterator().next();
        Assert.assertEquals(mediaType.getSchema().getType(), "object");
    }

    @Test(description = "Test03 - Test path Item get method without path parameter and with multiple response")
    public void testPathItemGetwithoutPathParamAndMultipleResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/getMultipleResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        System.out.println(ResolveComponentUtil.resolveOpeApiContract(api));
        Collection<ApiResponse> responses =
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

    @Test(description = "Test04 - Test path Item get method without path parameter and with multiple mime response")
    public void testGetMultipleMIMEResponses() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/getOneResponseWithMultipleMIME.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        System.out.println(ResolveComponentUtil.resolveOpeApiContract(api));
        Collection<ApiResponse> responses =
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

    @Test(description = "Test05 - Test path Item get method with oneOf type response")
    public void testGetOneofResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/getOneOfResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        System.out.println(ResolveComponentUtil.resolveOpeApiContract(api));

    }

    @Test(description = "Test06 - Test path Item get method with oneOf type response")
    public void testGetAnyofResponse() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/getAnyOfResponses.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        System.out.println(ResolveComponentUtil.resolveOpeApiContract(api));

    }

    @Test(description = "Test0 -  path Item post method single field")
    public void testResolveComponentType() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/post.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());

        Assert.assertEquals(ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                .getPost().getParameters().get(1).getSchema().getType().toString(), "string");
    }

    @Test(description = "Test path Item post method object type reference")
    public void testPathItemPostMultipleFields() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("pathItem/postObjectRef.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());

        Assert.assertEquals(ResolveComponentUtil.resolveOpeApiContract(api).getPaths().get("/user")
                .getPost().getParameters().get(1).getSchema().getType().toString(), "object");
    }




}
