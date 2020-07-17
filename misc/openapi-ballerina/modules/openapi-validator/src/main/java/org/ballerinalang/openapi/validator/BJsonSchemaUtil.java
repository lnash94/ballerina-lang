/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This util class for validate the any given schema with BVarSymbol type.
 */
public  class BJsonSchemaUtil {

    public static List<ValidationError> validateBallerinaType(Schema schema, BVarSymbol bVarSymbol)
            throws OpenApiValidatorException {

        List<ValidationError> validationErrors = new ArrayList<>();
        Map<String, Schema> properties = null;
        BType ballerinaParamType = bVarSymbol.getType();
        Boolean isExitType = false;
        /** Handle the body parameter with records
         *  Here validate the BvarType againts to schema
         */

        if (schema != null) {
            if ((bVarSymbol.type instanceof BRecordType) ||
                    ((schema instanceof ObjectSchema))) {

                if (schema instanceof Schema) {
                    if (schema.getProperties() != null) {
                        properties = schema.getProperties();
                    } else {
//                    String schemaRef = schema.get$ref();
//                    properties = OpenAPISummaryUtil.getOpenAPIComponent(schemaRef).getProperties();
                    }
                }
                if (schema instanceof ObjectSchema) {
                    properties = ((ObjectSchema) schema).getProperties();
                }
                BType resourceType;
                BRecordType recordType = null;

//          check the array type whether the array
                if (bVarSymbol.type instanceof BArrayType) {
                    if (((BArrayType) bVarSymbol.type).eType instanceof BRecordType) {
                        resourceType = ((BArrayType) bVarSymbol.type).eType;
                        recordType = (BRecordType) resourceType;
                    }
                } else {
                    resourceType = bVarSymbol.getType();
                    recordType = (BRecordType) resourceType;
                }

                for (BField field : recordType.fields) {
                    boolean isExist = false;
                    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                        if (entry.getKey().equals(field.name.getValue())) {
                            isExist = true;
                            if (entry.getValue().getType() != null) {
                                if (!field.getType().getKind().typeName()
                                        .equals(BJsonSchemaUtil.convertOpenAPITypeToBallerina(entry.getValue()
                                                .getType()))) {

                                    TypeMismatch validationError = new TypeMismatch(
                                            field.name.getValue().toString(),
                                            convertTypeToEnum(entry.getValue().getType()),
                                            convertTypeToEnum(field.getType().getKind().typeName()));

                                    validationErrors.add(validationError);

                                } else if (entry.getValue() instanceof ObjectSchema) {
//                         Handle the nested record type
                                    Schema schema1 = entry.getValue();
                                    if (field.type instanceof BRecordType) {
                                        List<ValidationError> nestedRecordValidation = BJsonSchemaUtil
                                                .validateBallerinaType(schema1, field.symbol);
                                        validationErrors.addAll(nestedRecordValidation);
                                    } else {
//                                    Type mismatch
                                        TypeMismatch validationError = new TypeMismatch(
                                                field.name.getValue().toString(),
                                                convertTypeToEnum("record"),
                                                convertTypeToEnum(field.getType().getKind().typeName()));
                                        validationErrors.add(validationError);
                                    }

                                } else {
//                                Handle array type mismatching.
                                    if (field.getType().getKind().typeName().equals("[]")) {
                                        BArrayType bArrayType = (BArrayType) field.type;
                                        ArraySchema arraySchema = (ArraySchema) entry.getValue();
                                        BArrayType traversNestedArray = bArrayType;
                                        ArraySchema traversSchemaNestedArray = arraySchema;
//                               Handle nested array type
                                        if ((bArrayType.eType instanceof BArrayType) &&
                                                (arraySchema.getItems() instanceof ArraySchema)) {
                                            traversNestedArray = (BArrayType) bArrayType.eType;
                                            traversSchemaNestedArray = (ArraySchema) arraySchema.getItems();

                                            while ((traversNestedArray.eType instanceof BArrayType) &&
                                                    (traversSchemaNestedArray.getItems() instanceof ArraySchema)) {

                                                traversSchemaNestedArray =
                                                        (ArraySchema) traversSchemaNestedArray.getItems();
                                                traversNestedArray = (BArrayType) traversNestedArray.eType;
                                            }
                                        }
//                                    Handle record type array

                                        if ((traversNestedArray.eType instanceof BRecordType) &&
                                                traversSchemaNestedArray.
                                                        getItems() != null) {
                                            if ((traversNestedArray.eType.tsymbol.type instanceof BRecordType) &&
                                                    traversSchemaNestedArray.getItems() instanceof ObjectSchema) {
                                                Schema schema2 = traversSchemaNestedArray.getItems();
                                                BVarSymbol bVarSymbol2 = field.symbol;
                                                List<ValidationError> nestedRecordValidation = BJsonSchemaUtil
                                                        .validateBallerinaType(schema2, bVarSymbol2);
                                                validationErrors.addAll(nestedRecordValidation);
                                            }
                                        } else if (!traversNestedArray.eType.tsymbol.toString().equals(BJsonSchemaUtil
                                                .convertOpenAPITypeToBallerina(
                                                        traversSchemaNestedArray.getItems().getType()))) {

                                            TypeMismatch validationError = new TypeMismatch(
                                                    field.name.getValue().toString(),
                                                    convertTypeToEnum(traversSchemaNestedArray.getItems().getType()),
                                                    convertTypeToEnum(traversNestedArray.eType.tsymbol.toString()));
                                            validationErrors.add(validationError);
                                        }
                                    }
                                }
                            } else {
//                            this for by chance type= null in entry
                            }
                        }
                    }
                    if (!isExist) {
                        MissingFieldInJsonSchema validationError = new MissingFieldInJsonSchema(field.name.toString(),
                                convertTypeToEnum(field.getType().getKind().typeName()));
                        validationErrors.add(validationError);
                    }
                }
//            Find missing fields in BallerinaType
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    boolean isExist = false;
                    for (BField field : recordType.fields) {
                        if (field.name.getValue().equals(entry.getKey())) {
                            isExist = true;
//                      Handle the type mismatching in above validation
                        }
                    }
                    if (!isExist) {
                        MissingFieldInBallerinaType validationError = new MissingFieldInBallerinaType(entry.getKey(),
                                convertTypeToEnum(entry.getValue().getType().toString()));
                        validationErrors.add(validationError);
                    }
                }
                isExitType = true;
            } else if (ballerinaParamType.getKind().typeName().equals("[]")
                    && schema.getType().equals("array")) {

                ArraySchema arraySchema = (ArraySchema) schema;
                BArrayType bArrayType = (BArrayType) bVarSymbol.type;
                BArrayType traversNestedArray = bArrayType;
                ArraySchema traversSchemaNestedArray = arraySchema;

//                               Handle nested array type
                if ((bArrayType.eType instanceof BArrayType) &&
                        (arraySchema.getItems() instanceof ArraySchema)) {
                    traversNestedArray = (BArrayType) bArrayType.eType;
                    traversSchemaNestedArray = (ArraySchema) arraySchema.getItems();

                    while ((traversNestedArray.eType instanceof BArrayType) &&
                            (traversSchemaNestedArray.getItems() instanceof ArraySchema)) {

                        traversSchemaNestedArray =
                                (ArraySchema) traversSchemaNestedArray.getItems();
                        traversNestedArray = (BArrayType) traversNestedArray.eType;
                    }
                }
//                                    Handle record type array

                if ((traversNestedArray.eType instanceof BRecordType) &&
                        traversSchemaNestedArray.
                                getItems() != null) {
                    if ((traversNestedArray.eType.tsymbol.type instanceof BRecordType) &&
                            traversSchemaNestedArray.getItems() instanceof ObjectSchema) {
                        Schema schema2 = traversSchemaNestedArray.getItems();
                        List<ValidationError> nestedRecordValidation = BJsonSchemaUtil
                                .validateBallerinaType(schema2, bVarSymbol);
                        validationErrors.addAll(nestedRecordValidation);
                    }
                } else if (!traversNestedArray.eType.tsymbol.toString().equals(BJsonSchemaUtil
                        .convertOpenAPITypeToBallerina(
                                traversSchemaNestedArray.getItems().getType()))) {

                    TypeMismatch validationError = new TypeMismatch(
                            bVarSymbol.name.getValue().toString(),
                            convertTypeToEnum(traversSchemaNestedArray.getItems().getType()),
                            convertTypeToEnum(traversNestedArray.eType.tsymbol.toString()));
                    validationErrors.add(validationError);
                }
                isExitType = true;

            } else if (ballerinaParamType.getKind().typeName().equals("string")
                    && schema.getType().equals("string")) {
                isExitType = true;

            } else if (ballerinaParamType.getKind().typeName().equals("int")
                    && schema.getType().equals("integer")) {
                isExitType = true;

            } else if (ballerinaParamType.getKind().typeName().equals("boolean")
                    && schema.getType().equals("boolean")) {
                isExitType = true;

            } else if (ballerinaParamType.getKind().typeName().equals("boolean")
                    && schema.getType().equals("boolean")) {
                isExitType = true;

            } else if (ballerinaParamType.getKind().typeName().equals("boolean")
                    && schema.getType().equals("boolean")) {

                isExitType = true;

            } else if (bVarSymbol.type instanceof BUnionType) {

            }

        }
        if (!isExitType) {
            TypeMismatch typeMismatch = new TypeMismatch(bVarSymbol.name.toString(),
                    convertTypeToEnum(ballerinaParamType.getKind().typeName()),
                    convertTypeToEnum(schema.getType()));
            validationErrors.add(typeMismatch);
        }
        return validationErrors;
    }

    private static Constants.Type convertTypeToEnum(String type) {
        Constants.Type convertedType;
        switch (type) {
            case "integer":
                convertedType = Constants.Type.INTEGER;
                break;
            case "int":
                convertedType = Constants.Type.INT;
                break;
            case "string":
                convertedType = Constants.Type.STRING;
                break;
            case "boolean":
                convertedType = Constants.Type.BOOLEAN;
                break;
            case "array":
            case "[]":
                convertedType = Constants.Type.ARRAY;
                break;
            case "object":
                convertedType = Constants.Type.OBJECT;
                break;
            case "record":
                convertedType = Constants.Type.RECORD;
                break;
            case "number":
                convertedType = Constants.Type.DECIMAL;
                break;
            default:
                convertedType = Constants.Type.ANYDATA;
        }

        return convertedType;
    }

    public static String convertOpenAPITypeToBallerina(String type) {
        String convertedType;
        switch (type) {
            case "integer":
                convertedType = "int";
                break;
            case "string":
                convertedType = "string";
                break;
            case "boolean":
                convertedType = "boolean";
                break;
            case "array":
                convertedType = "[]";
                break;
            case "object":
                convertedType = "record";
                break;
            case "number":
                convertedType = "decimal";
                break;
            default:
                convertedType = "";
        }

        return convertedType;
    }

}