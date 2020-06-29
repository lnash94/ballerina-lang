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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.OpenAPISummaryUtil;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.constraints.Null;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for {}.
 */
public class OpenApiComponentByRefTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private OpenAPI api;
    private Object Null;

    @Test(description = "Test get the component name using references string")
    public void testComponentName(){
        Assert.assertEquals(OpenAPISummaryUtil.getcomponetName("#/components/schemas/ValueField"),"ValueField");
        Assert.assertEquals(OpenAPISummaryUtil.getcomponetName("#/definitions/Pet"),"Pet");
        Assert.assertEquals(OpenAPISummaryUtil.getcomponetName("User"), Null);
    }

    @Test(description = "Test get the component using reference")
    public void testComponent() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        OpenAPISummaryUtil openAPISummaryUtil = new OpenAPISummaryUtil();
        openAPISummaryUtil.setDefinitionURI(contractPath.toString());
        Assert.assertTrue(openAPISummaryUtil.getOpenAPIComponent("#/definitions/Pet") instanceof Schema);
    }
    @Test(description = "Test get the component using component name")
    public void testComponentByName() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("project-based-tests/src/record/resources/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        OpenAPISummaryUtil openAPISummaryUtil = new OpenAPISummaryUtil();
        openAPISummaryUtil.setDefinitionURI(contractPath.toString());
        Assert.assertTrue(openAPISummaryUtil.getComponetByName("Pet") instanceof Schema);
    }

}
