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
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.ResourceValidationError;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ResourceValidation {

    /**
     *
     * @param openApi
     * @param serviceNode
     * @param tags
     * @param operations
     * @param excludeTags
     * @param excludeOperations
     * @param kind
     * @param dLog
     * @throws OpenApiValidatorException
     */
    public static void validateResource(OpenAPI openApi,
                                        ServiceNode serviceNode,
                                        List<String> tags,
                                        List<String> operations,
                                        List<String> excludeTags,
                                        List<String> excludeOperations,
                                        Diagnostic.Kind kind,
                                        DiagnosticLog dLog) throws OpenApiValidatorException {

        boolean tagFilteringEnabled = tags.size() > 0;
        boolean operationFilteringEnabled = operations.size() > 0;
        boolean excludeTagsFilteringEnabled = excludeTags.size() > 0;
        boolean excludeOperationFilterEnable = excludeOperations.size() > 0;


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
//        Check with open api  contract
        Paths openAPIPathSummary = openApi.getPaths();
        for (ResourcePathSummary resourcePathSummary : resourcePathSummaryList) {
            for (Map.Entry<String, PathItem> openApiPath : openAPIPathSummary.entrySet()) {
                if (resourcePathSummary.getPath().equals(openApiPath.getKey())) {
                    if (!resourcePathSummary.getMethods().isEmpty()) {
                        Map<String, ResourceMethod> resourceMethods = resourcePathSummary.getMethods();
                        for (Map.Entry<String, ResourceMethod> method: resourceMethods.entrySet()) {
                            if (method.getKey().equals("get")) {
                                if (openApiPath.getValue().getGet() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getGet(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            } else if (method.getKey().equals("post")) {
                                if (openApiPath.getValue().getPost() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getPost(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            } else if (method.getKey().equals("put")) {
                                if (openApiPath.getValue().getPut() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getPut(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            } else if (method.getKey().equals("delete")) {
                                if (openApiPath.getValue().getDelete() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getDelete(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            } else if (method.getKey().equals("patch")) {
                                if (openApiPath.getValue().getPatch() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getPatch(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            } else if (method.getKey().equals("head")) {
                                if (openApiPath.getValue().getHead() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getHead(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
                                }
                            } else if (method.getKey().equals("options")) {
                                if (openApiPath.getValue().getOptions() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getOptions(),
                                                    method.getValue());
                                    generateDlogMessage(kind, dLog, resourcePathSummary, method, postErrors);
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
