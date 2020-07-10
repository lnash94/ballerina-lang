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
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.ballerinalang.model.elements.MarkdownDocAttachment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * This util class for resolve the any given schema with reference.
 */
public class ResolveComponentUtil {

    public static OpenAPI resolveOpeApiContract(OpenAPI openAPI) {
        io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();
//        Path items handling 
        for (Map.Entry pathItem: paths.entrySet()) {
//            if (pathItem.getValue() instanceof PathItem) {
                PathItem operation = (PathItem) pathItem.getValue();
//                if (((PathItem) pathItem).get$ref()!=null) {
//                    handle reference in
//                }
//                Handle GET method
                if (operation.getGet()!= null) {
                    List<Parameter> parameters =  operation.getGet().getParameters();
                    Collection<ApiResponse> responses = operation.getGet().getResponses().values();
//                    Handle responses with reference
                    if ((responses != null)&&(!responses.isEmpty())) {
                        for ( ApiResponse apiResponse: responses) {
                            if (!apiResponse.getContent().isEmpty()) {
                                Content content = apiResponse.getContent();
//                                Handle reference in media  type
                                for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
//                                        Handle mediaType has oneOf type references
                                    if (mediaTypeEntry.getValue().getSchema() instanceof ComposedSchema) {
                                        ComposedSchema composedSchema =
                                                ((ComposedSchema) mediaTypeEntry.getValue().getSchema());
                                        if ((composedSchema.getOneOf()!= null) && (!composedSchema.getOneOf().isEmpty())) {
                                            for (Schema schema: composedSchema.getOneOf()) {
                                                if (schema.get$ref()!=null) {
                                                    Schema oneOfSchema = openAPI.getComponents().getSchemas()
                                                            .get(getcomponetName(schema.get$ref()));
                                                    composedSchema.getOneOf().remove(0);
//                                                    add to another list and add it to this.
                                                    composedSchema.getOneOf().add(oneOfSchema);
                                                }
                                            }
                                        }
                                    }
                                    if (mediaTypeEntry.getValue().getSchema().get$ref()!=null) {
                                        Schema mediaSchema =
                                                openAPI.getComponents().getSchemas()
                                                        .get(getcomponetName(mediaTypeEntry.getValue().getSchema()
                                                                .get$ref()));
                                        mediaTypeEntry.getValue().setSchema(mediaSchema);
                                    }



                                }
                            }
                        }
                    }
//                    Handle parameters with Response
                    if ((parameters != null)&&(!parameters.isEmpty())) {
                        for (Parameter parameter: parameters) {
                            if ((parameter.getSchema().getType() == null) && parameter.getSchema().get$ref() != null) {
                                Schema schema =
                                        openAPI.getComponents().getSchemas()
                                                .get(getcomponetName(parameter.getSchema().get$ref()));
                                parameter.setSchema(schema);
                            }
                        }
                    }
//                    Handle request body with reference

                }
//                Handle POST method
                if ( operation.getPost() != null) {
                    List<Parameter> parameters =  operation.getPost().getParameters();
                    if (!parameters.isEmpty()) {
                        for (Parameter parameter: parameters) {
                            if ((parameter.getSchema().getType() == null) && parameter.getSchema().get$ref() != null) {
                                Schema schema =
                                        openAPI.getComponents().getSchemas()
                                                .get(getcomponetName(parameter.getSchema().get$ref()));
                                parameter.setSchema(schema);
//                                System.out.println(openAPI);
                            }
                        }
                    }
//                    return openAPI;
                }
            }

//        }
        System.out.println(openAPI);
        return openAPI;
    }

    //  Get component name from reference
    public static String getcomponetName(String ref) {
        String componentName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }


}
