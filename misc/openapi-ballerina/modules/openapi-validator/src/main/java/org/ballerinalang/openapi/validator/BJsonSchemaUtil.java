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
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
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

                            } else {
//                                Handle array type mismatching.
                                if (field.getType().getKind().typeName().equals("[]")){
                                    BArrayType bArrayType = (BArrayType) field.type;
                                    ArraySchema arraySchema = (ArraySchema) entry.getValue();
                                    BArrayType traversNestedArray = bArrayType;
                                    ArraySchema traversSchemaNestedArray = arraySchema;
//                               Handle nested array type
                                    if ((bArrayType.eType instanceof BArrayType) &&
                                            ( arraySchema.getItems() instanceof ArraySchema)) {
                                        traversNestedArray = (BArrayType) bArrayType.eType;
                                        traversSchemaNestedArray = (ArraySchema) arraySchema.getItems();

                                        while ((traversNestedArray.eType instanceof BArrayType) &&
                                                ( traversSchemaNestedArray.getItems() instanceof ArraySchema)) {

                                                traversSchemaNestedArray = (ArraySchema) traversSchemaNestedArray.getItems();
                                                traversNestedArray = (BArrayType) traversNestedArray.eType;

                                        }
                                    }
//                                    Handle record type array
                                    if ((traversNestedArray.eType instanceof BRecordType) && traversSchemaNestedArray.
                                            getItems().get$ref()!= null){
                                        if (!traversNestedArray.eType.tsymbol.name.toString().equals(
                                                OpenAPISummaryUtil.getcomponetName(traversSchemaNestedArray.getItems()
                                                        .get$ref()))) {
//                                            typemismatch
                                        } else {
                                            Schema schema2 =
                                                    OpenAPISummaryUtil.getOpenAPIComponent(traversSchemaNestedArray.getItems().get$ref());
                                            BVarSymbol bVarSymbol2 = field.symbol;
                                            List<ValidationError> nestedRecordValidation = BJsonSchemaUtil
                                                    .validateBallerinaType(schema2, bVarSymbol2);
                                            validationErrors.addAll(nestedRecordValidation);
                                        }

                                    }

                                    if (!traversNestedArray.eType.tsymbol.toString().equals(BJsonSchemaUtil
                                            .convertOpenAPITypeToBallerina(traversSchemaNestedArray.getItems().getType()))) {

                                        TypeMismatch validationError = new TypeMismatch(
                                                field.name.getValue().toString(),
                                                convertTypeToEnum(arraySchema.getItems().getType()),
                                                convertTypeToEnum(bArrayType.eType.tsymbol.toString()));
                                        validationErrors.add(validationError);

                                    }
                                }
                            }
                        } else {
//                         Handle the nested record type
                            if (entry.getValue().get$ref() != null) {
                                Schema schema1 = OpenAPISummaryUtil.getOpenAPIComponent(entry.getValue().get$ref());
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
