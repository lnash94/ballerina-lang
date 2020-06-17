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
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for {}.
 */
public class ValidatorUtilTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;

    @BeforeTest(description = "Setup the Openapi schema")
    public void setup() throws OpenApiValidatorException, UnsupportedEncodingException {
    }

    @Test(description = "Test boolean")
    public void testLoadOpenapiTest() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User")),true);
    }

    @Test(description = "Travse the schema")
    public void testLoadBType() throws UnsupportedEncodingException {
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
//        Path sourceRoot = RES_DIR.resolve("project-based-tests/src/record");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));
//        bLangPackage = OpenApiValidatorUtil.compileFile(sourceRoot,"petstore_service.bal");
//        System.out.println(bLangPackage);
        Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType( bLangPackage.getFunctions().get(0).mapSymbol),true);


//        String balfile = sourceRoot.resolve("recordHandlingService.bal").toString();
//        Path balFpath = Paths.get(balfile);
//        Path programDir = balFpath.toAbsolutePath().getParent();
//        String filename = balFpath.toAbsolutePath().getFileName().toString();
//        bLangPackage = OpenApiValidatorUtil.compileFile(programDir, filename);
//        bLangPackage.getTypeDefinitions().get(0).symbol

    }

//    @Test(description = "Test json schema missing fields")
//    public void missJsonFields (){
//        Path sourceRoot = RES_DIR.resolve("project-based-tests/openapi-validator/");
//        List<String> argList = new ArrayList<>();
//        argList.add("record_handling");
//        try {
//            OpenApiValidatorUtil.execute(argList,false,sourceRoot);
////            Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType( bLangPackage.getTypeDefinitions().get(0).symbol),true);
//        } catch (BLauncherException e) {
//            List<String> exception = e.getMessages();
//
//        }
//    }
}
