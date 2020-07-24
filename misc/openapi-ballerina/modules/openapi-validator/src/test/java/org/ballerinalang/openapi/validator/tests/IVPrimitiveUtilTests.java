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
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.TypeMismatch;
import org.ballerinalang.openapi.validator.ValidationError;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for BJsonSchemaUtil Invalid tests.
 */
public class IVPrimitiveUtilTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/record/resources/")
            .toAbsolutePath();
    private OpenAPI api;
    private OpenAPI newApi;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BVarSymbol extractBVarSymbol;
    private List<ValidationError> validationErrors = new ArrayList<>();

    @Test(description = "Type mismatch with integer")
    public void testIntegerType() throws UnsupportedEncodingException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("invalidTests/primitive/integerB.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/primitive/integerB.bal");
        extractSchema = ValidatorTest.getSchema(api, "/user/{userId}");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "userId");

    }

    @Test(description = "Type mismatch with array")
    public void testArrayType() throws UnsupportedEncodingException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("invalidTests/primitive/arrayB.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/primitive/arrayB.bal");
        extractSchema = ValidatorTest.getSchema(api, "/user/{userId}");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "userId");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.INT);

    }

    @Test(description = "Type mismatch with array")
    public void testiArrayType() throws UnsupportedEncodingException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("invalidTests/primitive/iarrayB.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/primitive/iarrayB.bal");
        extractSchema = ValidatorTest.getSchema(api, "/user/{userId}");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "userId");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(description = "Type mismatch with record array")
    public void testRecordArrayType() throws UnsupportedEncodingException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("invalidTests/primitive/arrayRB.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/primitive/arrayRB.bal");
        extractSchema = ValidatorTest.getSchema(api, "/user/{category}");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(),"id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }


}
