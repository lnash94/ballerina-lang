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

package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function type symbol.
 *
 * @since 1.1.0
 */
public class BInvokableTypeSymbol extends BTypeSymbol {

    public List<BVarSymbol> params;
    public BVarSymbol restParam;
    public BType returnType;

    public BInvokableTypeSymbol(int symTag, int flags, PackageID pkgID, BType type, BSymbol owner) {
        super(symTag, flags, Names.EMPTY, pkgID, type, owner);
        this.params = new ArrayList<>();
    }

    @Override
    public SymbolKind getKind() {
        return SymbolKind.INVOKABLE_TYPE;
    }
}
