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
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ResourceValidation {

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
//                        Handling the Missing path in openApi contract
                if (resourceValidationError.getresourceMethod() == null) {
//                    System.out.println(ErrorMessages.undocumentedResourcePath(resourceValidationError.getResourcePath()));
                    dLog.logDiagnostic(kind, resourceValidationError.getPosition(),
                            ErrorMessages.undocumentedResourcePath(resourceValidationError.getResourcePath()));
                } else {
//                    Handle undocumented method in contract
//                    System.out.println(ErrorMessages.undocumentedResourceMethods(resourceValidationError.getresourceMethod(),
//                            resourceValidationError.getResourcePath()));
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
                                    && (resourceValidationError.resourceMethod != null)) {
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
//        Get api operations
        Paths openAPIPathSummary = openApi.getPaths();
        for (ResourcePathSummary resourcePathSummary : resourcePathSummaryList) {
            for (Map.Entry<String, PathItem> openApiPath : openAPIPathSummary.entrySet()) {
                if (resourcePathSummary.getPath().equals(openApiPath.getKey())) {
                    if (!resourcePathSummary.getMethods().isEmpty()) {
                        Map<String, ResourceMethod> resourceMethods = resourcePathSummary.getMethods();
                        for (Map.Entry<String, ResourceMethod> method: resourceMethods.entrySet()) {
                            if (method.getKey().equals("get")) {
                                if (openApiPath.getValue().getGet() != null) {
                                    List<ValidationError> getErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getGet(),
                                                    method.getValue());
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getPost(),
                                                    method.getValue());
                                    if (!getErrors.isEmpty()) {
                                        for (ValidationError getErr : getErrors) {
                                            if (getErr instanceof TypeMismatch) {
//                                              message
                                                if (((TypeMismatch) getErr).getRecordName() != null) {
                                                      dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                                                            ErrorMessages.typeMismatchingRecord(getErr.getFieldName(),
                                                                    ((TypeMismatch) getErr).getRecordName(),
                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString((
                                                                            (TypeMismatch)getErr).getTypeJsonSchema()),
                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString(((
                                                                            TypeMismatch)getErr).getTypeBallerinaType())
                                                                    , method.getKey(), resourcePathSummary.getPath()));

                                                } else {
                                                      dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
                                                            ErrorMessages.typeMismatching(getErr.getFieldName(),
                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString
                                                                            (((TypeMismatch) getErr).getTypeJsonSchema()),
                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString
                                                                            (((TypeMismatch) getErr).getTypeBallerinaType())
                                                                    , method.getKey(), resourcePathSummary.getPath()));
                                                }

                                            } else if (getErr instanceof MissingFieldInJsonSchema) {

//                                              message
                                            } else if (getErr instanceof OneOfTypeValidation) {
                                                if (!(((OneOfTypeValidation) getErr).getBlockErrors()).isEmpty()) {

                                                    List<ValidationError> oneOferrorlist =
                                                            ((OneOfTypeValidation) getErr).getBlockErrors();

                                                    for (ValidationError oneOfvalidation : oneOferrorlist ) {
                                                        if (oneOferrorlist instanceof TypeMismatch) {

                                                        } else if (oneOferrorlist instanceof MissingFieldInJsonSchema) {

                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (method.getKey().equals("post")) {
                                if (openApiPath.getValue().getPost() != null) {
                                    List<ValidationError> postErrors =
                                            ResourceFunctionToOperation.validate(openApiPath.getValue().getPost(),
                                                    method.getValue());
                                    if (!postErrors.isEmpty()) {
                                        for (ValidationError postErr : postErrors) {
                                            if (postErr instanceof TypeMismatch) {
//                                              message
                                                if (((TypeMismatch) postErr).getRecordName() != null) {
                                                    System.out.println(ErrorMessages.typeMismatchingRecord(postErr.getFieldName(),
                                                            ((TypeMismatch) postErr).getRecordName(),
                                                            BTypeToJsonValidatorUtil.convertEnumTypetoString((
                                                                    (TypeMismatch)postErr).getTypeJsonSchema()),
                                                            BTypeToJsonValidatorUtil.convertEnumTypetoString(((
                                                                    TypeMismatch)postErr).getTypeBallerinaType())
                                                            , method.getKey(), resourcePathSummary.getPath()));
//                                                    dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
//                                                            ErrorMessages.typeMismatchingRecord(getErr.getFieldName(),
//                                                                    ((TypeMismatch) getErr).getRecordName(),
//                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString((
//                                                                            (TypeMismatch)getErr).getTypeJsonSchema()),
//                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString(((
//                                                                            TypeMismatch)getErr).getTypeBallerinaType())
//                                                                    , method.getKey(), resourcePathSummary.getPath()));

                                                } else {
                                                    System.out.println(ErrorMessages.typeMismatching(postErr.getFieldName(),
                                                            BTypeToJsonValidatorUtil.convertEnumTypetoString
                                                                    (((TypeMismatch) postErr).getTypeJsonSchema()),
                                                            BTypeToJsonValidatorUtil.convertEnumTypetoString
                                                                    (((TypeMismatch) postErr).getTypeBallerinaType())
                                                            , method.getKey(), resourcePathSummary.getPath()));
//                                                    dLog.logDiagnostic(kind, method.getValue().getMethodPosition(),
//                                                            ErrorMessages.typeMismatching(getErr.getFieldName(),
//                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString
//                                                                            (((TypeMismatch) getErr).getTypeJsonSchema()),
//                                                                    BTypeToJsonValidatorUtil.convertEnumTypetoString
//                                                                            (((TypeMismatch) getErr).getTypeBallerinaType())
//                                                                    , method.getKey(), resourcePathSummary.getPath()));
                                                }

                                            } else if (postErr instanceof MissingFieldInJsonSchema) {

//                                              message
                                            } else if (postErr instanceof OneOfTypeValidation) {
                                                if (!(((OneOfTypeValidation) postErr).getBlockErrors()).isEmpty()) {

                                                    List<ValidationError> oneOferrorlist =
                                                            ((OneOfTypeValidation) postErr).getBlockErrors();

                                                    for (ValidationError oneOfvalidation : oneOferrorlist ) {
                                                        if (oneOferrorlist instanceof TypeMismatch) {

                                                        } else if (oneOferrorlist instanceof MissingFieldInJsonSchema) {

                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

//                            type miss match
//                            ballerina filed miss
//                            parameter miss
//                            oneOf type handle

                        }
                    }
                }
            }
        }
//        for ()
//        List<ValidationError> validationErrors = ResourceFunctionToOperation.validate();

    }

}
