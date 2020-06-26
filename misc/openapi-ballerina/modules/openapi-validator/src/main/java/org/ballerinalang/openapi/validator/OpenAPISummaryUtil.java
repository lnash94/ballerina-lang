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

/**
 *
 */
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class OpenAPISummaryUtil {
    private static OpenAPIComponentSummary openAPIComponentSummary;
    private List<OpenAPIPathSummary> openAPISummaryList;
    
    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI URI for the OpenAPI contract
     * @return {@link OpenAPI} OpenAPI model
     * @throws OpenApiValidatorException in case of exception
     */
    public static OpenAPI parseOpenAPIFile(String definitionURI) throws OpenApiValidatorException {
        Path contractPath = Paths.get(definitionURI);
        if (!Files.exists(contractPath)) {
            throw new OpenApiValidatorException(ErrorMessages.invalidFilePath(definitionURI));
        }

        if (!(definitionURI.endsWith(".yaml") || definitionURI.endsWith(".json"))) {
            throw new OpenApiValidatorException(ErrorMessages.invalidFile());
        }

        OpenAPI api = new OpenAPIV3Parser().read(definitionURI);
        if (api == null) {
            throw new OpenApiValidatorException(ErrorMessages.parserException(definitionURI));
        }

        return api;
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     *
     * @param openAPISummaries        list of openAPI path summaries
     * @param contract                openAPI contract
     * @param openAPIComponentSummary list of openAPI components
     */
    public void summarizeOpenAPI(List<OpenAPIPathSummary> openAPISummaries, OpenAPI contract
            , OpenAPIComponentSummary openAPIComponentSummary) {
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

        openAPIComponentSummary.setComponents(contract.getComponents());

    }

    private static void addOpenapiSummary(OpenAPIPathSummary openAPISummary, String get, Operation get2) {
        openAPISummary.addAvailableOperation(get);
        openAPISummary.addOperation(get, get2);
    }

//  Get component name reference
    public static String getcomponetName(String ref) {
        String componentName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }    
//   Get component according to  ref name
    public static Schema getOpenAPIComponent(String ref) {
//        OpenAPI openAPIContract = OpenAPISummaryUtil.parseOpenAPIFile("");
        Schema openAPIComponent = openAPIContract.getComponents().getSchemas().get("getcomponetName(ref)");
        return openAPIComponent;
    }

}
