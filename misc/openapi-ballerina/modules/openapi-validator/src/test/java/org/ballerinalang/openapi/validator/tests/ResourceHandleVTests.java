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
import org.ballerinalang.openapi.validator.ResourceValidationError;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ResourceHandleVTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/resourceHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BLangService extractBLangservice;
    private List<ResourceValidationError> validationErrors = new ArrayList<>();

    @Test(description = "Test for checking whether resource paths ara documented in openapi contract")
    public void testResourcePath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceHandle/ballerina/valid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        validationErrors = MatchResourcewithOperationId.checkResouceIsAvailable(api, extractBLangservice);
        Assert.assertTrue(validationErrors.isEmpty());
//        System.out.println(api);

    }

}
