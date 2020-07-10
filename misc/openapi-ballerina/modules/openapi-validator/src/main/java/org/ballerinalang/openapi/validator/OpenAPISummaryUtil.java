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
import io.swagger.v3.oas.models.media.Schema;

/**
 * This util for get the openapi component schema for given reference name.
 */
public class OpenAPISummaryUtil {

//  Get component name from reference
    public static String getcomponetName(String ref) {
        String componentName = null;
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }    
//   Get component according to  ref name
    public static Schema getOpenAPIComponent(String ref) throws OpenApiValidatorException {
//        String executionPath = System.getProperty("user.dir");
//        Path execution = Paths.get(executionPath);
//        String contractPath = execution.resolve("src/resources").toAbsolutePath().toString();
        OpenAPI openAPIContract = ValidatorUtil.parseOpenAPIFile(OpenAPIValidatorPlugin.getOpenapiContractURI());
        Schema openAPIComponent = openAPIContract.getComponents().getSchemas().
                get(OpenAPISummaryUtil.getcomponetName(ref));
        return openAPIComponent;
    }
    public static Schema getComponetByName(String name) throws OpenApiValidatorException {
        OpenAPI openAPIContract = ValidatorUtil.parseOpenAPIFile(OpenAPIValidatorPlugin.getOpenapiContractURI());
        Schema openAPIComponent = openAPIContract.getComponents().getSchemas().get(name);
        return openAPIComponent;
    }

}
