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
package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResolveComponentUtil;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Unit tests for ResoloveComponentUtil.
 */
public class ResolveComponentUtilTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/componentResolve" +
            "/resources/components/")
            .toAbsolutePath();
    private OpenAPI api;
    Components components;
    Collection<ApiResponse> responses;

    @Test(description = "Test01 - Test component with reference")
    public void testComponents() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("components.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();

        Map<String, Schema> component = components.getSchemas();
        Map<String, Schema> properties = component.get("Order").getProperties();
        Assert.assertEquals(properties.get("id").getType(), "integer");

    }

    @Test(description = "Test02 - Test component with nested reference")
    public void testNestedComponents() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("nestedComponents.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        components = ResolveComponentUtil.resolveOpeApiContract(api).getComponents();

        Map<String, Schema> component = components.getSchemas();
        Map<String, Schema> properties = component.get("NestedComponent").getProperties();
        Map<String, Schema> fieldProperties = properties.get("category").getProperties();
        Assert.assertEquals(fieldProperties.get("name").getType(), "string");

    }

    @Test(description = "Test03 - test nested component")
    public void testResolveNestedComponent() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("resolveNestedComponent.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        Schema schema1 = api.getComponents().getSchemas().get("NestedComponent");
        Schema schema2 = ResolveComponentUtil.resolveNestedComponent(schema1, api);

        Map<String, Schema> nestedProperties = schema2.getProperties();
        Map<String, Schema> nextNestedProperties = nestedProperties.get("category").getProperties();
        Map<String, Schema> nextNextNestedProperties = nextNestedProperties.get("tag").getProperties();
        Assert.assertEquals(nextNextNestedProperties.get("id").getType(),"integer");

    }
    @Test(description = "Test04 - test 4 nested component")
    public void testResolve4NestedComponent() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("resolveFourNestedComponents.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        Schema schema1 = api.getComponents().getSchemas().get("FourNestedComponent");
        Schema schema2 = ResolveComponentUtil.resolveNestedComponent(schema1, api);

        Map<String, Schema> nestedProperties = schema2.getProperties();
        Map<String, Schema> nextNestedProperties = nestedProperties.get("category").getProperties();
        Map<String, Schema> nextNextNestedProperties = nextNestedProperties.get("tag").getProperties();
        Map<String, Schema> nextNextNextNestedProperties = nextNextNestedProperties.get("date").getProperties();
        Map<String, Schema> threeNextNestedProperties = nextNextNextNestedProperties.get("year").getProperties();

        Assert.assertEquals(threeNextNestedProperties.get("month").getType(),"string");

    }

    @Test(description = "Test05 - test 4 nested component with resolveOpenapiContract function")
    public void testNestedComponent() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("fourNestedComponent.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        OpenAPI openAPI = ResolveComponentUtil.resolveOpeApiContract(api);
        Schema component = openAPI.getComponents().getSchemas().get("FourNestedComponent");

        Map<String, Schema> nestedProperties = component.getProperties();
        Map<String, Schema> nextNestedProperties = nestedProperties.get("category").getProperties();
        Map<String, Schema> nextNextNestedProperties = nextNestedProperties.get("tag").getProperties();
        Map<String, Schema> nextNextNextNestedProperties = nextNextNestedProperties.get("date").getProperties();
        Map<String, Schema> threeNextNestedProperties = nextNextNextNestedProperties.get("year").getProperties();

        Assert.assertEquals(threeNextNestedProperties.get("month").getType(),"string");

    }
    @Test(description = "Test06 component section name")
    public void testGetComponentScetion () {
//    OpenAPI version 3.0
        Assert.assertEquals(ResolveComponentUtil.getComponentScetion("#/components/schemas/Error"), "schemas");
        Assert.assertEquals(ResolveComponentUtil.getComponentScetion("#/components/parameters/Error"), "parameters");
        Assert.assertEquals(ResolveComponentUtil.getComponentScetion("#/components/responses/Error"), "responses");
//    OpenApi version 2.0
        Assert.assertEquals(ResolveComponentUtil.getComponentScetion("#/definitions/Error"), "definitions");
        Assert.assertEquals(ResolveComponentUtil.getComponentScetion("#/parameters/Error"), "parameters");
        Assert.assertEquals(ResolveComponentUtil.getComponentScetion("#/responses/Error"), "responses");
    }

    @Test(description = "Test07 - tests with component has parameters")
    public void testComponentParameter () throws OpenApiValidatorException {
//        Path contractPath = RES_DIR.resolve("componentsWithParameter.yaml");
        Path contractPath = RES_DIR.resolve("parameterBlockComponents.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        OpenAPI openAPI = ResolveComponentUtil.resolveOpeApiContract(api);
//        Schema component = openAPI.getComponents().getSchemas().get("FourNestedComponent");

    }
    @Test(description = "Test08 component section name out comes")
    public void testParameterSection() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("parameterBlockComponents.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        OpenAPI openAPI = ResolveComponentUtil.resolveOpeApiContract(api);
//        ResolveComponentUtil.getSchemaByName("#/components/parameters/offsetParam", api);
    }

    @Test(description = "Test09 component section name out comes")
    public void testreSolveParameterSection() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("parameterBlockComponents.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        OpenAPI openAPI = ResolveComponentUtil.resolveOpeApiContract(api);
//        ResolveComponentUtil.getSchemaByName("#/components/parameters/offsetParam", api);
    }
    @Test(description = "Test09 component section name out comes")
    public void testResponseSection() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("responseBlockComponents.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
//        OpenAPI openAPI = ResolveComponentUtil.resolveOpeApiContract(api);
//        ResolveComponentUtil.getSchemaByName("#/components/responses/ImageResponse", api);
//        ResolveComponentUtil.getSchemaByName("#/components/responses/GenericError", api);
    }

}
