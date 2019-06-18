/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.RecordType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;

/**
 * {@code BRecordType} represents record type in Ballerina.
 *
 * @since 0.971.0
 */
public class BRecordType extends BStructureType implements RecordType {

    private static final String SPACE = " ";
    private static final String RECORD = "record";
    private static final String CLOSE_LEFT = "{|";
    private static final String OPEN_LEFT = "{";
    private static final String SEMI = ";";
    private static final String CLOSE_RIGHT = "|}";
    private static final String DOLLAR = "$";
    private static final String OPEN_RIGHT = "}";
    private static final String REST = "...";
    public boolean sealed;
    public BType restFieldType;

    public BRecordType(BTypeSymbol tSymbol) {
        super(TypeTags.RECORD, tSymbol);
        this.fields = new ArrayList<>();
    }

    public String getDesc() {
        return TypeDescriptor.SIG_STRUCT + getQualifiedTypeName() + ";";
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.RECORD;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {

        if (tsymbol.name.value.isEmpty() || tsymbol.name.value.startsWith(DOLLAR)) {
            StringBuilder sb = new StringBuilder();
            sb.append(RECORD).append(SPACE);
            sb.append(sealed ? CLOSE_LEFT : OPEN_LEFT);
            fields.forEach(fields -> sb.append(SPACE).append(fields.type).append(SPACE)
                    .append(fields.name).append(SEMI));
            if (sealed) {
                sb.append(SPACE).append(CLOSE_RIGHT);
                return sb.toString();
            }
            sb.append(restFieldType).append(REST);
            sb.append(SPACE).append(OPEN_RIGHT);
            return sb.toString();
        }
        return this.tsymbol.toString();
    }
}
