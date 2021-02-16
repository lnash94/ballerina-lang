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
package org.ballerinalang.langserver.contexts;

import io.ballerina.projects.PackageCompilation;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class CodeActionContextImpl extends AbstractDocumentServiceContext implements CodeActionContext {

    private Position cursorPosition;
    private List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics;
    private final CodeActionParams params;
    private PositionDetails positionDetails;

    public CodeActionContextImpl(LSOperation operation,
                                 String fileUri,
                                 WorkspaceManager wsManager,
                                 CodeActionParams params,
                                 LanguageServerContext serverContext) {
        super(operation, fileUri, wsManager, serverContext);
        this.params = params;
    }

    @Override
    public Position cursorPosition() {
        if (this.cursorPosition == null) {
            int line = params.getRange().getStart().getLine();
            int col = params.getRange().getStart().getCharacter();
            this.cursorPosition = new Position(line, col);
        }

        return this.cursorPosition;
    }

    @Override
    public List<io.ballerina.tools.diagnostics.Diagnostic> allDiagnostics() {
        if (diagnostics == null) {
            PackageCompilation compilation = workspace().waitAndGetPackageCompilation(this.filePath()).orElseThrow();
            this.diagnostics = new ArrayList<>(compilation.diagnosticResult().diagnostics());
        }

        return this.diagnostics;
    }

    @Override
    public List<Diagnostic> cursorDiagnostics() {
        return params.getContext().getDiagnostics();
    }

    @Override
    public void setPositionDetails(PositionDetails positionDetails) {
        this.positionDetails = positionDetails;
    }

    @Override
    public PositionDetails positionDetails() {
        return this.positionDetails;
    }

    /**
     * Represents Language server code action context Builder.
     *
     * @since 2.0.0
     */
    protected static class CodeActionContextBuilder extends AbstractContextBuilder<CodeActionContextBuilder> {

        private final CodeActionParams params;

        public CodeActionContextBuilder(CodeActionParams params,
                                        LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_CODE_ACTION, serverContext);
            this.params = params;
        }

        public CodeActionContext build() {
            return new CodeActionContextImpl(this.operation,
                                             this.fileUri,
                                             this.wsManager,
                                             this.params,
                                             this.serverContext);
        }

        @Override
        public CodeActionContextBuilder self() {
            return this;
        }
    }
}
