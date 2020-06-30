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

/**
 *
 */
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This util class for validate the any given schema with BVarSymbol type.
 */
public  class BJsonSchemaUtil {

    public static List<ValidationError> validateBallerinaType(Schema schema, BVarSymbol bVarSymbol)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrors = new ArrayList<>();
        Map<String, Schema> properties = null;
        /** Handle the body parameter with records
         *  Here validate the BvarType againts to schema
         */
        if (schema != null) {
            if (schema instanceof Schema) {
                if (schema.getProperties() != null) {
                    properties = schema.getProperties();
                } else {
                    String schemaRef = schema.get$ref();
                    properties = OpenAPISummaryUtil.getOpenAPIComponent(schemaRef).getProperties();
                }
            }
            if (schema instanceof ObjectSchema) {
                properties = ((ObjectSchema) schema).getProperties();
            }

            BType resourceType = bVarSymbol.getType();
            BRecordType recordType = (BRecordType) resourceType;

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

                            }
                        } else {
//                         Handle the nested record type
                            if (entry.getValue().get$ref() != null) {
                                Schema schema1 = OpenAPISummaryUtil.getOpenAPIComponent(entry.getValue().get$ref());
                                if (field.type instanceof BRecordType) {
                                    List<ValidationError> nestedRecordValdidation = BJsonSchemaUtil
                                            .validateBallerinaType(schema1, field.symbol);
                                    validationErrors.addAll(nestedRecordValdidation);
                                } else {
//                                    Type mismatch
                                    TypeMismatch validationError = new TypeMismatch(
                                            field.name.getValue().toString(),
                                            convertTypeToEnum("object"),
                                            convertTypeToEnum(field.getType().getKind().typeName()));
                                    validationErrors.add(validationError);
                                }
                            }
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
                    for (BField field: recordType.fields) {
                        if (field.name.getValue().equals(entry.getKey())) {
                            isExist = true;
//Handle the type mismatching in above validation
                        }
                    }
                    if (!isExist) {
                        MissingFieldInBallerinaType validationError = new MissingFieldInBallerinaType(entry.getKey(),
                                convertTypeToEnum(entry.getValue().getType().toString()));
                        validationErrors.add(validationError);
                    }
                }
        }

        return validationErrors;

    }

    private static Constants.Type convertTypeToEnum(String type) {
        Constants.Type convertedType;
        switch (type) {
            case "integer":
                convertedType = Constants.Type.INT;
                break;
            case "string":
                convertedType = Constants.Type.STRING;
                break;
            case "boolean":
                convertedType = Constants.Type.BOOLEAN;
                break;
            case "array":
                convertedType = Constants.Type.ARRAY;
                break;
            case "object":
                convertedType = Constants.Type.RECODR;
                break;
            case "number":
                convertedType = Constants.Type.DECIMAL;
                break;
            default:
                convertedType = Constants.Type.ANYDATA;
        }

        return convertedType;
    }

    private static String convertOpenAPITypeToBallerina(String type) {
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
