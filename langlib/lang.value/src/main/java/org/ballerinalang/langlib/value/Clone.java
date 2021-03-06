/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.langlib.value;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.CloneUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Performs a deep copy, recursively copying all structural values and their members.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "lang.value",
        functionName = "clone",
        args = {@Argument(name = "value", type = TypeKind.ANYDATA)},
        returnType = {@ReturnType(type = TypeKind.ANYDATA)}
)
public class Clone {

    public static Object clone(Strand strand, Object value) {
        return CloneUtils.cloneValue(value);
    }

    public static Object clone_bstring(Strand strand, Object value) {
        return clone(strand, value);
    }
}
