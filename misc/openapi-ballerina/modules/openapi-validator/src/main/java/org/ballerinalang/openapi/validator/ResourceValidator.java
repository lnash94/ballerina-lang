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
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.ballerinalang.openapi.validator.error.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ResourceValidator {

    public static List<ValidationError> validateWhatMissingResource(Operation operation, ResourceMethod resourceMethod)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrors = new ArrayList<>();
        if (resourceMethod.getParamNames() != null) {

            for (ResourceParameter resourceParameter: resourceMethod.getParamNames()) {
                Boolean isParameterExit = false;
                //            Request body handling
                if ((resourceMethod.getBody() != null) && (resourceMethod.getBody().equals(resourceParameter.getName()))
                        && (operation.getRequestBody() != null)) {
                    if (operation.getRequestBody() != null) {
                        RequestBody requestBody = operation.getRequestBody();
                        if (requestBody.getContent() != null) {
                            Map<String, Schema> requestBodySchemas = getOperationRequestBody(operation);
                            if (!requestBodySchemas.isEmpty()) {

                                for (Map.Entry<String, Schema> requestBodyOperation: requestBodySchemas.entrySet()) {
//                                    System.out.println(requestBodyOperation.getValue());
                                    List<ValidationError> requestBValidationError  =
                                            BTypeToJsonValidatorUtil.validate(requestBodyOperation.getValue(),
                                                    resourceParameter.getParameter().symbol);

                                    if (requestBValidationError.isEmpty()) {
                                        isParameterExit = true;
                                        break;
                                    } else {
//                                        if (resourceParameter.getParameter().type instanceof BRecordType) {
//                                            List<String> list =
//                                                    BTypeToJsonValidatorUtil.getRecordFields(
//                                                            (BRecordType) resourceParameter.getParameter().type);
//                                            List<String> errorList = new ArrayList<>();
                                            for (ValidationError validationError: requestBValidationError) {
                                                if ((validationError instanceof TypeMismatch) ||
                                                        (validationError instanceof MissingFieldInJsonSchema) ||
                                                        (validationError instanceof OneOfTypeValidation)) {
//                                                    errorList.add(validationError.getFieldName());
                                                    validationErrors.add(validationError);
                                                }
                                            }
//                                            if (list.containsAll(errorList)) {
//                                                validationErrors.addAll(requestBValidationError);
//                                                isParameterExit = true;
//                                                break;
//                                            }
                                            isParameterExit = true;
                                            break;

//                                        }
                                    }
                                }
                            }
                        }
                    }
                    //                    Handle Path parameter
                } else if (operation.getParameters() != null) {
                    for (Parameter parameter : operation.getParameters()) {
                        if (resourceParameter.getName().equals(parameter.getName())) {
                            List<ValidationError> validationErrorsResource = new ArrayList<>();
                            if (parameter.getSchema() != null) {
                                isParameterExit = true;
                                validationErrorsResource = BTypeToJsonValidatorUtil.validate(parameter.getSchema(),
                                        resourceParameter.getParameter().symbol);
                                if (!validationErrorsResource.isEmpty()) {
                                    validationErrors.addAll(validationErrorsResource);
                                }
                                break;
                            }
                        }
                    }
                }
                if (!isParameterExit) {
                    ValidationError validationError = new ValidationError(resourceParameter.getName(),
                            BTypeToJsonValidatorUtil.convertTypeToEnum(resourceParameter.getType()));
                    validationErrors.add(validationError);
                }
            }
        }
        return validationErrors;
    }

//    get the requestBody parameter form operation
    public static Map<String, Schema> getOperationRequestBody(Operation operation) {

        Map<String, Schema> requestBodySchemas = new HashMap<>();
        Content content = operation.getRequestBody().getContent();
        for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
            requestBodySchemas.put(mediaTypeEntry.getKey(), mediaTypeEntry.getValue().getSchema());
        }
        return requestBodySchemas;
    }

    /**
     *
     * @param operation
     * @param resourceMethod
     * @return
     * @throws OpenApiValidatorException
     */

    public static List<ValidationError> validateWhatMissService(Operation operation, ResourceMethod resourceMethod)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrorList = new ArrayList<>();
        Boolean isOParamExit = false;
