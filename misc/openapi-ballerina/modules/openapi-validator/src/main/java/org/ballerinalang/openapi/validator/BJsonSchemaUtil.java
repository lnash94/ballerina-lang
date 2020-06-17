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

import io.swagger.v3.oas.models.media.Schema;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

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

    private static String getcomponetName(String ref){
        String componentName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }

    public static boolean validateBallerinaType(BVarSymbol bVarSymbol){
        ValidationError validationError = new ValidationError();
//        if (schema instanceof Schema) {
//            return true;
//        }
//        , BTypeSymbol bTypeSymbol
        if(bVarSymbol instanceof BVarSymbol) {
            return true;
        }

        return false;
    }


}
