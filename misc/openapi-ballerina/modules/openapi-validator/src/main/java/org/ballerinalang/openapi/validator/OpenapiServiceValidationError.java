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

import org.ballerinalang.util.diagnostic.Diagnostic;

import java.util.ArrayList;
import java.util.List;

public class OpenapiServiceValidationError {
    Diagnostic.DiagnosticPosition position;
    String serviceOperation;
    String servicePath;
    List<String> tags;

    public OpenapiServiceValidationError() {
        this.position = null;
        this.serviceOperation = null;
        this.servicePath = null;
        this.tags = new ArrayList<>();
    }

    public OpenapiServiceValidationError(Diagnostic.DiagnosticPosition position, String serviceOperation,
                                         String servicePath, List<String> tags) {
        this.position = position;
        this.serviceOperation = serviceOperation;
        this.servicePath = servicePath;
        this.tags = tags;
    }


    public Diagnostic.DiagnosticPosition getPosition() {

        return position;
    }

    public String getServiceOperation() {

        return serviceOperation;
    }

    public String getServicePath() {

        return servicePath;
    }

    public void addTag(List<String> tags) {
        this.tags.addAll(tags);
    }

    public List<String> getTags() {
        return tags;
    }
}
