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

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.ballerinalang.openapi.validator.error.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OperationToResourceFunction {

    public static List<ValidationError> validate(Operation operation, ResourceMethod resourceMethod)
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
                if (!resourceParam.isEmpty()) {
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
                                    if (validEr.getFieldName() != null) {
                                        if (validEr instanceof OneOfTypeValidation) {
                                            OneOfTypeValidation oneOf = (OneOfTypeValidation) validEr;
                                            if (!oneOf.getBlockErrors().isEmpty()) {
                                                for (ValidationError vE : oneOf.getBlockErrors()) {
                                                    if (vE.getFieldName() != null) {
                                                        errorFields.add(vE.getFieldName());
                                                    }
                                                }
                                            }
                                        } else {
                                            errorFields.add(validEr.getFieldName());
                                        }
                                    }
                                }
                                List<String> schemaFields =
                                        BTypeToJsonValidatorUtil.getSchemaFields(operationRB.getValue());
                                if (schemaFields.containsAll(errorFields)) {
                                    isOParamExit = true;
                                    validationErrorList.addAll(validationErrors);
                                    break;
                                }
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
