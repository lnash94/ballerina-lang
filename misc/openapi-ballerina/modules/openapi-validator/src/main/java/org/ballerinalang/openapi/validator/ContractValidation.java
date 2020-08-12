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
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.openapi.validator.error.OpenapiServiceValidationError;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ContractValidation {

    public static void validateContract(OpenAPI openApi,
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
        boolean excludeOperationFilterEnabled = excludeOperations.size() > 0;

        List<OpenAPIPathSummary> openAPIPathSummaryList = MatchResourcewithOperationId.summarizeOpenAPI(openApi);
        List<OpenapiServiceValidationError> missingPathInResource =
                MatchResourcewithOperationId.checkServiceAvailable(openAPIPathSummaryList, serviceNode);

        if (!missingPathInResource.isEmpty()) {
            for (OpenapiServiceValidationError contractError : missingPathInResource) {
//                handle missing path and operation when filter enable
                if (tagFilteringEnabled || operationFilteringEnabled || excludeTagsFilteringEnabled
                        || excludeOperationFilterEnabled) {
                    if (operationFilteringEnabled) {
                        operationsFilter(serviceNode, contractError.getOpenAPIPathSummary(), operations, kind,
                                Diagnostic.Kind.ERROR,
                                Diagnostic.Kind.WARNING, dLog, contractError.getServiceOperation());
                    } else if (excludeOperationFilterEnabled) {
                        operationsFilter(serviceNode, contractError.getOpenAPIPathSummary(), excludeOperations, kind,
                                Diagnostic.Kind.WARNING,
                                Diagnostic.Kind.ERROR, dLog, contractError.getServiceOperation());
                    }

                    if (tagFilteringEnabled) {
                        tagsFilter(serviceNode, contractError.getOpenAPIPathSummary(), tags, kind,
                                Diagnostic.Kind.ERROR,
                                Diagnostic.Kind.WARNING, dLog, contractError.getServiceOperation());
                    } else if (excludeTagsFilteringEnabled) {
                        tagsFilter(serviceNode, contractError.getOpenAPIPathSummary(), excludeTags, kind,
                                Diagnostic.Kind.WARNING,
                                Diagnostic.Kind.ERROR, dLog, contractError.getServiceOperation());;
                    }

                } else {
//                    Handle missing Path
                    if (contractError.getServiceOperation() == null) {
                        dLog.logDiagnostic(kind, getServiceNamePosition(serviceNode),
                                ErrorMessages.unimplementedOpenAPIPath(contractError.getServicePath()));
                    } else {
//                        Handle missing operation
                        dLog.logDiagnostic(kind, getServiceNamePosition(serviceNode),
                                ErrorMessages.unimplementedOpenAPIOperationsForPath(contractError.getServiceOperation(),
                                        contractError.getServicePath()));
                    }
                }

            }

        }

//        remove undocumented operations and path form validating
        List<OpenAPIPathSummary> openAPISummaries = MatchResourcewithOperationId.summarizeOpenAPI(openApi);

        if (!openAPISummaries.isEmpty()) {
            Iterator<OpenAPIPathSummary> openAPIPathIterator = openAPISummaries.iterator();
            while (openAPIPathIterator.hasNext()) {
                OpenAPIPathSummary openAPIPathSummary = openAPIPathIterator.next();
                if (!missingPathInResource.isEmpty()) {
                    for (OpenapiServiceValidationError error: missingPathInResource) {
                        if (error.getServicePath().equals(openAPIPathSummary.getPath())) {
                            if ((error.getServiceOperation() != null) &&
                                    (!openAPIPathSummary.getOperations().isEmpty())) {
                                Map<String, Operation> operationsMap = openAPIPathSummary.getOperations();
                                operationsMap.entrySet().removeIf(operationMap -> operationMap.getKey()
                                        .equals(error.getServiceOperation()));
                            } else if (error.getServiceOperation() == null) {
                                openAPIPathIterator.remove();
                            }
                        }
                    }
                }
            }
        }
//        Generate error message
        List<ResourcePathSummary> resourcePathSummaries = MatchResourcewithOperationId.summarizeResources(serviceNode);
        for (OpenAPIPathSummary openAPIPath : openAPISummaries) {
            if (!resourcePathSummaries.isEmpty()) {
                for (ResourcePathSummary resourcePathSummary: resourcePathSummaries) {
                    if (openAPIPath.getPath().equals(resourcePathSummary.getPath())) {
//                        tag filter enable
//                        operation filter
//                        exclude tag filter
//                        exclude operation filter
//                        tag + excludeOperation
//                        operation + Exclude Tag
//
                    }
                }
            }

        }


    }
//    get service position
    private static Diagnostic.DiagnosticPosition getServiceNamePosition(ServiceNode serviceNode) {
        return serviceNode.getName().getPosition();
    }

    private static void tagsFilter (ServiceNode serviceNode,
                                    OpenAPIPathSummary openApiSummary,
                                    List<String> tags,
                                    Diagnostic.Kind kind,
                                    Diagnostic.Kind kind1,
                                    Diagnostic.Kind kind2,
                                    DiagnosticLog dLog,
                                   String operation) {
        for (String method : openApiSummary.getAvailableOperations()) {
            if (openApiSummary.hasTags(tags, method)) {
                kind = kind1;
                break;
            } else {
                kind = kind2;
            }
        }
        if (operation != null) {
            dLog.logDiagnostic(kind, getServiceNamePosition(serviceNode),
                    ErrorMessages.unimplementedOpenAPIOperationsForPath(operation, openApiSummary.getPath()));
        } else {
            dLog.logDiagnostic(kind, getServiceNamePosition(serviceNode),
                    ErrorMessages.unimplementedOpenAPIPath(openApiSummary.getPath()));
        }

    }
    //for Operation Filter
    private static void operationsFilter (ServiceNode serviceNode,
                                          OpenAPIPathSummary openApiSummary,
                                          List<String> operations,
                                          Diagnostic.Kind kind,
                                          Diagnostic.Kind kind1,
                                          Diagnostic.Kind kind2,
                                          DiagnosticLog dLog,
                                          String operation) {
        for (String method : openApiSummary.getAvailableOperations()) {
            if (openApiSummary.hasOperations(operations, method)) {
                kind = kind1;
                break;
            } else {
                kind = kind2;
            }
        }
        if (operation != null) {
            dLog.logDiagnostic(kind, getServiceNamePosition(serviceNode),
                    ErrorMessages.unimplementedOpenAPIOperationsForPath(operation, openApiSummary.getPath()));
        } else {
            dLog.logDiagnostic(kind, getServiceNamePosition(serviceNode),
                    ErrorMessages.unimplementedOpenAPIPath(openApiSummary.getPath()));
        }
    }

}