//        handle path , query paramters
        if (operation.getParameters() != null) {
            List<Parameter> operationParam = operation.getParameters();
            for (Parameter param : operationParam) {
                isOParamExit = false;
                if (!resourceMethod.getParamNames().isEmpty()) {
                    for (ResourceParameter resourceParam: resourceMethod.getParamNames()) {
                        if (param.getName().equals(resourceParam.getName())) {
                            isOParamExit = true;
                            List<ValidationError> validationErrors =
                                    BTypeToJsonValidatorUtil.validate(param.getSchema(),
                                            resourceParam.getParameter().symbol);
                            if (!validationErrors.isEmpty()) {
                                validationErrorList.addAll(validationErrors);
                            }
                            break;
                        }
                    }
                }
                if (!isOParamExit) {
                    MissingFieldInBallerinaType validationError = new MissingFieldInBallerinaType(param.getName(),
                            BTypeToJsonValidatorUtil.convertTypeToEnum(param.getSchema().getType()));
                    validationErrorList.add(validationError);
                }
            }
        }
//        handle the requestBody
        if (operation.getRequestBody() != null) {

            List<ResourceParameter> resourceParam = resourceMethod.getParamNames();
            Map<String, Schema> requestBodySchemas = ResourceValidator.getOperationRequestBody(operation);
            for (Map.Entry<String, Schema> operationRB: requestBodySchemas.entrySet()) {
                isOParamExit = false;
                if (!resourceParam.isEmpty()){
                    for (ResourceParameter resourceParameter : resourceParam) {
                        if (resourceMethod.getBody().equals(resourceParameter.getName())) {
                            List<ValidationError> validationErrors =
                                    BTypeToJsonValidatorUtil.validate(operationRB.getValue(),
                                            resourceParameter.getParameter().symbol);
                            if (validationErrors.isEmpty()) {
                                isOParamExit = true;
                            } else {
                                List<String> errorFields = new ArrayList<>();
                                for (ValidationError validEr: validationErrors) {
                                    if ((validEr instanceof MissingFieldInBallerinaType) ||
                                            (validEr instanceof OneOfTypeValidation) || (validEr instanceof TypeMismatch)) {
                                        validationErrorList.add(validEr);
                                        isOParamExit = true;
                                        break;
                                    }
//                                    if (validEr.getFieldName() != null) {
//                                        if (validEr instanceof OneOfTypeValidation) {
//                                            OneOfTypeValidation oneOf = (OneOfTypeValidation) validEr;
//                                            if (!oneOf.getBlockErrors().isEmpty()) {
//                                                for (ValidationError vE : oneOf.getBlockErrors()) {
//                                                    if (vE.getFieldName() != null) {
//                                                        errorFields.add(vE.getFieldName());
//                                                    }
//                                                }
//                                            }
//                                        } else {
//                                            errorFields.add(validEr.getFieldName());
//                                        }
//                                    }
                                }
//                                List<String> schemaFields =
//                                        BTypeToJsonValidatorUtil.getSchemaFields(operationRB.getValue());
//                                if (schemaFields.containsAll(errorFields)) {
//                                    isOParamExit = true;
//                                    validationErrorList.addAll(validationErrors);
//                                    break;
//                                }
                            }
                        }
                    }
                }
                if (!isOParamExit) {
                    String type = "";
                    if (operationRB.getValue().getType() == null && (operationRB.getValue().getProperties() != null)) {
                        type = "object";
                    } else {
                        type = operationRB.getValue().getType();
                    }
                    ValidationError validationError = new ValidationError(operationRB.getKey(),
                            BTypeToJsonValidatorUtil.convertTypeToEnum(type));
                    validationErrorList.add(validationError);
                }
            }
        }
        return validationErrorList;
    }

}
