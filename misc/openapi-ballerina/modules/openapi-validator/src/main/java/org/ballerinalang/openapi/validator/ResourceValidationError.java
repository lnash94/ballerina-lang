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

public class ResourceValidationError {
//    Diagnostic.Kind kind;
    Diagnostic.DiagnosticPosition position;
    String resourceMethod;
    String resourcePath;

    public ResourceValidationError() {
//        this.kind = null;
        this.position = null;
        this.resourceMethod = null;
        this.resourcePath = null;
    }

    public ResourceValidationError(Diagnostic.DiagnosticPosition position, String resourceMethod,
                                   String resourcePath) {
//        this.kind = kind;
        this.position = position;
        this.resourceMethod = resourceMethod;
        this.resourcePath = resourcePath;
    }

//    public Diagnostic.Kind getKind() {
//
//        return kind;
//    }

    public Diagnostic.DiagnosticPosition getPosition() {

        return position;
    }

    public String getresourceMethod() {

        return resourceMethod;
    }

    public String getResourcePath() {

        return resourcePath;
    }
}
