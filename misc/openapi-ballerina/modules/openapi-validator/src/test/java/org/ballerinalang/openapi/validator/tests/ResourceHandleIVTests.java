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
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.MatchResourcewithOperationId;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.OpenapiServiceValidationError;
import org.ballerinalang.openapi.validator.ResourceValidationError;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class ResourceHandleIVTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/resourceHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BLangService extractBLangservice;
    private List<ResourceValidationError> validationErrors = new ArrayList<>();
    private List<OpenapiServiceValidationError> serviceValidationErrors = new ArrayList<>();

    @Test(description = "Test for checking whether resource paths are documented in openapi contract")
    public void testResourcePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = MatchResourcewithOperationId.checkResourceIsAvailable(api, extractBLangservice);

        Assert.assertTrue(validationErrors.get(0) instanceof ResourceValidationError);
        Assert.assertEquals(validationErrors.get(0).getResourcePath(), "/extraPathPet");
    }

    @Test(description = "Test for checking whether resource paths method are documented in openapi contract")
    public void testResourceExtraMethod() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreExtraMethod.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreExtraMethod.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = MatchResourcewithOperationId.checkResourceIsAvailable(api, extractBLangservice);

        Assert.assertTrue(validationErrors.get(0) instanceof ResourceValidationError);
        Assert.assertEquals(validationErrors.get(0).getresourceMethod(), "post");
    }

    @Test(description = "Test for checking whether openapi service operations are documented in ballerina resource")
    public void testExtraServicePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreExtraServiceOperation.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/invalid/petstoreExtraServiceOperation" +
                ".bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        serviceValidationErrors = MatchResourcewithOperationId.checkServiceAvailable(api, extractBLangservice);

        Assert.assertTrue(serviceValidationErrors.get(0) instanceof OpenapiServiceValidationError);
        Assert.assertEquals(serviceValidationErrors.get(0).getServiceOperation(), "post");
        Assert.assertEquals(serviceValidationErrors.get(0).getServicePath(), "/pets/{petId}");
    }
}
