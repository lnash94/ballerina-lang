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
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.BJsonSchemaUtil;
import org.ballerinalang.openapi.validator.Constants;
import org.ballerinalang.openapi.validator.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.OneOfTypeMismatch;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResolveComponentUtil;
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
public class InvalidValidatorUtilTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/record/resources/")
            .toAbsolutePath();
    private OpenAPI api;
    private OpenAPI newApi;
    private BLangPackage bLangPackage;
    private Schema extractSchema;
    private BVarSymbol extractBVarSymbol;
    private List<ValidationError> validationErrors = new ArrayList<>();

    @Test(description = "Test missing field in ballerinaType")
    public void testMissingFieldInJsonSchema() throws UnsupportedEncodingException, OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("invalidTests/missingFieldInJsonSchema.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/missingFieldInJsonSchema.bal");
        extractSchema = ValidatorTest.getComponet(api, "MissingFieldInJsonSchema");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0)instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "phone2");

    }

    @Test(description = "Test missing field in ballerinaType")
    public void testMissingFieldInBallerinaType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/missingFieldInBallerinaType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/missingFieldInBallerinaType.bal");
        extractSchema = ValidatorTest.getComponet(api, "MissingFieldInBallerinaType");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(1) instanceof MissingFieldInBallerinaType);
        Assert.assertEquals(validationErrors.get(1).getFieldName(), "middleName");
    }

    @Test(description = "Test type mismatch")
    public void testTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatch.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/typeMisMatch.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatch");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(),Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(),Constants.Type.STRING);
    }

    @Test(description = "Test type mismatch with array. Same field name has ballerina type as array and json type as " +
            "string")
    public void testTypeMismatchArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatchArray.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/typeMisMatchArray.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatchArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "phone");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(),Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(),Constants.Type.ARRAY);
    }

    @Test(description = "Test type mismatch with array. Same field name has ballerina type as string array and json " +
            "type as integer array")
    public void testTypeMismatchArrayType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatchArrayType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/typeMisMatchArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatchArrayType");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "phone");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(description = "test Nested array type")
    public void testTypeMisMatchNestedArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatchNestedArrayType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/typeMisMatchNestedArrayType.bal");
        extractSchema = ValidatorTest.getComponet(api, "TypeMisMatchNestedArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals((validationErrors).get(0).getFieldName(), "phone");
        System.out.println(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema());
    }

    @Test(description = "Test record field with array type of another record")
    public void testRecordArray() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/recordTypeArray.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/recordTypeArray.bal");
        extractSchema = ValidatorTest.getComponet(api, "RecordTypeArray");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue((validationErrors).get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(description = "Test for nested record")
    public void testNestedRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/nestedRecord.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        newApi = ResolveComponentUtil.resolveOpeApiContract(api);
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/nestedRecord.bal");
        extractSchema = ValidatorTest.getComponet(newApi, "NestedRecord");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertEquals(validationErrors.get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.STRING);

    }

    @Test(description = "Test for nested 4 record")
    public void testNested4Record() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/nested4Record.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        newApi = ResolveComponentUtil.resolveOpeApiContract(api);
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/nested4Record.bal");
        extractSchema = ValidatorTest.getComponet(newApi, "FourNestedComponent");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertEquals(validationErrors.get(0).getFieldName(), "month");
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeJsonSchema(), Constants.Type.STRING );
        Assert.assertEquals(((TypeMismatch) (validationErrors).get(0)).getTypeBallerinaType(), Constants.Type.INT);

    }
//      need to implement
    @Test(description = "test nested record has field with inline record ")
    public void testInlineRecordinNested() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/nested4Recordinline.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/inlineRecord.bal");
        extractSchema = ValidatorTest.getComponet(api, "AllOfTest");
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
//        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

