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
import org.ballerinalang.openapi.validator.BJsonSchemaUtil;
import org.ballerinalang.openapi.validator.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.OpenApiValidatorUtil;
import org.ballerinalang.openapi.validator.TypeMismatch;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for {}.
 */
public class ValidatorUtilTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;

    @Test(description = "Test boolean")
    public void testLoadOpenapiTest() throws OpenApiValidatorException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User")),true);
    }

    @Test(description = "Traverse the Btype")
    public void testLoadBType() throws UnsupportedEncodingException {
//        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        String ballerinaFilePath = RES_DIR.resolve("project-based-tests").resolve("src/record").
                resolve("petstore_service.bal").toString();
        Path filePath = Paths.get(ballerinaFilePath);
        Path programDir = filePath.toAbsolutePath().getParent();
        String fileName = filePath.toAbsolutePath().getFileName().toString();
        bLangPackage = OpenApiValidatorUtil.compileFile(programDir, fileName);
//        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));
//        Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType( bLangPackage.getServices().get(0).
//        resourceFunctions.get(0).symbol.params.get(2)),true);

    }

    @Test(description = "Test missing field in JsonSchema")
    public void testMissingFieldInJsonSchema() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));
        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(1))instanceof
                MissingFieldInJsonSchema);
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(1).getFieldName()),
                "username");
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(2).getFieldName()),
                "phone2");

    }

    @Test(description = "Test missing field in ballerinaType")
    public void testMissingFieldInBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)))instanceof List);
        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2))).get(3)instanceof
                MissingFieldInBallerinaType);
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(3).getFieldName()),
                "username1");
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(4).getFieldName()),
                "phone");

    }

    @Test(description = "Test type mismatch")
    public void testTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2))).get(0)instanceof
                TypeMismatch);
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(0).getFieldName()),
                "id");

    }


    @Test(description = "Test References, missing field in ballerinaType")
    public void testRefMissingFieldInBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));
        Assert.assertTrue(BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("Pet"),
                bLangPackage.getServices().get(1).resourceFunctions.get(0).symbol.params.get(2))instanceof List);

    }


}
