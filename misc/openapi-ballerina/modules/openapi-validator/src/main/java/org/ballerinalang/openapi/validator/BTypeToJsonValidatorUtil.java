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
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This util class for validate the any given schema with BVarSymbol type.
 */
public  class BTypeToJsonValidatorUtil {

    public static List<ValidationError> validate(Schema schema, BVarSymbol bVarSymbol)
            throws OpenApiValidatorException {

        List<ValidationError> validationErrors = new ArrayList<>();
        Map<String, Schema> properties = null;
        Boolean isExitType = false;
        BType resourceType;
        resourceType = bVarSymbol.getType();

        /**
         *  Here validate the BvarType againts to schema
         */

        if (schema != null) {
            if ((bVarSymbol.type instanceof BRecordType) ||
                    ((schema instanceof ObjectSchema))) {
                if (schema.getProperties() != null) {
                    properties = schema.getProperties();
                }
                if (schema instanceof ObjectSchema) {
                    properties = ((ObjectSchema) schema).getProperties();
                }
//          check the Item type has array type
                BRecordType recordType = null;
                if (resourceType instanceof BArrayType) {
                    BArrayType bArrayTypeBVarSymbol = (BArrayType) resourceType;
                    if (bArrayTypeBVarSymbol.eType instanceof BRecordType) {
                        resourceType = bArrayTypeBVarSymbol.eType;
                        if (resourceType instanceof BRecordType) {
                            recordType = (BRecordType) resourceType;
                        }
                    }
                } else if (resourceType instanceof BRecordType) {
                    recordType = (BRecordType) resourceType;
                }
//          Validate errors in records
                List<ValidationError> recordValidationErrors = new ArrayList<>();
                recordValidationErrors = validateRecord(recordValidationErrors, properties, recordType);
                validationErrors.addAll(recordValidationErrors);
                isExitType = true;

//          Array type validation
            } else if (resourceType.getKind().typeName().equals("[]")
                    && schema.getType().equals("array")) {
                BArrayType bArrayType = null;
                ArraySchema arraySchema = new ArraySchema();
                if (schema instanceof ArraySchema) {
                    arraySchema = (ArraySchema) schema;
                }
                if (resourceType instanceof BArrayType) {
                    bArrayType = (BArrayType) resourceType;
                }
                BArrayType traversNestedArray = bArrayType;
                ArraySchema traversSchemaNestedArray = arraySchema;

//              Handle nested array type

                if (bArrayType != null) {
                    BType bArrayTypeEtype = bArrayType.eType;
                    Schema arraySchemaItems = arraySchema.getItems();
                    if ((bArrayTypeEtype instanceof BArrayType) && (arraySchemaItems instanceof ArraySchema)) {

                        traversNestedArray = (BArrayType) bArrayTypeEtype;
                        traversSchemaNestedArray = (ArraySchema) arraySchemaItems;

                        while ((traversNestedArray.eType instanceof BArrayType) &&
                                (traversSchemaNestedArray.getItems() instanceof ArraySchema)) {
                            Schema<?> traversSchemaNestedArraySchemaType = traversSchemaNestedArray.getItems();
                            if (traversSchemaNestedArraySchemaType instanceof ArraySchema) {
                                traversSchemaNestedArray =
                                        (ArraySchema) traversSchemaNestedArraySchemaType;
                            }
                            BType traversNestedArrayBType = traversNestedArray.eType;
                            if (traversNestedArrayBType instanceof BArrayType) {
                                traversNestedArray = (BArrayType) traversNestedArrayBType;
                            }
                        }
                    }
//                        Handle record type array

                    if ((traversNestedArray.eType instanceof BRecordType) &&
                            traversSchemaNestedArray.
                                    getItems() != null) {
                        if ((traversNestedArray.eType.tsymbol.type instanceof BRecordType) &&
                                traversSchemaNestedArray.getItems() instanceof ObjectSchema) {
                            Schema schema2 = traversSchemaNestedArray.getItems();
                            List<ValidationError> nestedRecordValidation = BTypeToJsonValidatorUtil
                                    .validate(schema2, bVarSymbol);
                            validationErrors.addAll(nestedRecordValidation);
                        }
                    } else if (!traversNestedArray.eType.tsymbol.toString().equals(BTypeToJsonValidatorUtil
                            .convertOpenAPITypeToBallerina(traversSchemaNestedArray.getItems().getType()))) {
                        TypeMismatch validationError = new TypeMismatch(
                                bVarSymbol.name.getValue(),
                                convertTypeToEnum(traversSchemaNestedArray.getItems().getType()),
                                convertTypeToEnum(traversNestedArray.eType.tsymbol.toString()));
                        validationErrors.add(validationError);
                    }
                }
                isExitType = true;

            } else if (resourceType.getKind().typeName().equals("string")
                    && schema.getType().equals("string")) {
                isExitType = true;

            } else if (resourceType.getKind().typeName().equals("int")
                    && schema.getType().equals("integer")) {
                isExitType = true;

            } else if (resourceType.getKind().typeName().equals("boolean")
                    && schema.getType().equals("boolean")) {
                isExitType = true;

            } else if (resourceType.getKind().typeName().equals("boolean")
                    && schema.getType().equals("boolean")) {
                isExitType = true;

            } else if (resourceType.getKind().typeName().equals("boolean")
                    && schema.getType().equals("boolean")) {

                isExitType = true;

            } else if (resourceType instanceof BUnionType) {
//                Handle OneOf type
                BUnionType bUnionType = (BUnionType) resourceType;
                if (schema instanceof ComposedSchema) {
                    ComposedSchema composedSchema = (ComposedSchema) schema;
                    if ((composedSchema.getOneOf() != null) &&
                            (bUnionType.getMemberTypes() != null)) {

                        List<Schema> oneOflist01 = composedSchema.getOneOf();
                        Set<BType> memberList01 = new HashSet<>((bUnionType.getMemberTypes()));
                        List<BType> memberList = new ArrayList<>();
                        List<Schema> oneOflist = new ArrayList<>();
                        memberList.addAll(memberList01);
                        oneOflist.addAll(oneOflist01);

//                  Schema against to ballerina records
                        Iterator<Schema> iterator = oneOflist.iterator();
                        while (iterator.hasNext()) {
                            List<ValidationError> misFieldBallerina = new ArrayList<>();
                            Schema schema1 = iterator.next();
                            Iterator<BType> memberIterator = memberList.iterator();
                            while (memberIterator.hasNext()) {
                                isExitType = true;
                                BType member = memberIterator.next();
                                if (member instanceof BRecordType) {
//                                    Record validation
                                    List<ValidationError> validationErrorListForRecords = new ArrayList<>();
                                    validationErrorListForRecords = validateRecord(validationErrorListForRecords,
                                            schema1.getProperties(),
                                            (BRecordType) member);
                                    if (validationErrorListForRecords.isEmpty()) {
                                        misFieldBallerina.clear();
                                        memberIterator.remove();
                                        iterator.remove();
                                        break;
                                    } else {
//                                        check the given error fields are same as the given schema fields
                                        if (validationErrorListForRecords.stream().
                                                allMatch(item -> item instanceof TypeMismatch)) {
                                            OneOfTypeValidation oneOfTypeValidation =
                                                    new OneOfTypeValidation(getRecordName(member.toString()),
                                                            Constants.Type.RECORD, validationErrorListForRecords);
                                            validationErrors.add(oneOfTypeValidation);
                                        } else if (validationErrorListForRecords.stream().
                                                allMatch(item -> item instanceof MissingFieldInBallerinaType)) {
                                            misFieldBallerina.addAll(validationErrorListForRecords);
                                        }
                                    }
                                } else if (!(member instanceof BAnyType)) {
//                                    Handle primitive data type
                                    isExitType =
                                            member.tsymbol.type.toString().
                                                    equals(convertOpenAPITypeToBallerina(schema1.getType()));
                                    if (isExitType) {
                                        break;
                                    }
                                } else {
//                                    Remove any type value from parameter list
                                    memberIterator.remove();
                                }
                            }
//                      Handle Schema that not in ballerina resource
                            if (!misFieldBallerina.isEmpty()) {
                                if (misFieldBallerina.stream().
                                        allMatch(item -> item instanceof MissingFieldInBallerinaType)) {
                                        OneOfTypeValidation oneOfTypeValidation =
                                                new OneOfTypeValidation("Schema object",
                                                        Constants.Type.OBJECT, misFieldBallerina);
                                        validationErrors.add(oneOfTypeValidation);
                                }
                            }

                            if (!isExitType) {
                                TypeMismatch typeMismatch = new TypeMismatch(bVarSymbol.name.toString(),
                                        convertTypeToEnum(schema1.getType()), null);
                                validationErrors.add(typeMismatch);
                            }
                        }
                        if ((!(oneOflist.isEmpty())) && (memberList.isEmpty())) {
                            for (Schema oneOf : oneOflist) {
                                if (oneOf.getProperties() != null) {
                                    Map<String, Schema> property = oneOf.getProperties();
                                    List<ValidationError> validationErrorsOneOfSchema = new ArrayList<>();
                                    for (Map.Entry<String, Schema> prop: property.entrySet()) {
                                        MissingFieldInBallerinaType missingFieldInBallerinaType =
                                                new MissingFieldInBallerinaType(prop.getKey(),
                                                        convertTypeToEnum(prop.getValue().getType()));
                                        validationErrorsOneOfSchema.add(missingFieldInBallerinaType);
                                    }
                                    OneOfTypeValidation oneOfTypeValidation = new OneOfTypeValidation("OpenApi Schema",
                                            Constants.Type.OBJECT, validationErrorsOneOfSchema);
                                    validationErrors.add(oneOfTypeValidation);
                                }
                            }
                        }

//                       Handle Record against the schema

                        if (!(memberList.isEmpty())) {
                            List<ValidationError> validationErrorsBa =  new ArrayList<>();
                            for (BType member: memberList) {
//                            handle record type
                                if (member instanceof BRecordType) {
                                    isExitType = true;
                                    if (!(oneOflist.isEmpty())) {
                                        Iterator<Schema> oneOfSchema = oneOflist.iterator();
                                        while (oneOfSchema.hasNext()) {
                                            Schema schema2 = oneOfSchema.next();
                                            if (schema2.getProperties() != null) {
                                                validationErrorsBa = validateRecord(validationErrorsBa,
                                                        schema2.getProperties(), (BRecordType) member);
                                            }
                                            if (!(validationErrorsBa.isEmpty())) {
                                                List<String> errorFields = new ArrayList<>();
                                                List<ValidationError> errorValidation = new ArrayList<>();
                                                for (ValidationError validationError: validationErrorsBa) {
                                                    if (validationError instanceof MissingFieldInJsonSchema) {
                                                        errorFields.add(validationError.getFieldName());
                                                        errorValidation.add(validationError);
                                                    }
                                                }
                                                List<String> recordFields = getRecordFields((BRecordType) member);
                                                if (errorFields.containsAll(recordFields)) {
                                                    OneOfTypeValidation oneOfTypeValidation =
                                                            new OneOfTypeValidation(getRecordName(member.toString()),
                                                                    Constants.Type.RECORD, errorValidation);
                                                    validationErrors.add(oneOfTypeValidation);
                                                    validationErrorsBa.clear();
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        List<ValidationError> validationErrorslist1 = new ArrayList<>();
                                        OneOfTypeValidation oneOfTypeValidation =
                                                new OneOfTypeValidation(getRecordName(member.toString())
                                                , Constants.Type.RECORD, validationErrorslist1);
                                        validationErrors.add(oneOfTypeValidation);
                                    }
//                                Handle primitive type
                                } else if (!(member instanceof BAnyType)) {
                                    for (Schema schema2: oneOflist) {
                                        isExitType =
                                                member.tsymbol.type.toString().
                                                        equals(convertOpenAPITypeToBallerina(schema2.getType()));
                                        if (isExitType) {
                                            break;
                                        }
                                    }
                                    if (!isExitType) {
                                        TypeMismatch typeMismatch = new TypeMismatch(bVarSymbol.name.toString(),
                                                null,
                                                convertTypeToEnum(member.getKind().typeName()));
                                        validationErrors.add(typeMismatch);
                                    }
                                } else {
                                    isExitType = true;
                                }
                            }
                            if (!(validationErrorsBa.isEmpty())) {
                                if (validationErrorsBa.stream().
                                        allMatch(item -> item instanceof MissingFieldInJsonSchema)) {
                                    OneOfTypeValidation oneOfTypeValidation =
                                            new OneOfTypeValidation("Ballerina records",
                                                    Constants.Type.RECORD, validationErrorsBa);
                                    validationErrors.add(oneOfTypeValidation);
                                }
                            }
                        }
                    }
                }
                isExitType = true;
            }
        }
        if (!isExitType) {
            TypeMismatch typeMismatch = new TypeMismatch(bVarSymbol.name.toString(),
                    convertTypeToEnum(resourceType.getKind().typeName()),
                    convertTypeToEnum(schema.getType()));
            validationErrors.add(typeMismatch);
        }
        return validationErrors;
    }
//      Record validation function
    private static List<ValidationError> validateRecord(List<ValidationError> validationErrors,
                                                        Map<String, Schema> properties,
                                                        BRecordType recordType) throws OpenApiValidatorException {
//        BType record against the schema
        for (BField field : recordType.fields) {
            boolean isExist = false;
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                if (entry.getKey().equals(field.name.getValue())) {
                    isExist = true;
                    if (entry.getValue().getType() != null) {
                        if (!field.getType().getKind().typeName()
                                .equals(BTypeToJsonValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue()
                                        .getType()))) {

                            TypeMismatch validationError = new TypeMismatch(
                                    field.name.getValue(),
                                    convertTypeToEnum(entry.getValue().getType()),
                                    convertTypeToEnum(field.getType().getKind().typeName()));

                            validationErrors.add(validationError);

                        } else if (entry.getValue() instanceof ObjectSchema) {
//                         Handle the nested record type
                            Schema schemaObject = entry.getValue();
                            if (field.type instanceof BRecordType) {
                                List<ValidationError> nestedRecordValidation = BTypeToJsonValidatorUtil
                                        .validate(schemaObject, field.symbol);
                                validationErrors.addAll(nestedRecordValidation);
                            } else {
//                                    Type mismatch handle
                                TypeMismatch validationError = new TypeMismatch(
                                        field.name.getValue(),
                                        convertTypeToEnum("object"),
                                        convertTypeToEnum(field.getType().getKind().typeName()));
                                validationErrors.add(validationError);
                            }

                        } else {
//                                Handle array type mismatching.
                            if (field.getType().getKind().typeName().equals("[]")) {
                                BArrayType bArrayType = null;
                                Schema entrySchema = entry.getValue();
                                ArraySchema arraySchema = new ArraySchema();
                                if (field.type instanceof  BArrayType) {
                                    bArrayType = (BArrayType) field.type;
                                }
                                if (entrySchema instanceof ArraySchema) {
                                    arraySchema = (ArraySchema) entrySchema;
                                }
                                if (bArrayType != null) {
                                    BArrayType traversNestedArray = bArrayType;
                                    ArraySchema traversSchemaNestedArray = arraySchema;
//                               Handle nested array type
                                    if ((bArrayType.eType instanceof BArrayType) &&
                                            (arraySchema.getItems() instanceof ArraySchema)) {
                                        Schema traversSchemaNestedArraySchemaType = arraySchema.getItems();
                                        traversNestedArray = (BArrayType) bArrayType.eType;
                                        if (traversSchemaNestedArraySchemaType instanceof ArraySchema) {
                                            traversSchemaNestedArray = (ArraySchema) traversSchemaNestedArraySchemaType;
                                        }
                                        while ((traversNestedArray.eType instanceof BArrayType) &&
                                                (traversSchemaNestedArray.getItems() instanceof ArraySchema)) {
                                            Schema<?> traversSchemaNestedArraySchema =
                                                    traversSchemaNestedArray.getItems();
                                            BType traversNestedArrayBtype = traversNestedArray.eType;
                                            if (traversSchemaNestedArraySchema instanceof ArraySchema) {
                                                traversSchemaNestedArray =
                                                        (ArraySchema) traversSchemaNestedArraySchema;
                                            }
                                            if (traversNestedArrayBtype instanceof BArrayType) {
                                                traversNestedArray = (BArrayType) traversNestedArrayBtype;
                                            }
                                        }
                                    }
//                                    Handle record type in item array
                                    if ((traversNestedArray.eType instanceof BRecordType) &&
                                            traversSchemaNestedArray.
                                                    getItems() != null) {
                                        if ((traversNestedArray.eType.tsymbol.type instanceof BRecordType) &&
                                                traversSchemaNestedArray.getItems() instanceof ObjectSchema) {
                                            Schema schema2 = traversSchemaNestedArray.getItems();
                                            BVarSymbol bVarSymbol2 = field.symbol;
                                            List<ValidationError> nestedRecordValidation = BTypeToJsonValidatorUtil
                                                    .validate(schema2, bVarSymbol2);
                                            validationErrors.addAll(nestedRecordValidation);
                                        }
                                    } else if (!traversNestedArray.eType.tsymbol.toString().equals(
                                            BTypeToJsonValidatorUtil
                                            .convertOpenAPITypeToBallerina(
                                                    traversSchemaNestedArray.getItems().getType()))) {

                                        TypeMismatch validationError = new TypeMismatch(
                                                field.name.getValue(),
                                                convertTypeToEnum(traversSchemaNestedArray.getItems().getType()),
                                                convertTypeToEnum(traversNestedArray.eType.tsymbol.toString()));
                                        validationErrors.add(validationError);
                                    }
                                }
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
            for (BField field : recordType.fields) {
                if (field.name.getValue().equals(entry.getKey())) {
                    isExist = true;
//          Handle the type mismatching in above validation
                }
            }
            if (!isExist) {
                MissingFieldInBallerinaType validationError = new MissingFieldInBallerinaType(entry.getKey(),
                        convertTypeToEnum(entry.getValue().getType()));
                validationErrors.add(validationError);
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
                convertedType = Constants.Type.NUMBER;
                break;
            case  "decimal":
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
//  function for return fields of record
    public static List<String> getRecordFields(BRecordType bRecordType) {
        List<String> recordFields = new ArrayList<>();
        for (BField field: bRecordType.getFields()) {
            BType bType = field.getType();
            if (bType instanceof BRecordType) {
                BRecordType recordField = (BRecordType) bType;
                List<String> nestedRecordFields = getRecordFields(recordField);
                recordFields.addAll(nestedRecordFields);

            } else {
                if (field.name != null) {
                    recordFields.add(field.name.toString());
                }
            }
        }
        return recordFields;
    }
//  function for return fields to schema
    public static List<String> getSchemaFields(Schema schema) {
        List<String> jsonFeilds = new ArrayList<>();
        Map<String, Schema> properties = schema.getProperties();
        for (Map.Entry<String, Schema> schemaEntry: properties.entrySet()) {
            Schema entrySchema = schemaEntry.getValue();
            if (entrySchema instanceof ObjectSchema) {
                ObjectSchema objectSchema = (ObjectSchema) entrySchema;
                List<String> nestedObjectSchema = getSchemaFields(objectSchema);
                jsonFeilds.addAll(nestedObjectSchema);
            } else {
                jsonFeilds.add(schemaEntry.getKey());
            }
        }
        return jsonFeilds;
    }

//    get record name
    public static String getRecordName(String ref) {
        String recordName = null;
        if (ref != null) {
            String[] splitRef = ref.split(":");
            recordName = splitRef[splitRef.length - 1];
        }
        return recordName;
    }

}
