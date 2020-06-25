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
import io.swagger.v3.oas.models.parameters.Parameter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public  class BJsonSchemaUtil {
    private static OpenAPIComponentSummary openAPIComponentSummary;

    BJsonSchemaUtil() {
        this.openAPIComponentSummary = null;
    }

//    public static Map<String , List<String>> validateBallerinaType(Schema openapiSchema, BVarSymbol Ballerinatype, String validatorAgainst,
//                                                                            Map<String , List<String>> validationError){
//
////        Map<String , List<String>> validationError = new HashMap<String, List<String>>();
//        List<String> typeMismatch = new ArrayList<>();
//        List<String> jsonFieldMiss = new ArrayList<>();
//        List<String> ballerinaFieldMiss = new ArrayList<>();
//        Map<String, Schema> properties = null;
//
//        if (openapiSchema instanceof Schema) {
//            if (openapiSchema.getProperties() != null) {
//                properties = openapiSchema.getProperties();
//            } else {
//                String schema_ref = openapiSchema.get$ref();
//                properties = openAPIComponentSummary.getSchema(getcomponetName(schema_ref)).getProperties();
//            }
//        }
//        if (openapiSchema instanceof ObjectSchema) {
//            properties = ((ObjectSchema) openapiSchema).getProperties();
//        }
//
//        BType resourceType = Ballerinatype.getType();
//        BRecordType recordType = (BRecordType) resourceType;
//
//        switch (validatorAgainst) {
////            validate Resource against OpenApi
//            case ("RAO"):
//                for (BField field: recordType.fields) {
//                    boolean isExist = false;
//                    boolean isTypeMatch = false;
//                    for (Map.Entry<String, Schema> entry : properties.entrySet()) {
//                        if (entry.getKey().equals(field.name.getValue())) {
//                            isExist = true;
//                            if (entry.getValue().getType()!= null){
//                                if (!field.getType().getKind().typeName()
//                                        .equals(BJsonSchemaUtil.convertOpenAPITypeToBallerina(entry.getValue()
//                                                .getType()))){
//                                    if (validationError.containsKey("TM")){
//                                        validationError.get("TM").add(field.name.toString());
//                                    } else {
//                                        typeMismatch.add(field.name.toString());
//                                    }
//                                }
//                            } else {
////                                BType nestedRecord = field.getType();
////                                BRecordType nestedRecordType = (BRecordType) nestedRecord;
//                                BJsonSchemaUtil.validateBallerinaType(entry.getValue(),field.symbol,"RAO", validationError);
//
//                            }
//
//                        }
//                    }
//                    if (!isExist) {
//                        if (validationError.containsKey("RAO")) {
//                            validationError.get("RAO").add(field.name.toString());
//                        } else {
//                            ballerinaFieldMiss.add(field.name.toString());
//                        }
//                    }
//                }
//                if (!ballerinaFieldMiss.isEmpty()) {
//                    validationError.put("RAO", ballerinaFieldMiss);
//                }
//                if (!typeMismatch.isEmpty()) {
//                    validationError.put("TM", typeMismatch);
//                }
//                return validationError;
//
//            case ("OAR"):
//                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
//                    boolean isExist = false;
//                    for (BField field: recordType.fields.values()) {
//                        if (field.name.getValue().equals(entry.getKey())) {
//                            isExist=true;
//                            if (!field.getType().getKind().typeName()
//                                    .equals(BJsonSchemaUtil.convertOpenAPITypeToBallerina(entry.getValue()
//                                            .getType()))){
//                                typeMismatch.add(field.name.toString());
//                            }
//                        }
//                    }
//                    if (!isExist) {
//                        jsonFieldMiss.add(entry.getKey());
//                    }
//                }
//                if (!jsonFieldMiss.isEmpty()) {
//                    validationError.put("OAR", ballerinaFieldMiss);
//                }
//                if (!typeMismatch.isEmpty()) {
//                    validationError.put("TM", typeMismatch);
//                }
//                return validationError;
//            default:
//                return validationError;
//        }
//    }

    public static List<ValidationError> validateBallerinaType(Parameter parameter, BVarSymbol bVarSymbol) {
        Schema schema = new Schema();
        schema = null;
        return validateBallerinaType(schema, parameter, bVarSymbol );
    }
    public static List<ValidationError> validateBallerinaType(Schema schema, BVarSymbol bVarSymbol) {
//        List<Parameter> parameters = new ArrayList<>();
        Parameter parameter = new Parameter();
        parameter = null;
        return validateBallerinaType(schema,  parameter, bVarSymbol);
    }
//List<Parameter> parameters or one parameter
    public static List<ValidationError> validateBallerinaType(Schema schema,  Parameter parameter, BVarSymbol bVarSymbol) {
        List<ValidationError> validationErrors = new ArrayList<>();
        Map<String, Schema> properties = null;


        /** Handle path parameters list
         *
         */
//        if (!parameters.isEmpty()) {
//            for (Parameter parameter: parameters) {
//                boolean isExist = false;
//                if (parameter.getName().equals(bVarSymbol.name.getValue())) {
//                    isExist = true;
//                    if (!convertOpenAPITypeToBallerina(parameter.getSchema().getType()).equals(bVarSymbol.getType().toString())) {
//                        isExist = false;
//                    }
//                }
//
//            }
//
//        }
//        one by one parameter validate
        if (parameter != null) {
            if (parameter.getName().equals(bVarSymbol.name.getValue())) {
                if (!convertOpenAPITypeToBallerina(parameter.getSchema().getType()).equals(bVarSymbol.getType().toString())) {
                    ValidationError validationError = new ValidationError(parameter.getName().toString(),
                            convertTypeToEnum(parameter.getSchema().getType().toString()));
                    validationErrors.add(validationError);
                }
            } else {
                ValidationError validationError = new ValidationError(parameter.getName().toString(),
                        convertTypeToEnum(parameter.getSchema().getType().toString()));
                validationErrors.add(validationError);
            }
        }

        /** Handle the body parameter with records
         *  Here validate the BvarType againts to schema
         */
//        Boolean tag = false;

        if (schema != null) {
            if (schema instanceof Schema) {
                if (schema.getProperties() != null) {
                    properties = schema.getProperties();
                } else {
                    String schema_ref = schema.get$ref();
                    properties = openAPIComponentSummary.getSchema(getcomponetName(schema_ref)).getProperties();
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
                                ValidationError validationError = new ValidationError(
                                        field.name.getValue().toString(),
                                        convertTypeToEnum(entry.getValue().getType()));
                                validationErrors.add(validationError);

                            }
                        } else {
//                        TO-DO handle the nexted record
//                                BType nestedRecord = field.getType();
//                                BRecordType nestedRecordType = (BRecordType) nestedRecord;
//                        BJsonSchemaUtil.validateBallerinaType(entry.getValue(), field.symbol, "RAO", validationError);
                        }
                    }
                }
                if (!isExist) {
                    ValidationError validationError = new ValidationError(field.name.toString(),
                            convertTypeToEnum(field.getType().getKind().typeName()));
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

    private static String getcomponetName(String ref) {
        String componentName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }
}