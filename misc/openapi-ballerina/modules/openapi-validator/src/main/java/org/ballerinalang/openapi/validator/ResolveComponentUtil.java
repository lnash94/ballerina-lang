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

import io.swagger.models.apideclaration.Api;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.ballerinalang.model.elements.MarkdownDocAttachment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
/**
 * This util class for resolve the any given schema with reference.
 */
public class ResolveComponentUtil {

    public static OpenAPI resolveOpeApiContract(OpenAPI openAPI) {

//        Components Handling
        Components components = openAPI.getComponents();
        if (components != null) {
//            Handle component schemas
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                for (Map.Entry<String, Schema> schema: schemas.entrySet()) {
                    if (schema.getValue().get$ref() != null) {
//                    with default reference
                        Schema refSchema = openAPI.getComponents().getSchemas()
                                .get(getcomponetName(schema.getValue().get$ref()));
                        schemas.put(schema.getKey(), refSchema);
                    }
//                with properties
                    Map<String, Schema> properties = schema.getValue().getProperties();
                    if (properties != null) {
                        for (Map.Entry<String, Schema> propSchema: properties.entrySet()) {
                            String ref = propSchema.getValue().get$ref();
                            if ( ref != null) {
                                Schema schema1 = openAPI.getComponents().getSchemas().get(getcomponetName(ref));
                                Schema schema2 = ResolveComponentUtil.resolveNestedComponent(schema1, openAPI);
                                propSchema.setValue(schema2);
                            } else if (propSchema.getValue() instanceof ArraySchema) {
                                Schema schema3 = ResolveComponentUtil.resolveNestedComponent(propSchema.getValue(),
                                        openAPI);
                                propSchema.setValue(schema3);
//                                ((ArraySchema) propSchema.getValue()).setItems(schema3);
                            }
                            continue;
                        }
                    }
                }
            }
//            Handle reusable parameters
            Map<String, Parameter> parameters = components.getParameters();
            if (parameters != null) {
                for (Map.Entry<String, Parameter> parameter: parameters.entrySet()) {
                    if (parameter.getValue().get$ref() != null) {
                        Schema schemaParam =
                                openAPI.getComponents().getSchemas()
                                        .get(getcomponetName(parameter.getValue().get$ref()));
                        Schema schemaParam2 = resolveNestedComponent(schemaParam, openAPI);
                        parameter.getValue().setSchema(schemaParam2);
                        parameter.getValue().set$ref(null);
                    }
                }
            }
//            Handle reusable responses
            Map<String, ApiResponse> responses = components.getResponses();
            if (responses != null) {
                for (Map.Entry<String, ApiResponse> responseEntry: responses.entrySet()) {
                    if(responseEntry.getValue().get$ref() != null) {
                        ApiResponse apiResponse = components.getResponses().get(getcomponetName(responseEntry
                                .getValue().get$ref()));
                        responseEntry.setValue(apiResponse);
                    }
                }
                Map<String, ApiResponse> responses2 = openAPI.getComponents().getResponses();
                handleResponses(openAPI, responses2.values());
            }
//            Handle reusable requestBody

        }

//        Path items handling
        io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();
        if (paths != null) {
            for (Map.Entry pathItem: paths.entrySet()) {
//            if (pathItem.getValue() instanceof PathItem) {
                PathItem operation = (PathItem) pathItem.getValue();
//                if (((PathItem) pathItem).get$ref()!=null) {
//                    handle reference in
//                }
//                Handle GET method
                if (operation.getGet()!= null) {
                    List<Parameter> parameters =  operation.getGet().getParameters();
                    Collection<ApiResponse> responses = null;
                    final ApiResponses responses1 = operation.getGet().getResponses();
//                    Handle responses with reference
                    if (responses1 != null) {
                        responses = operation.getGet().getResponses().values();
                        handleResponses(openAPI, responses);
                    }
//                    Handle parameters with Response
                    if ((parameters != null) && (!parameters.isEmpty())) {
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
                    Collection<ApiResponse> responses = null;
                    final ApiResponses responses1 = operation.getPost().getResponses();
//                    Handle responses with reference
                    if (responses1 != null) {
                        responses = operation.getPost().getResponses().values();
                        handleResponses(openAPI, responses);
                    }

                    if ((parameters != null) && (!parameters.isEmpty())) {
                        for (Parameter parameter: parameters) {
                            if ((parameter.getSchema().getType() == null) && parameter.getSchema().get$ref() != null) {
                                Schema schema =
                                        openAPI.getComponents().getSchemas()
                                                .get(getcomponetName(parameter.getSchema().get$ref()));
                                parameter.setSchema(schema);
                            }
                        }
                    }
                }
            }
        }

//        }
//        System.out.println(openAPI);
        return openAPI;
    }

// Resolve reference in responses
    private static void handleResponses(OpenAPI openAPI, Collection<ApiResponse> responses) {

        //                    Handle responses with reference
        if ((responses != null) && (!responses.isEmpty())) {
            for (ApiResponse apiResponse : responses) {
                if ((apiResponse.getContent() != null) && (!apiResponse.getContent().isEmpty())) {
                    Content content = apiResponse.getContent();
//                                Handle reference in media  type
                    for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                        if (mediaTypeEntry.getValue().getSchema() instanceof ComposedSchema) {
                            ComposedSchema composedSchema =
                                    ((ComposedSchema) mediaTypeEntry.getValue().getSchema());
//                                        Handle mediaType has oneOf type references
                            if ((composedSchema.getOneOf() != null) && (!composedSchema.getOneOf().isEmpty())) {
                                List<Schema> newSchemas = new ArrayList<>();
                                ListIterator<Schema> composedIter =
                                        composedSchema.getOneOf().listIterator();
                                while (composedIter.hasNext()) {
                                    Schema iterSchema = composedIter.next();
                                    if (iterSchema.get$ref() != null) {
                                        Schema oneOfSchema = openAPI.getComponents().getSchemas()
                                                .get(getcomponetName(iterSchema.get$ref()));
                                        composedIter.set(oneOfSchema);
                                    }
                                }
                                composedSchema.getOneOf().addAll(newSchemas);
                            }
//                                        Handle mediaType has anyOf references
                            if ((composedSchema.getAnyOf() != null) && (!composedSchema.getAnyOf().isEmpty())) {
                                List<Schema> newSchemas = new ArrayList<>();
                                ListIterator<Schema> composedIter =
                                        composedSchema.getAnyOf().listIterator();
                                while (composedIter.hasNext()) {
                                    Schema iterSchema = composedIter.next();
                                    if (iterSchema.get$ref() != null) {
                                        Schema anyOfSchema = openAPI.getComponents().getSchemas()
                                                .get(getcomponetName(iterSchema.get$ref()));
                                        composedIter.set(anyOfSchema);
                                    }
                                }
                                composedSchema.getAnyOf().addAll(newSchemas);
                            }
//                                       mediaType hasn't  allOf references yet
                        }
                        if (mediaTypeEntry.getValue().getSchema().get$ref() != null) {
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
    }

//     Get component name from reference
    public static String getcomponetName(String ref) {
        String componentName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }
//    Get component section name
    public static String getComponentScetion(String ref) {
        String sectionName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            sectionName = splitRef[splitRef.length - 2];
        }
        return sectionName;
    }

//  Resolve reference in components
    public static  Schema resolveNestedComponent(Schema schema, OpenAPI openAPI) {

        if (schema instanceof ArraySchema) {
            if (((ArraySchema) schema).getItems().get$ref() != null) {
                Schema schema3 = openAPI.getComponents().getSchemas().get(ResolveComponentUtil.
                        getcomponetName(((ArraySchema) schema).getItems().get$ref()));
                Schema schema4 = ResolveComponentUtil.resolveNestedComponent(schema3, openAPI);
                ((ArraySchema) schema).setItems(schema4);
            }
        } else if (schema.get$ref() != null) {
            Schema schema1 = openAPI.getComponents().getSchemas().get(ResolveComponentUtil.
                    getcomponetName(schema.get$ref()));
            schema = ResolveComponentUtil.resolveNestedComponent(schema1, openAPI);

        } else if (schema.getProperties() != null) {
            Map<String, Schema> properties = schema.getProperties();
            for (Map.Entry<String, Schema> propSchema: properties.entrySet()) {
                if (propSchema.getValue().get$ref() != null) {
                    Schema schema2 = openAPI.getComponents().getSchemas()
                            .get(getcomponetName(propSchema.getValue().get$ref()));
                    propSchema.setValue(schema2);
                    String schema1Ref = schema2.get$ref();
                    if (schema1Ref != null) {
                        Schema schema3 = resolveNestedComponent(schema2, openAPI);
                        propSchema.setValue(schema3);
                    }
                }
                Map<String, Schema> propSchemaProperties = propSchema.getValue().getProperties();
                if (propSchemaProperties != null) {
                    for (Map.Entry<String, Schema> mapPropSchema: propSchemaProperties.entrySet()) {
                        String mapPropSchemaRef = mapPropSchema.getValue().get$ref();
                        if (mapPropSchemaRef != null) {
                            Schema schema4 = openAPI.getComponents().getSchemas()
                                    .get(getcomponetName(mapPropSchemaRef));
                            Schema schema5 = resolveNestedComponent(schema4, openAPI);
                            mapPropSchema.setValue(schema5);
                        }
                        Map<String, Schema> mapPropSchemaProperties = mapPropSchema.getValue().getProperties();
                        if ( mapPropSchemaProperties != null) {
                            for (Map.Entry<String, Schema> schemaEntry: mapPropSchemaProperties.entrySet()) {
                                Schema schema6 = resolveNestedComponent(schemaEntry.getValue(), openAPI);
                                schemaEntry.setValue(schema6);
                            }
                        }
                        continue;
                    }
                }
                continue;
            }
        }
        return schema;
    }
}
