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
import org.ballerinalang.openapi.validator.BJsonSchemaUtil;
import org.ballerinalang.openapi.validator.Constants;
import org.ballerinalang.openapi.validator.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.TypeMismatch;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for BJsonSchemaUtil.
 */
public class ValidValidatorUtilTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/record/resources/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BVarSymbol extractBVarSymbol;

    @Test(description = "Valid test case")
    public void  testValidCase() throws OpenApiValidatorException, UnsupportedEncodingException {
//        Load yaml file
        Path contractPath = RES_DIR.resolve("validTests/valid.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        Load BlangPackage for given .bal file
        bLangPackage = ValidatorTest.getBlangPackage("validTests/validSchema.bal");
        Schema extractSchema = ValidatorTest.getComponet(api, "Valid");
        BVarSymbol extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(description = "Test type mismatch with array. Same field name has ballerina type as string array and json " +
            "type as integer array")
    public void testTypeMismatchArrayType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/validTypeMisMatchArrayType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("validTests/validTypeMisMatchArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "ValidTypeMisMatchArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(description = "Test Nested array type")
    public void testTypeMisMatchNestedArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("validTests/validTypeMisMatchNestedArrayType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("validTests/validTypeMisMatchNestedArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "ValidTypeMisMatchNestedArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).isEmpty());
    }

    @Test(description = "Test record field with array type of another record")
    public void testRecordArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/recordTypeArray.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/recordTypeArray.bal");
        extractSchema = ValidatorTest.getComponet(api, "RecordTypeArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).get(0)
                instanceof TypeMismatch);

    }

    @Test(description = "Test allOf type")
    public void testAllOfType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/allOfType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/allOfType.bal");
        extractSchema = ValidatorTest.getComponet(api, "AllOfTest");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

//        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).get(0)instanceof
//                TypeMismatch);
//        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol))
//                .get(0).getFieldName(), "phone");
    }

    @Test(description = "Test oneOf type")
    public void testOneOfType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/oneOf.bal");
        extractSchema = ValidatorTest.getComponet(api, "AllOfTest");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

//        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).get(0)instanceof
//                TypeMismatch);
//        Assert.assertEquals((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol))
//                .get(0).getFieldName(), "phone");
    }

    @Test(description = "Inline record ")
    public void testInlineRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/inlineRecord.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/inlineRecord.bal");
        extractSchema = ValidatorTest.getComponet(api, "AllOfTest");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

//        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).get(0)instanceof
//                TypeMismatch);
    }

    @Test(description = "Test for nested record")
    public void testNestedRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/nestedRecord.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/nestedRecord.bal");
        extractSchema = ValidatorTest.getComponet(api, "NestedRecord");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);

    }

}
