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


    public static List<ResourceValidationError> checkResouceIsAvailable(OpenAPI openAPI, ServiceNode serviceNode) {
        List<ResourceValidationError> resourceValidationErrorList = new ArrayList<>();
        List<ResourceSummary> resourceSummaryList = summarizeResources(serviceNode);
        List<OpenAPIPathSummary> openAPISummaries = summarizeOpenAPI(openAPI);
        Boolean isExit = false;
//      Check given path with its methods has documented in OpenApi contract
        for (ResourceSummary resourceSummary: resourceSummaryList) {
            String resourcePath = resourceSummary.getPath();
            for (OpenAPIPathSummary openAPIPathSummary: openAPISummaries) {
                String servicePath = openAPIPathSummary.getPath();
                List<String> servicePathOpearations = openAPIPathSummary.getAvailableOperations();
                if (resourcePath.equals(servicePath)) {
                        isExit = true;
                        break;
                }
            }
            if(!isExit) {
                ResourceValidationError resourceValidationError =
                        new ResourceValidationError(resourceSummary.getPathPosition(), null, resourcePath );
                resourceValidationErrorList.add(resourceValidationError);
            }
        }



    return resourceValidationErrorList;
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

                                    if (body != null) {
                                        resourceMethod.setBody(body);
                                    }
                                    // Extract and add the resource parameters
                                    if (resource.getParameters().size() > 0 ) {
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
                                    resourcePathSummary1.addMethod(methodName, resourceMethod);
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

                        if (body != null) {
                            resourceMethod.setBody(body);
                        }
                        // Extract and add the resource parameters
                        if (resource.getParameters().size() > 0 ) {
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
                        // Add the resource summary to the resource summary list.
                        resourceSummaryList.add(resourcePathSummary);
                    }
                }
            }
        }
        return resourceSummaryList;
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     * @param contract                openAPI contract
     */
    public static List<OpenAPIPathSummary>   summarizeOpenAPI(OpenAPI contract) {
        List<OpenAPIPathSummary> openAPISummaries = null;
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
//        openAPIComponentSummary.setComponents(contract.getComponents());
    }

    private static void addOpenapiSummary(OpenAPIPathSummary openAPISummary, String get, Operation get2) {
        openAPISummary.addAvailableOperation(get);
        openAPISummary.addOperation(get, get2);
    }

}
