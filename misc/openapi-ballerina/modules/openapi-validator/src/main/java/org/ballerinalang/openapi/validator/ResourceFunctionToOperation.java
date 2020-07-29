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
import io.swagger.v3.oas.models.parameters.Parameter;
import org.ballerinalang.model.tree.FunctionNode;

import java.util.ArrayList;
import java.util.List;

public class ResourceFunctionToOperation {

    public static List<ValidationError> validate(Operation operation, ResourceMethod resourceMethod)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrors = new ArrayList<>();
        if (resourceMethod.getParamNames() != null) {
            for (ResourceParameter resourceParameter: resourceMethod.getParamNames()) {
                Boolean isParamterExit = false;
                if (operation.getParameters() != null) {
                    for (Parameter parameter : operation.getParameters()) {
                        if (resourceParameter.getName().equals(parameter.getName())) {
                            List<ValidationError> validationErrorsResource = new ArrayList<>();
                            if (parameter.getSchema() != null) {
                                isParamterExit = true;
                                validationErrorsResource = BTypeToJsonValidatorUtil.validate(parameter.getSchema(),
                                        resourceParameter.getParameter().symbol);
                                if (!validationErrorsResource.isEmpty()) {
                                    validationErrors.addAll(validationErrorsResource);
//                                    for (ValidationError validationError: validationErrorsResource) {
//                                        if (validationError instanceof TypeMismatch) {
//
//
//                                        } else if (validationError instanceof MissingFieldInJsonSchema) {
//
//                                        }
//                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (!isParamterExit) {
                        ValidationError validationError = new ValidationError(resourceParameter.getName(),
                                BTypeToJsonValidatorUtil.convertTypeToEnum(resourceParameter.getType()));
                        validationErrors.add(validationError);
                    }
                }
            }
        }
        return validationErrors;
    }

}
