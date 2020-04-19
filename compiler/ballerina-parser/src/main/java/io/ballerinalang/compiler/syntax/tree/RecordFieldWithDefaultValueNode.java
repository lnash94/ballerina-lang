/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

/**
 * This is a generated syntax tree node.
 *
 * @since 1.3.0
 */
public class RecordFieldWithDefaultValueNode extends NonTerminalNode {

    public RecordFieldWithDefaultValueNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public MetadataNode metadata() {
        return childInBucket(0);
    }

    public Node typeName() {
        return childInBucket(1);
    }

    public Token fieldName() {
        return childInBucket(2);
    }

    public Token equalsToken() {
        return childInBucket(3);
    }

    public ExpressionNode expression() {
        return childInBucket(4);
    }

    public Token semicolonToken() {
        return childInBucket(5);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "metadata",
                "type",
                "fieldName",
                "equalsToken",
                "expression",
                "semicolonToken"};
    }

    public RecordFieldWithDefaultValueNode modify(
            MetadataNode metadata,
            Node typeName,
            Token fieldName,
            Token equalsToken,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadata,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }
}