//        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).get(0)instanceof
//                TypeMismatch);
    }

    @Test(description = "Test oneOf type with primitive data type")
    public void testOneOfTypewithPrimitiveData() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/primitive/oneOfPrimitive.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/primitive/oneOf.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().
                        getContent().get("application/json").getSchema();
        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(((TypeMismatch)validationErrors.get(0)).getTypeBallerinaType(), Constants.Type.DECIMAL);

    }

    @Test(description = "Test oneOf with record type Type mismatching")
    public void testOneOfType() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOf.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/oneOf/oneOf.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().get("application/json").getSchema();

        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);
        Assert.assertTrue(validationErrors.get(0) instanceof OneOfTypeMismatch);
        Assert.assertEquals(((OneOfTypeMismatch) validationErrors.get(0)).getFieldName(), "Cat");
        Assert.assertEquals(((OneOfTypeMismatch) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(), "id");


    }

    @Test(description = "Test oneOf with record type Type mismatching")
    public void testOneOfTypeMistMatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOfTypeMismatch.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/oneOf/oneOfTypeMismatch.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().get("application/json").getSchema();

        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);
        System.out.println(validationErrors);
        Assert.assertTrue(validationErrors.stream().allMatch(item -> item instanceof OneOfTypeMismatch));
//        Assert.assertEquals(((TypeMismatch) validationErrors.get(0)).getFieldName(), "id");

    }

    @Test(description = "Test for missing fields in json schema when oneOf type record scenarios ")
    public void testOneOfMisJson() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOfMisFieldsJson.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/oneOf/oneOfMisFieldsJson.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().get("application/json").getSchema();

        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);

        Assert.assertTrue(validationErrors.get(0) instanceof OneOfTypeMismatch);
        Assert.assertEquals(((OneOfTypeMismatch) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(),
                "place");

    }
    @Test(description = "Test for missing fields in ballerina when oneOf type has")
    public void testOneOfMisBal() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("invalidTests/oneOf/oneOfBalRecord.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/oneOf/oneOfBalRecord.bal");
        ComposedSchema extractSchema =
                (ComposedSchema) api.getPaths().get("/oneOfRequestBody").getPost().getRequestBody().getContent().get("application/json").getSchema();

        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);
        System.out.println(((OneOfTypeMismatch) validationErrors.get(0)).getBlockErrors().get(0).getFieldName());
        Assert.assertTrue(validationErrors.stream().allMatch(item -> item instanceof OneOfTypeMismatch));
        Assert.assertEquals(((OneOfTypeMismatch) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(),
                "age");
    }
//
//    @Test(description = "Test allOf type")
//    public void testAllOfType() throws OpenApiValidatorException, UnsupportedEncodingException {
//        Path contractPath = RES_DIR.resolve("invalidTests/allOfType.yaml");
//        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/allOfType.bal");
//        extractSchema = ValidatorTest.getComponet(api, "AllOfTest");
//        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
//        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);
//
//    }
//
//    @Test(description = "Inline record ")
//    public void testInlineRecord() throws OpenApiValidatorException, UnsupportedEncodingException {
//        Path contractPath = RES_DIR.resolve("invalidTests/inlineRecord.yaml");
//        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        bLangPackage = ValidatorTest.getBlangPackage("invalidTests/inlineRecord.bal");
//        extractSchema = ValidatorTest.getComponet(api, "AllOfTest");
//        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
//        validationErrors = BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol);
//
////        Assert.assertTrue((BJsonSchemaUtil.validateBallerinaType(extractSchema, extractBVarSymbol)).get(0)instanceof
////                TypeMismatch);
//    }
//
//    @Test(description = "Test record fields")
//    public void testRecordFields() throws UnsupportedEncodingException {
//        bLangPackage = ValidatorTest.getBlangPackage("validTests/nestedRecord.bal");
//        extractBVarSymbol = ValidatorTest.getBVarSymbol(bLangPackage);
//        List<String> abc = BJsonSchemaUtil.getRecordFields((BRecordType) extractBVarSymbol.getType());
////        System.out.println(abc);
//    }
//    @Test(description = "Test record fields")
//    public void testSchemaFields() throws UnsupportedEncodingException, OpenApiValidatorException {
//        Path contractPath = RES_DIR.resolve("validTests/nestedRecord.yaml");
//        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        extractSchema = ValidatorTest.getComponet(api, "NestedRecord");
//        List<String> abc = BJsonSchemaUtil.getSchemaFields(extractSchema);
////        System.out.println(abc);
//    }
}
