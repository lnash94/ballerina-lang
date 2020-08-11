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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.openapi.validator.error.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.ResourceValidationError;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceValidator {

    /**
     *
     * @param openApi
     * @param serviceNode
     * @param kind
     * @param dLog
     * @throws OpenApiValidatorException
     */
    public static void validateResource(OpenAPI openApi,
                                        ServiceNode serviceNode,
                                        Filters filters,
                                        Diagnostic.Kind kind,
                                        DiagnosticLog dLog) throws OpenApiValidatorException {
//        filter openApi operation according to given filters
        List<OpenAPIPathSummary> openAPIPathSummaries = MatchResourcewithOperationId.filterOpenapi(openApi, filters);
//        Check all the filtered operations a available at service file
        List<ResourceValidationError> resourceMissingPathMethod =
                MatchResourcewithOperationId.checkResourceIsAvailable(openApi, serviceNode);

        if (!resourceMissingPathMethod.isEmpty()) {
            for (ResourceValidationError resourceValidationError: resourceMissingPathMethod) {
//                     Handling the Missing path in openApi contract
                if (resourceValidationError.getresourceMethod() == null) {
                    dLog.logDiagnostic(kind, resourceValidationError.getPosition(),
                            ErrorMessages.undocumentedResourcePath(resourceValidationError.getResourcePath()));
                } else {
//                    Handle undocumented method in contract
                    dLog.logDiagnostic(kind, resourceValidationError.getPosition(),
                            ErrorMessages.undocumentedResourceMethods(resourceValidationError.getresourceMethod(),
                                    resourceValidationError.getResourcePath()));
                }
            }
        }
//        Store the Resource Path that need to validate
        List<ResourcePathSummary> resourcePathSummaryList =
                MatchResourcewithOperationId.summarizeResources(serviceNode);

        if (!resourcePathSummaryList.isEmpty()) {
            Iterator<ResourcePathSummary> resourcePSIterator = resourcePathSummaryList.iterator();
            while (resourcePSIterator.hasNext()) {
                ResourcePathSummary resourcePathSummary = resourcePSIterator.next();
                if (!resourceMissingPathMethod.isEmpty()) {
                    for (ResourceValidationError resourceValidationError: resourceMissingPathMethod) {
                        if (resourcePathSummary.getPath().equals(resourceValidationError.getResourcePath())) {
                            if ((!resourcePathSummary.getMethods().isEmpty())
                                    && (resourceValidationError.getresourceMethod() != null)) {
                                Map<String, ResourceMethod> resourceMethods = resourcePathSummary.getMethods();
                                resourceMethods.entrySet().removeIf(resourceMethod -> resourceMethod.getKey()
                                        .equals(resourceValidationError.getresourceMethod()));
                            } else if (resourceValidationError.getresourceMethod() == null) {
                                resourcePSIterator.remove();
                            }
                        }
                    }
                }
            }
        }
        if (!openAPIPathSummaries.isEmpty()) {
            Iterator<ResourcePathSummary> resourcePathSummaryIterator = resourcePathSummaryList.iterator();
            while (resourcePathSummaryIterator.hasNext()) {
                Boolean isExit = false;
                ResourcePathSummary resourcePathSummary = resourcePathSummaryIterator.next();
                for (OpenAPIPathSummary apiPathSummary: openAPIPathSummaries) {
                    if (resourcePathSummary.getPath().equals(apiPathSummary.getPath())) {
                        isExit = true;
                        if (!(resourcePathSummary.getMethods().isEmpty()) && !(apiPathSummary.getOperations().isEmpty())) {
                            Iterator<Map.Entry<String, ResourceMethod>> methods =
                                    resourcePathSummary.getMethods().entrySet().iterator();
                            while (methods.hasNext()) {
                                Boolean isMethodExit = false;
                                Map.Entry<String, ResourceMethod> re_methods = methods.next();
                                Map<String, Operation> operations = apiPathSummary.getOperations();
                                for (Map.Entry<String, Operation> operation: operations.entrySet()) {
                                    if (re_methods.getKey().equals(operation.getKey())) {
                                        isMethodExit = true;
                                        break;
                                    }
                                }
                                if (!isMethodExit) {
                                    methods.remove();
                                }
                            }
                        }
                        break;
                    }
                }
                if (!isExit) {
                    resourcePathSummaryIterator.remove();
                }
            }

        }

//        Check with open api  contract
//        Paths openAPIPathSummary = openApi.getPaths();
        for (ResourcePathSummary resourcePathSummary : resourcePathSummaryList) {
            for (OpenAPIPathSummary openApiPath : openAPIPathSummaries) {
                if (resourcePathSummary.getPath().equals(openApiPath.getPath())) {
                    if (!resourcePathSummary.getMethods().isEmpty()) {
                        Map<String, ResourceMethod> resourceMethods = resourcePathSummary.getMethods();
                        for (Map.Entry<String, ResourceMethod> method: resourceMethods.entrySet()) {
                            for (Map.Entry<String, Operation> operation: openApiPath.getOperations().entrySet()) {
                                if (method.getKey().equals(operation.getKey())) {
                                    List<ValidationError> postErrors =
                                            ResourceValidator.validateWhatMissingResource(operation.getValue(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            }
                        }
                    }
                }
            }
        }
//        wise versa open api against to service
        for (OpenAPIPathSummary openAPIPathSummary: openAPIPathSummaries) {
            for (ResourcePathSummary resourcePathSummary: resourcePathSummaryList) {
                if (openAPIPathSummary.getPath().equals(resourcePathSummary.getPath())) {
                    if (!openAPIPathSummary.getOperations().isEmpty()) {
                        if (!resourcePathSummary.getMethods().isEmpty()) {
                            Map<String, Operation> operations = openAPIPathSummary.getOperations();
                            for (Map.Entry<String, Operation> operation : operations.entrySet()) {
                                Map<String, ResourceMethod> methods = resourcePathSummary.getMethods();
                                for (Map.Entry<String, ResourceMethod> method: methods.entrySet()) {
                                    if (operation.getKey().equals(method.getKey())) {
                                        List<ValidationError> errorList =
                                                ResourceValidator.validateWhatMissService(operation.getValue(),
                                                        method.getValue());
                                        if (!errorList.isEmpty()) {
                                            for (ValidationError error: errorList) {
                                                if (error instanceof MissingFieldInBallerinaType) {
                                                    dLog.logDiagnostic(kind, serviceNode.getPosition(),
                                                            ErrorMessages.unimplementedFieldInOperation(
                                                                    error.getFieldName(),
                                                                    ((MissingFieldInBallerinaType) error)
                                                                            .getRecordName(), operation.getKey(),
                                                                    openAPIPathSummary.getPath()));

                                                } else if (error instanceof OneOfTypeValidation) {
                                                    if (!((OneOfTypeValidation) error).getBlockErrors().isEmpty()) {
                                                        List<ValidationError> oneOfErrors =
                                                                ((OneOfTypeValidation) error).getBlockErrors();
                                                        for (ValidationError oneOf : oneOfErrors) {
                                                            if (oneOf instanceof MissingFieldInBallerinaType) {
                                                                dLog.logDiagnostic(kind, serviceNode.getPosition(),
                                                                        ErrorMessages.unimplementedFieldInOperation(
                                                                                oneOf.getFieldName(),
                                                                                ((MissingFieldInBallerinaType) oneOf)
                                                                                        .getRecordName(), operation.
                                                                                        getKey(), openAPIPathSummary.
                                                                                        getPath()));
                                                            }
                                                        }
                                                    }

                                                } else if (error instanceof ValidationError) {
                                                    dLog.logDiagnostic(kind, serviceNode.getPosition(),
                                                            ErrorMessages.unimplementedParameterForOperation(
                                                                    error.getFieldName(),
                                                                    operation.getKey(), openAPIPathSummary.getPath()));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param kind
     * @param dLog
     * @param resourcePathSummary
     * @param method
     * @param postErrors
     */
    private static void generateDlogMessage(Diagnostic.Kind kind, DiagnosticLog dLog,
                                            ResourcePathSummary resourcePathSummary,
                                            Map.Entry<String, ResourceMethod> method,
                                            List<ValidationError> postErrors) {

        if (!postErrors.isEmpty()) {
            for (ValidationError postErr : postErrors) {
                if (postErr instanceof TypeMismatch) {
                    generateTypeMisMatchDlog(kind, dLog, resourcePathSummary, method, postErr);
                } else if (postErr instanceof MissingFieldInJsonSchema) {
                    dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                            ErrorMessages.undocumentedFieldInRecordParam(postErr.getFieldName(),
                                    ((MissingFieldInJsonSchema) postErr).getRecordName(),
                                    method.getKey(), resourcePathSummary.getPath()));
                } else if (postErr instanceof OneOfTypeValidation) {
                    if (!(((OneOfTypeValidation) postErr).getBlockErrors()).isEmpty()) {
                        List<ValidationError> oneOferrorlist =
                                ((OneOfTypeValidation) postErr).getBlockErrors();
                        for (ValidationError oneOfvalidation : oneOferrorlist) {
                            if (oneOfvalidation instanceof TypeMismatch) {
                                generateTypeMisMatchDlog(kind, dLog, resourcePathSummary, method, oneOfvalidation);

                            } else if (oneOfvalidation instanceof MissingFieldInJsonSchema) {
                                dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                                        ErrorMessages.undocumentedFieldInRecordParam(oneOfvalidation.getFieldName(),
                                                ((MissingFieldInJsonSchema) oneOfvalidation).getRecordName(),
                                                method.getKey(), resourcePathSummary.getPath()));
                            }
                        }
                    }
                } else if (postErr instanceof ValidationError) {
                    dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                            ErrorMessages.undocumentedResourceParameter(postErr.getFieldName(),
                                    method.getKey(), resourcePathSummary.getPath()));
                }
            }
        }
    }

    /**
     *
     * @param kind
     * @param dLog
     * @param resourcePathSummary
     * @param method
     * @param postErr
     */
    private static void generateTypeMisMatchDlog(Diagnostic.Kind kind, DiagnosticLog dLog,
                                                 ResourcePathSummary resourcePathSummary,
                                                 Map.Entry<String, ResourceMethod> method, ValidationError postErr) {

        if (((TypeMismatch) postErr).getRecordName() != null) {
            dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                    ErrorMessages.typeMismatchingRecord(postErr.getFieldName(),
                            ((TypeMismatch) postErr).getRecordName(),
                            BTypeToJsonValidatorUtil.convertEnumTypetoString((
                                    (TypeMismatch) postErr).getTypeJsonSchema()),
                            BTypeToJsonValidatorUtil.convertEnumTypetoString(((
                                    TypeMismatch) postErr).getTypeBallerinaType())
                            , method.getKey(), resourcePathSummary.getPath()));

        } else {
            dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                    ErrorMessages.typeMismatching(postErr.getFieldName(),
                            BTypeToJsonValidatorUtil.convertEnumTypetoString
                                    (((TypeMismatch) postErr).getTypeJsonSchema()),
                            BTypeToJsonValidatorUtil.convertEnumTypetoString
                                    (((TypeMismatch) postErr).getTypeBallerinaType())
                            , method.getKey(), resourcePathSummary.getPath()));
        }
    }
}
