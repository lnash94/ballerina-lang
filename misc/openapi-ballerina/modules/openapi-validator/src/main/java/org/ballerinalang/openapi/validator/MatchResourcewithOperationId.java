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
import io.swagger.v3.oas.models.PathItem;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MatchResourcewithOperationId {

    /**
     * Checking the available of resource function in openApi contract
     * @param openAPI           openApi contract object
     * @param serviceNode       resource service node
     * @return                  validation Error list with ResourceValidationError type
     */

    public static List<ResourceValidationError> checkResourceIsAvailable(OpenAPI openAPI, ServiceNode serviceNode) {
        List<ResourceValidationError> resourceValidationErrorList = new ArrayList<>();
        List<ResourcePathSummary> resourcePathSummaries = summarizeResources(serviceNode);
        List<OpenAPIPathSummary> openAPISummaries = summarizeOpenAPI(openAPI);
        Boolean isExit = false;
//      Check given path with its methods has documented in OpenApi contract
        for (ResourcePathSummary resourcePathSummary: resourcePathSummaries) {
            isExit = false;
            String resourcePath = resourcePathSummary.getPath();
            Map<String, ResourceMethod> resourcePathMethods = resourcePathSummary.getMethods();
            List<String> servicePathOpearations = new ArrayList<>();
            for (OpenAPIPathSummary openAPIPathSummary : openAPISummaries) {
                String servicePath = openAPIPathSummary.getPath();
                servicePathOpearations = openAPIPathSummary.getAvailableOperations();
                if (resourcePath.equals(servicePath)) {
                    isExit = true;
                    if ((!servicePathOpearations.isEmpty()) && (!resourcePathMethods.isEmpty())) {
                        for (Map.Entry<String, ResourceMethod> entry : resourcePathMethods.entrySet()) {
                            Boolean isMethodExit = false;
                            for (String operation : servicePathOpearations) {
                                if (entry.getKey().equals(operation)) {
                                    isMethodExit = true;
                                    break;
                                }
                            }
                            if (!isMethodExit) {
                                ResourceValidationError resourceValidationError =
                                        new ResourceValidationError(entry.getValue().getMethodPosition(),
                                                entry.getKey(), resourcePath);
                                resourceValidationErrorList.add(resourceValidationError);
                            }
                        }
                    }
                    break;
                }
            }
            if (!isExit) {
                ResourceValidationError resourceValidationError =
                        new ResourceValidationError(resourcePathSummary.getPathPosition(), null, resourcePath);
                resourceValidationErrorList.add(resourceValidationError);
            }
        }

    return resourceValidationErrorList;
    }

    /**
     * Checking the documented services are available at the resource file
     * @param openAPI           openApi contract object
     * @param serviceNode       resource file service
     * @return                  validation error list type with OpenAPIServiceValidationError
     */
    public static List<OpenapiServiceValidationError> checkServiceAvailable(OpenAPI openAPI, ServiceNode serviceNode) {
        List<OpenapiServiceValidationError> validationErrors = new ArrayList<>();
        List<ResourcePathSummary> resourcePathSummaries = summarizeResources(serviceNode);
        List<OpenAPIPathSummary> openAPISummaries = summarizeOpenAPI(openAPI);
        Boolean isServiceExit = false;
//        check the contract paths are available at the resource
        for (OpenAPIPathSummary openAPIPathSummary: openAPISummaries) {
            isServiceExit = false;
            for (ResourcePathSummary resourcePathSummary: resourcePathSummaries) {
                if (openAPIPathSummary.getPath().equals(resourcePathSummary.getPath())) {
                    isServiceExit = true;
//                    check whether documented operations are available at resource file
                    if ((!openAPIPathSummary.getAvailableOperations().isEmpty())) {
                        for (String operation: openAPIPathSummary.getAvailableOperations()) {
                            Boolean isOperationExit = false;
                            if (!(resourcePathSummary.getMethods().isEmpty())) {
                                for (Map.Entry<String, ResourceMethod> method:
                                        resourcePathSummary.getMethods().entrySet()) {
                                    if (operation.equals(method.getKey())) {
                                        isOperationExit = true;
                                        break;
                                    }
                                }
                                if (!isOperationExit) {
                                    OpenapiServiceValidationError openapiServiceValidationError =
                                            new OpenapiServiceValidationError(serviceNode.getPosition(), operation,
                                                    openAPIPathSummary.getPath(),
                                                    openAPIPathSummary.getOperations().get(operation).getTags());
                                    validationErrors.add(openapiServiceValidationError);
                                }
                            }
                        }
                    }
                    break;
                }
            }
            if (!isServiceExit) {
                OpenapiServiceValidationError openapiServiceValidationError =
                        new OpenapiServiceValidationError(serviceNode.getPosition(),
                                null, openAPIPathSummary.getPath(), null);
                validationErrors.add(openapiServiceValidationError);
            }
        }

        return validationErrors;
    }

    /**
     * Extract the details to be validated from the resource.
     * @param serviceNode         service node
     */
    public static List<ResourcePathSummary>  summarizeResources(ServiceNode serviceNode) {
        // Iterate resources available in a service and extract details to be validated.
        List<ResourcePathSummary> resourceSummaryList = new ArrayList<>();
        for (FunctionNode resource : serviceNode.getResources()) {
            AnnotationAttachmentNode annotation = null;

            // Find the "ResourceConfig" annotation.
            for (AnnotationAttachmentNode ann : resource.getAnnotationAttachments()) {

                if (Constants.HTTP.equals(ann.getPackageAlias().getValue())
                        && Constants.RESOURCE_CONFIG.equals(ann.getAnnotationName().getValue())) {
                    annotation = ann;
                }
            }

            if (annotation != null) {
                if (annotation.getExpression() instanceof BLangRecordLiteral) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();

                    String methodPath = null;
                    Diagnostic.DiagnosticPosition pathPos = null;
                    ResourceMethod resourceMethod = new ResourceMethod();
                    String methodName = null;
                    Diagnostic.DiagnosticPosition methodPos = null;
                    String body = null;

                    for (BLangRecordLiteral.RecordField field : recordLiteral.getFields()) {

                        BLangExpression keyExpr;
                        BLangExpression valueExpr;

                        if (field.isKeyValueField()) {
                            BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                            keyExpr = keyValue.getKey();
                            valueExpr = keyValue.getValue();
                        } else {
                            BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                    (BLangRecordLiteral.BLangRecordVarNameField) field;
                            keyExpr = varNameField;
                            valueExpr = varNameField;
                        }



                        if (keyExpr instanceof BLangSimpleVarRef) {
                            BLangSimpleVarRef path = (BLangSimpleVarRef) keyExpr;
                            String contractAttr = path.getVariableName().getValue();
                            // Extract the path and methods of the resource.
                            if (contractAttr.equals(Constants.PATH)) {
                                if (valueExpr instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) valueExpr;
                                    if (value.getValue() instanceof String) {
                                        methodPath = (String) value.getValue();
                                        pathPos = path.getPosition();
                                    }
                                }

                            } else if (contractAttr.equals(Constants.METHODS)) {
                                if (valueExpr instanceof BLangListConstructorExpr) {
                                    BLangListConstructorExpr methodSet = (BLangListConstructorExpr) valueExpr;
                                    for (BLangExpression methodExpr : methodSet.exprs) {
                                        if (methodExpr instanceof BLangLiteral) {
                                            BLangLiteral method = (BLangLiteral) methodExpr;
                                            methodName = ((String) method.value).toLowerCase(Locale.ENGLISH);
                                            methodPos = path.getPosition();

                                        }
                                    }
                                }
                            } else if (contractAttr.equals(Constants.BODY)) {
                                if (valueExpr instanceof BLangLiteral) {
                                    BLangLiteral value = (BLangLiteral) valueExpr;
                                    if (value.getValue() instanceof String) {
                                        body = (String) value.getValue();
                                    }
                                }
                            }
                        }
                    }
                    Boolean isPathExit = false;
                    if (!resourceSummaryList.isEmpty()) {

                        for (ResourcePathSummary resourcePathSummary1 : resourceSummaryList){
                            isPathExit = false;
                            if (methodPath != null) {
                                if (methodPath.equals(resourcePathSummary1.getPath())) {
                                    setValuesResourceMethods(resource, resourceMethod, methodName, methodPos, body,
                                            resourcePathSummary1);
                                    isPathExit =true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!isPathExit) {

                        ResourcePathSummary resourcePathSummary = new ResourcePathSummary();
                        resourcePathSummary.setPath(methodPath);
                        resourcePathSummary.setPathPosition(pathPos);
                        setValuesResourceMethods(resource, resourceMethod, methodName, methodPos, body,
                                resourcePathSummary);
                        // Add the resource summary to the resource summary list.
                        resourceSummaryList.add(resourcePathSummary);
                    }
                }
            }
        }
        return resourceSummaryList;
    }

    private static void setValuesResourceMethods(FunctionNode resource, ResourceMethod resourceMethod,
                                                 String methodName, Diagnostic.DiagnosticPosition methodPos,
                                                 String body, ResourcePathSummary resourcePathSummary) {

        if (body != null) {
            resourceMethod.setBody(body);
        }
        // Extract and add the resource parameters
        if (resource.getParameters().size() > 0) {
            resourceMethod.setParameters(resource.getParameters());
        }
        if (methodName != null) {
            resourceMethod.setMethod(methodName);
        }
        if (methodPos != null) {
            resourceMethod.setMethodPosition(methodPos);
        }
        if (resource.getPosition() != null) {
            resourceMethod.setResourcePosition(resource.getPosition());
        }
        resourcePathSummary.addMethod(methodName, resourceMethod);
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     * @param contract                openAPI contract
     */
    public static List<OpenAPIPathSummary>   summarizeOpenAPI(OpenAPI contract) {
        List<OpenAPIPathSummary> openAPISummaries = new ArrayList<>();
        io.swagger.v3.oas.models.Paths paths = contract.getPaths();
        for (Map.Entry pathItem : paths.entrySet()) {
            OpenAPIPathSummary openAPISummary = new OpenAPIPathSummary();
            if (pathItem.getKey() instanceof String
                    && pathItem.getValue() instanceof PathItem) {
                String key = (String) pathItem.getKey();
                openAPISummary.setPath(key);

                PathItem operations = (PathItem) pathItem.getValue();
                if (operations.getGet() != null) {
                    addOpenapiSummary(openAPISummary, Constants.GET, operations.getGet());
                }

                if (operations.getPost() != null) {
                    addOpenapiSummary(openAPISummary, Constants.POST, operations.getPost());
                }

                if (operations.getPut() != null) {
                    addOpenapiSummary(openAPISummary, Constants.PUT, operations.getPut());
                }

                if (operations.getDelete() != null) {
                    addOpenapiSummary(openAPISummary, Constants.DELETE, operations.getDelete());
                }

                if (operations.getHead() != null) {
                    addOpenapiSummary(openAPISummary, Constants.HEAD, operations.getHead());
                }

                if (operations.getPatch() != null) {
                    addOpenapiSummary(openAPISummary, Constants.PATCH, operations.getPatch());
                }

                if (operations.getOptions() != null) {
                    addOpenapiSummary(openAPISummary, Constants.OPTIONS, operations.getOptions());
                }

                if (operations.getTrace() != null) {
                    addOpenapiSummary(openAPISummary, Constants.TRACE, operations.getTrace());
                }
            }

            openAPISummaries.add(openAPISummary);
        }
    return openAPISummaries;
    }

    private static void addOpenapiSummary(OpenAPIPathSummary openAPISummary, String get, Operation get2) {
        openAPISummary.addAvailableOperation(get);
        openAPISummary.addOperation(get, get2);
    }

}
