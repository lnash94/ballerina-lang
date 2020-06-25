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

import io.swagger.v3.oas.models.media.ObjectSchema;
import org.ballerinalang.openapi.validator.*;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
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

    @BeforeTest(description = "Setup the Openapi schema")
    public void setup() throws OpenApiValidatorException, UnsupportedEncodingException {
    }

    @Test(description = "Test boolean")
    public void testLoadOpenapiTest() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User")),true);
    }

    @Test(description = "Traverse the Btype")
    public void testLoadBType() throws UnsupportedEncodingException {
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));
//        Assert.assertEquals(BJsonSchemaUtil.validateBallerinaType( bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)),true);

    }

    @Test(description = "Test path parameter")
    public void testInlineValidateBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/path-parameter/resources/petstore_inline.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("path-parameter"));

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getPaths().get("/user/{username}").getGet().getParameters().get(0),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)))instanceof List);
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getPaths().get("/user/{username}").getGet().getParameters().get(0),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(0).getFieldName()),"username");
//        System.out.println(api.getPaths().get("/user/{username}").getGet().getParameters().get(0));
//        System.out.println(api.getPaths().get("/user/{username}").getGet().getParameters().get(0).getSchema());
//        System.out.println(bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2));
//        System.out.println(bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol);
    }
    @Test(description = "Test path multiple parameter")
    public void testTwoParametersValidateBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/path-2-parameters/resources/path_2para.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("path-2-parameters"));

//        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getPaths().get("/{param1}/{param2}").getGet().getParameters().get(0),
//                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)))instanceof List);
//        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getPaths().get("/{param1}/{param2}").getGet().getParameters().get(0),
//                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(0).getFieldName()),"username");
//        System.out.println(api.getPaths().get("/{param1}/{param2}").getGet().getParameters());
//        System.out.println(api.getPaths().get("/{param1}/{param2}").getGet().getParameters().get(0).getSchema());
//        System.out.println(bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2));
//        System.out.println(bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol);
    }


    @Test(description = "Test body parameter")
    public void testValidateBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        //load the yaml file
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        //load the resource file
        Path sourceRoot = RES_DIR.resolve("project-based-tests");
        bLangPackage = OpenApiValidatorUtil.compileModule(sourceRoot, OpenApiValidatorUtil.getModuleName("record"));

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)))instanceof List);
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(0).getFieldName()),"email2");
        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(api.getComponents().getSchemas().get("User"),
                bLangPackage.getServices().get(0).resourceFunctions.get(0).symbol.params.get(2)).get(1).getFieldName()),"phone2");

    }


}
