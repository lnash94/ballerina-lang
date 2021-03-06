/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for desugar query pipeline into actual Ballerina code.
 *
 * @since 1.2.0
 */
public class QueryDesugar extends BLangNodeVisitor {
    private static final Name QUERY_CREATE_PIPELINE_FUNCTION = new Name("createPipeline");
    private static final Name QUERY_CREATE_INPUT_FUNCTION = new Name("createInputFunction");
    private static final Name QUERY_CREATE_LET_FUNCTION = new Name("createLetFunction");
    private static final Name QUERY_CREATE_JOIN_FUNCTION = new Name("createJoinFunction");
    private static final Name QUERY_CREATE_FILTER_FUNCTION = new Name("createFilterFunction");
    private static final Name QUERY_CREATE_SELECT_FUNCTION = new Name("createSelectFunction");
    private static final Name QUERY_CREATE_DO_FUNCTION = new Name("createDoFunction");
    private static final Name QUERY_ADD_STREAM_FUNCTION = new Name("addStreamFunction");
    private static final Name QUERY_CONSUME_STREAM_FUNCTION = new Name("consumeStream");
    private static final Name QUERY_TO_ARRAY_FUNCTION = new Name("toArray");
    private static final Name QUERY_GET_STREAM_FROM_PIPELINE_FUNCTION = new Name("getStreamFromPipeline");
    private static final String FRAME_PARAMETER_NAME = "$frame$";
    private static final CompilerContext.Key<QueryDesugar> QUERY_DESUGAR_KEY = new CompilerContext.Key<>();
    private BVarSymbol currentFrameSymbol;
    private BLangBlockFunctionBody currentLambdaBody;
    private Map<String, BSymbol> identifiers;
    private int streamElementCount = 0;
    private final Desugar desugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private SymbolEnv env;

    private QueryDesugar(CompilerContext context) {
        context.put(QUERY_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
    }

    public static QueryDesugar getInstance(CompilerContext context) {
        QueryDesugar desugar = context.get(QUERY_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new QueryDesugar(context);
        }
        return desugar;
    }

    BLangStatementExpression desugar(BLangQueryExpr queryExpr, SymbolEnv env) {
        List<BLangNode> clauses = queryExpr.getQueryClauses();
        DiagnosticPos pos = clauses.get(0).pos;
        BLangBlockStmt queryBlock = ASTBuilderUtil.createBlockStmt(pos);
        BLangVariableReference streamRef = buildStream(clauses, queryExpr.type, env, queryBlock);
        BLangStatementExpression streamStmtExpr;
        // TODO check queryExpr.isTable
        if (queryExpr.isStream) {
            streamStmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock, streamRef);
            streamStmtExpr.type = streamRef.type;
        } else {
            BLangVariableReference result = getStreamFunctionVariableRef(queryBlock,
                    QUERY_TO_ARRAY_FUNCTION, Lists.of(streamRef), pos);
            streamStmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock, result);
            streamStmtExpr.type = result.type;
        }
        return streamStmtExpr;
    }

    BLangStatementExpression desugar(BLangQueryAction queryAction, SymbolEnv env) {
        List<BLangNode> clauses = queryAction.getQueryClauses();
        DiagnosticPos pos = clauses.get(0).pos;
        BLangBlockStmt queryBlock = ASTBuilderUtil.createBlockStmt(pos);
        BLangVariableReference streamRef = buildStream(clauses, queryAction.type, env, queryBlock);
        BLangVariableReference result = getStreamFunctionVariableRef(queryBlock,
                QUERY_CONSUME_STREAM_FUNCTION, symTable.errorOrNilType, Lists.of(streamRef), pos);
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock, result);
        stmtExpr.type = symTable.errorOrNilType;
        return stmtExpr;
    }

    /**
     * Write the pipeline to the given `block` and return the reference to the resulting stream.
     *
     * @param clauses
     * @param resultType
     * @param env
     * @param block
     * @return
     */
    BLangVariableReference buildStream(List<BLangNode> clauses, BType resultType, SymbolEnv env, BLangBlockStmt block) {
        this.env = env;
        BLangNode initFromClause = clauses.get(0);
        final BLangVariableReference initPipeline = addPipeline(block, (BLangFromClause) initFromClause, resultType);
        BLangVariableReference initFrom = addInputFunction(block, (BLangFromClause) initFromClause);
        addStreamFunction(block, initPipeline, initFrom);
        for (BLangNode clause : clauses.subList(1, clauses.size())) {
            switch (clause.getKind()) {
                case FROM:
                case JOIN:
                    BLangVariableReference pipeline = addPipeline(block, (BLangInputClause) clause, resultType);
                    BLangVariableReference fromFunc = addInputFunction(block, (BLangInputClause) clause);
                    addStreamFunction(block, pipeline, fromFunc);
                    BLangVariableReference joinFunc = addJoinFunction(block, pipeline);
                    addStreamFunction(block, initPipeline, joinFunc);
                    break;
                case LET_CLAUSE:
                    BLangVariableReference letFunc = addLetFunction(block, (BLangLetClause) clause);
                    addStreamFunction(block, initPipeline, letFunc);
                    break;
                case WHERE:
                    BLangVariableReference whereFunc = addFilterFunction(block, (BLangWhereClause) clause);
                    addStreamFunction(block, initPipeline, whereFunc);
                    break;
                case ON:
                    BLangVariableReference onFunc = addFilterFunction(block, (BLangOnClause) clause);
                    addStreamFunction(block, initPipeline, onFunc);
                    break;
                case SELECT:
                    BLangVariableReference selectFunc = addSelectFunction(block, (BLangSelectClause) clause);
                    addStreamFunction(block, initPipeline, selectFunc);
                    break;
                case DO:
                    BLangVariableReference doFunc = addDoFunction(block, (BLangDoClause) clause);
                    addStreamFunction(block, initPipeline, doFunc);
                    break;
            }
        }
        return addGetStreamFromPipeline(block, initPipeline);
    }

    /**
     * Desugar fromClause/joinClause to below and return a reference to created join _StreamPipeline.
     * _StreamPipeline pipeline = createPipeline(collection);
     *
     * @param blockStmt  parent block to write to.
     * @param inputClause to init pipeline.
     * @return variableReference to created _StreamPipeline.
     */
    BLangVariableReference addPipeline(BLangBlockStmt blockStmt, BLangInputClause inputClause, BType resultType) {
        BLangExpression collection = inputClause.collection;
        DiagnosticPos pos = inputClause.pos;
        String name = getNewVarName();
        BVarSymbol dataSymbol = new BVarSymbol(0, names.fromString(name), env.scope.owner.pkgID,
                collection.type, this.env.scope.owner);
        BLangSimpleVariable dataVariable = ASTBuilderUtil.createVariable(inputClause.pos, name,
                collection.type, collection, dataSymbol);
        BLangSimpleVariableDef dataVarDef = ASTBuilderUtil.createVariableDef(inputClause.pos, dataVariable);
        BLangVariableReference valueVarRef = ASTBuilderUtil.createVariableRef(pos, dataSymbol);
        blockStmt.addStatement(dataVarDef);
        if (resultType.tag == TypeTags.ARRAY) {
            resultType = ((BArrayType) resultType).eType;
        } else if (resultType.tag == TypeTags.STREAM) {
            resultType = ((BStreamType) resultType).constraint;
        }
        BType typedescType = new BTypedescType(resultType, symTable.typeDesc.tsymbol);
        BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
        typedescExpr.resolvedType = resultType;
        typedescExpr.type = typedescType;
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_PIPELINE_FUNCTION,
                Lists.of(valueVarRef, typedescExpr), inputClause.pos);
    }

    /**
     * Desugar inputClause to below and return a reference to created from _StreamFunction.
     * _StreamFunction xsFrom = createFromFunction(function(_Frame frame) returns _Frame|error? {
     * int x = <int> frame["value"];
     * frame["x"] = x;
     * return frame;
     * });
     *
     * @param blockStmt  parent block to write to.
     * @param inputClause to be desugared.
     * @return variableReference to created from _StreamFunction.
     */
    BLangVariableReference addInputFunction(BLangBlockStmt blockStmt, BLangInputClause inputClause) {
        DiagnosticPos pos = inputClause.pos;
        // function(_Frame frame) returns _Frame|error? { return frame; }
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;

        // frame["x"] = x;, note: stmts will get added in reverse order.
        List<BVarSymbol> symbols = getIntroducedSymbols((BLangVariable)
                inputClause.variableDefinitionNode.getVariable());
        shadowSymbolScope(pos, body, ASTBuilderUtil.createVariableRef(pos, frameSymbol), symbols);

        // int x = <int> frame["value"];, note: stmts will get added in reverse order.
        BLangFieldBasedAccess valueAccessExpr = desugar.getValueAccessExpression(inputClause.pos,
                symTable.anyOrErrorType, frameSymbol);
        valueAccessExpr.expr = desugar.addConversionExprIfRequired(valueAccessExpr.expr,
                types.getSafeType(valueAccessExpr.expr.type, true, false));
        VariableDefinitionNode variableDefinitionNode = inputClause.variableDefinitionNode;
        BLangVariable variable = (BLangVariable) variableDefinitionNode.getVariable();
        variable.setInitialExpression(desugar.addConversionExprIfRequired(valueAccessExpr, inputClause.varType));
        // add at 0, otherwise, this goes under existing stmts.
        body.stmts.add(0, (BLangStatement) variableDefinitionNode);

        // at this point;
        // function(_Frame frame) returns _Frame|error? {
        //      int x = <int> frame["value"];
        //      frame["x"] = x;
        //      return frame;
        // }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_INPUT_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar joinClauses / nested fromClauses to below and return a reference to created join _StreamFunction.
     * _StreamFunction joinFunc = createJoinFunction(joinPipeline);
     *
     * @param blockStmt    parent block to write to.
     * @param joinPipeline previously created _StreamPipeline reference to be joined.
     * @return variableReference to created join _StreamFunction.
     */
    BLangVariableReference addJoinFunction(BLangBlockStmt blockStmt, BLangVariableReference joinPipeline) {
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_JOIN_FUNCTION,
                Lists.of(joinPipeline), joinPipeline.pos);
    }

    /**
     * Desugar letClause to below and return a reference to created let _StreamFunction.
     * _StreamFunction ysLet = createLetFunction(function(_Frame frame) returns _Frame|error? {
     * frame["y2"] = <int> frame["y"] * <int> frame["y"];
     * return frame;
     * });
     *
     * @param blockStmt parent block to write to.
     * @param letClause to be desugared.
     * @return variableReference to created let _StreamFunction.
     */
    BLangVariableReference addLetFunction(BLangBlockStmt blockStmt, BLangLetClause letClause) {
        DiagnosticPos pos = letClause.pos;
        // function(_Frame frame) returns _Frame|error? { return frame; }
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;

        // frame["x"] = x;, note: stmts will get added in reverse order.
        List<BVarSymbol> symbols = getIntroducedSymbols(letClause);
        shadowSymbolScope(pos, body, ASTBuilderUtil.createVariableRef(pos, frameSymbol), symbols);

        Collections.reverse(letClause.letVarDeclarations);
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            // add at 0, otherwise, this goes under existing stmts.
            body.stmts.add(0, (BLangStatement) letVariable.definitionNode);
        }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_LET_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar whereClause to below and return a reference to created filter _StreamFunction.
     * _StreamFunction xsFilter = createFilterFunction(function(_Frame frame) returns boolean {
     * return <int>frame["x"] > 0;
     * });
     *
     * @param blockStmt   parent block to write to.
     * @param whereClause to be desugared.
     * @return variableReference to created filter _StreamFunction.
     */
    BLangVariableReference addFilterFunction(BLangBlockStmt blockStmt, BLangWhereClause whereClause) {
        return addFilterFunction(whereClause.pos, whereClause.expression, blockStmt);
    }

    /**
     * Desugar onClause to below and return a reference to created filter _StreamFunction.
     * _StreamFunction xsFilter = createFilterFunction(function(_Frame frame) returns boolean {
     * return <int>frame["x"] > 0;
     * });
     *
     * @param blockStmt parent block to write to.
     * @param onClause  to be desugared.
     * @return variableReference to created filter _StreamFunction.
     */
    BLangVariableReference addFilterFunction(BLangBlockStmt blockStmt, BLangOnClause onClause) {
        return addFilterFunction(onClause.pos, onClause.expression, blockStmt);
    }

    /**
     * Desugar where/on clauses and return a reference to created filter _StreamFunction.
     *
     * @param pos              diagnostic position.
     * @param filterExpression filter expression.
     * @param blockStmt        parent block to write to.
     * @return variableReference to created filter _StreamFunction.
     */
    private BLangVariableReference addFilterFunction(DiagnosticPos pos,
                                                     BLangExpression filterExpression,
                                                     BLangBlockStmt blockStmt) {
        BLangLambdaFunction lambda = createFilterLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = pos;
        returnNode.setExpression(filterExpression);
        body.addStatement(returnNode);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_FILTER_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar selectClause to below and return a reference to created select _StreamFunction.
     * _StreamFunction selectFunc = createSelectFunction(function(_Frame frame) returns _Frame|error? {
     * int x2 = <int> frame["x2"];
     * int y2 = <int> frame["y2"];
     * _Frame frame = {"value": x2 + y2};
     * return frame;
     * });
     *
     * @param blockStmt    parent block to write to.
     * @param selectClause to be desugared.
     * @return variableReference to created select _StreamFunction.
     */
    BLangVariableReference addSelectFunction(BLangBlockStmt blockStmt, BLangSelectClause selectClause) {
        DiagnosticPos pos = selectClause.pos;
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol oldFrameSymbol = lambda.function.requiredParams.get(0).symbol;
        BLangSimpleVarRef frame = ASTBuilderUtil.createVariableRef(pos, oldFrameSymbol);
        // $frame$["$value$"] = select-expr;
        BLangStatement assignment = getAddToFrameStmt(pos, frame, "$value$", selectClause.expression);
        body.stmts.add(body.stmts.size() - 1, assignment);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_SELECT_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar doClause to below and return a reference to created do _StreamFunction.
     * _StreamFunction doFunc = createDoFunction(function(_Frame frame) {
     * int x2 = <int> frame["x2"];
     * int y2 = <int> frame["y2"];
     * });
     *
     * @param blockStmt parent block to write to.
     * @param doClause  to be desugared.
     * @return variableReference to created do _StreamFunction.
     */
    BLangVariableReference addDoFunction(BLangBlockStmt blockStmt, BLangDoClause doClause) {
        DiagnosticPos pos = doClause.pos;
        BLangLambdaFunction lambda = createActionLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        for (BLangStatement stmt : doClause.body.stmts) {
            body.addStatement(stmt);
        }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_DO_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar to following invocation.
     * stream:addStreamFunction(pipeline, streamFunction);
     *
     * @param blockStmt   parent block to write to.
     * @param pipelineRef variableReference to pipeline.
     * @param functionRef variableReference to stream function.
     */
    void addStreamFunction(BLangBlockStmt blockStmt, BLangVariableReference pipelineRef,
                           BLangVariableReference functionRef) {
        BLangInvocation addStreamFunctionInvocation = createQueryLibInvocation(QUERY_ADD_STREAM_FUNCTION,
                Lists.of(pipelineRef, functionRef), pipelineRef.pos);
        BLangExpressionStmt stmt = ASTBuilderUtil.createExpressionStmt(pipelineRef.pos, blockStmt);
        stmt.expr = addStreamFunctionInvocation;
    }

    /**
     * Desugar to following invocation.
     * stream<any|error, error?> result = xsPipeline.getStream();
     *
     * @param blockStmt   parent block to write to.
     * @param pipelineRef variableReference to pipeline.
     * @return variableReference to stream.
     */
    BLangVariableReference addGetStreamFromPipeline(BLangBlockStmt blockStmt, BLangVariableReference pipelineRef) {
        DiagnosticPos pos = pipelineRef.pos;
        // TODO: instead of null, send the expected type;??
        // TODO: for now type will be stream<any|error, error?> ; we can pass the expected type and add a cast
        BLangVariableReference streamVarRef = getStreamFunctionVariableRef(blockStmt,
                QUERY_GET_STREAM_FROM_PIPELINE_FUNCTION, null, Lists.of(pipelineRef), pos);
        return streamVarRef;
    }

    @Override
    public void visit(BLangLambdaFunction lambda) {
        BLangFunction function = lambda.function;
        currentFrameSymbol = function.requiredParams.get(0).symbol;
        identifiers = new HashMap<>();
        currentLambdaBody = (BLangBlockFunctionBody) function.getBody();
        List<BLangStatement> stmts = new ArrayList<>(currentLambdaBody.getStatements());
        stmts.forEach(stmt -> {
            stmt.accept(this);
        });
        currentFrameSymbol = null;
        identifiers = null;
        currentLambdaBody = null;
    }

    @Override
    public void visit(BLangSimpleVariableDef bLangSimpleVariableDef) {
        bLangSimpleVariableDef.getVariable().accept(this);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        bLangRecordVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.variableList.forEach(v -> v.getValue().accept(this));
        if (bLangRecordVariable.expr != null) {
            bLangRecordVariable.expr.accept(this);
        }
        if (bLangRecordVariable.hasRestParam()) {
            ((BLangNode) bLangRecordVariable.restParam).accept(this);
        }
    }

    @Override
    public void visit(BLangSimpleVariable bLangSimpleVariable) {
        identifiers.putIfAbsent(bLangSimpleVariable.name.value, bLangSimpleVariable.symbol);
        if (bLangSimpleVariable.expr != null) {
            bLangSimpleVariable.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr.accept(this);
        if (fieldAccessExpr.impConversionExpr != null) {
            fieldAccessExpr.impConversionExpr.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        if (invocationExpr.langLibInvocation) {
            requiredArgs = requiredArgs.subList(1, requiredArgs.size());
        }
        requiredArgs.forEach(arg -> arg.accept(this));
        invocationExpr.restArgs.forEach(arg -> arg.accept(this));
        if (invocationExpr.expr != null) {
            invocationExpr.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        // do nothing;
    }

    @Override
    public void visit(BLangReturn bLangReturn) {
        bLangReturn.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr bLangBinaryExpr) {
        bLangBinaryExpr.lhsExpr.accept(this);
        bLangBinaryExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangAssignment bLangAssignment) {
        bLangAssignment.varRef.accept(this);
        bLangAssignment.expr.accept(this);
    }

    @Override
    public void visit(BLangRecordLiteral bLangRecordLiteral) {
        for (RecordLiteralNode.RecordField field : bLangRecordLiteral.fields) {
            ((BLangNode) field).accept(this);
        }
    }

    @Override
    public void visit(BLangRecordKeyValueField recordKeyValue) {
        recordKeyValue.key.expr.accept(this);
        recordKeyValue.valueExpr.accept(this);
    }

    @Override
    public void visit(BLangRecordSpreadOperatorField spreadOperatorField) {
        spreadOperatorField.expr.accept(this);
    }

    public void visit(BLangConstRef constRef) {
       //do nothing
    }

    public void visit(BLangNumericLiteral literalExpr) {
        //do nothing
    }

    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.expressions.forEach(expression -> expression.accept(this));
        if (varRefExpr.restParam != null) {
            BLangExpression restExpr = (BLangExpression) varRefExpr.restParam;
            restExpr.accept(this);
        }
    }

    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.recordRefFields.forEach(recordVarRefKeyValue
                -> recordVarRefKeyValue.variableReference.accept(this));
        if (varRefExpr.restParam != null) {
            BLangExpression restExpr = (BLangExpression) varRefExpr.restParam;
            restExpr.accept(this);
        }
    }

    public void visit(BLangErrorVarRef varRefExpr) {
        if (varRefExpr.reason != null) {
            varRefExpr.reason.accept(this);
        }
        if (varRefExpr.restVar != null) {
            varRefExpr.restVar.accept(this);
        }
        varRefExpr.detail.forEach(bLangNamedArgsExpression -> bLangNamedArgsExpression.accept(this));
    }

    public void visit(BLangSimpleVarRef bLangSimpleVarRef) {
        BSymbol symbol = bLangSimpleVarRef.symbol;
        BSymbol resolvedSymbol = symResolver
                .lookupClosureVarSymbol(env, names.fromIdNode(bLangSimpleVarRef.variableName),
                        SymTag.VARIABLE);
        if (symbol != null && resolvedSymbol == symTable.notFoundSymbol) {
            String identifier = bLangSimpleVarRef.variableName.getValue();
            if (!FRAME_PARAMETER_NAME.equals(identifier) && !identifiers.containsKey(identifier)) {
                DiagnosticPos pos = currentLambdaBody.pos;
                BLangFieldBasedAccess frameAccessExpr = desugar.getFieldAccessExpression(pos, identifier,
                        symTable.anyOrErrorType, currentFrameSymbol);
                frameAccessExpr.expr = desugar.addConversionExprIfRequired(frameAccessExpr.expr,
                        types.getSafeType(frameAccessExpr.expr.type, true, false));

                if (symbol instanceof BVarSymbol) {
                    ((BVarSymbol) symbol).originalSymbol = null;
                    BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, identifier, symbol.type,
                            desugar.addConversionExprIfRequired(frameAccessExpr, symbol.type), (BVarSymbol) symbol);
                    BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
                    currentLambdaBody.stmts.add(0, variableDef);
                }
                identifiers.put(identifier, symbol);
            }
        } else if (resolvedSymbol != symTable.notFoundSymbol) {
            resolvedSymbol.closure = true;
        }
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.indexExpr.accept(this);
        indexAccessExpr.expr.accept(this);
    }

    public void visit(BLangTypeInit connectorInitExpr) {
        connectorInitExpr.argsExpr.forEach(arg -> arg.accept(this));
        connectorInitExpr.initInvocation.accept(this);
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        actionInvocationExpr.argExprs.forEach(arg -> arg.accept(this));
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr.accept(this);
        ternaryExpr.elseExpr.accept(this);
        ternaryExpr.thenExpr.accept(this);
    }

    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.exprList.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr.accept(this);
    }

    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.lhsExpr.accept(this);
        elvisExpr.rhsExpr.accept(this);
    }

    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression.accept(this);
    }

    public void visit(BLangLetExpression letExpr) {
        letExpr.expr.accept(this);
        letExpr.letVarDeclarations.forEach(var -> ((BLangNode) var.definitionNode).accept(this));
    }

    public void visit(BLangLetVariable letVariable) {
        //do nothing
    }

    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        tupleLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr.accept(this);
    }

    public void visit(BLangTypedescExpr accessExpr) {
    }

    public void visit(BLangXMLQName xmlQName) {
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.startTagName.accept(this);
        xmlElementLiteral.endTagName.accept(this);
        xmlElementLiteral.attributes.forEach(bLangXMLAttribute -> bLangXMLAttribute.accept(this));
        xmlElementLiteral.children.forEach(child -> child.accept(this));
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.textFragments.forEach(fragment -> fragment.accept(this));
        if (xmlTextLiteral.concatExpr != null) {
            xmlTextLiteral.concatExpr.accept(this);
        }
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.textFragments.forEach(fragment -> fragment.accept(this));
        if (xmlCommentLiteral.concatExpr != null) {
            xmlCommentLiteral.concatExpr.accept(this);
        }
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.dataFragments.forEach(fragment -> fragment.accept(this));
        if (xmlProcInsLiteral.dataConcatExpr != null) {
            xmlProcInsLiteral.dataConcatExpr.accept(this);
        }
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.textFragments.forEach(fragment -> fragment.accept(this));
        if (xmlQuotedString.concatExpr != null) {
            xmlQuotedString.concatExpr.accept(this);
        }
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.params.forEach(param -> param.accept(this));
        bLangArrowFunction.function.accept(this);
        bLangArrowFunction.body.accept(this);
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.startExpr.accept(this);
        intRangeExpression.endExpr.accept(this);
    }

    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        bLangVarArgsExpression.expr.accept(this);
    }

    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.expr.accept(this);
    }

    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr.accept(this);
    }

    public void visit(BLangMatchExpression bLangMatchExpression) {
        bLangMatchExpression.expr.accept(this);
        bLangMatchExpression.patternClauses.forEach(bLangMatchExprPatternClause ->
                bLangMatchExpression.patternClauses.forEach(pattern -> pattern.expr.accept(this)));
        bLangMatchExpression.patternClauses.forEach(bLangMatchExprPatternClause ->
                bLangMatchExpression.patternClauses.forEach(pattern -> pattern.variable.accept(this)));
        bLangMatchExpression.patternClauses.forEach(bLangMatchExprPatternClause ->
                bLangMatchExpression.expr.accept(this));
    }

    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
    }

    public void visit(BLangCheckedExpr checkedExpr) {
        checkedExpr.expr.accept(this);
    }

    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        checkPanickedExpr.expr.accept(this);
    }

    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        serviceConstructorExpr.serviceNode.accept(this);
    }

    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr.accept(this);
    }

    public void visit(BLangIsLikeExpr typeTestExpr) {
        typeTestExpr.expr.accept(this);
    }

    public void visit(BLangIgnoreExpr ignoreExpr) {
    }

    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
    }

    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        bLangXMLSequenceLiteral.xmlItems.forEach(item -> item.accept(this));
    }

    public void visit(BLangStatementExpression bLangStatementExpression) {
         bLangStatementExpression.expr.accept(this);
         bLangStatementExpression.stmt.accept(this);
    }

    public void visit(BLangTupleVariable bLangTupleVariable) {
        if (bLangTupleVariable.restVariable != null) {
            bLangTupleVariable.restVariable.accept(this);
        }
        bLangTupleVariable.memberVariables.forEach(var -> var.accept(this));
    }

    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        if (bLangTupleVariableDef.var.restVariable != null) {
            bLangTupleVariableDef.var.restVariable.accept(this);
        }
        if (bLangTupleVariableDef.var.expr != null) {
            bLangTupleVariableDef.var.expr.accept(this);
        }
        if (bLangTupleVariableDef.var.memberVariables != null) {
            bLangTupleVariableDef.var.memberVariables.forEach(var -> var.accept(this));
        }
    }

    public void visit(BLangErrorVariable bLangErrorVariable) {
        if (bLangErrorVariable.reason != null) {
            bLangErrorVariable.reason.accept(this);
        }
        bLangErrorVariable.detail.forEach(var -> var.valueBindingPattern.accept(this));
        if (bLangErrorVariable.restDetail != null) {
            bLangErrorVariable.restDetail.accept(this);
        }
        if (bLangErrorVariable.detailExpr != null) {
            bLangErrorVariable.detailExpr.accept(this);
        }
    }

    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        bLangErrorVariableDef.errorVariable.accept(this);
    }

    public void visit(BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        bLangMatchStmtStaticBindingPatternClause.literal.accept(this);
    }

    public void visit(BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
        if (bLangMatchStmtStructuredBindingPatternClause.bindingPatternVariable != null) {
            bLangMatchStmtStructuredBindingPatternClause.bindingPatternVariable.accept(this);
        }
        if (bLangMatchStmtStructuredBindingPatternClause.typeGuardExpr != null) {
            bLangMatchStmtStructuredBindingPatternClause.typeGuardExpr.accept(this);
        }
    }

    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
    }

    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        waitForAllExpr.keyValuePairs.forEach(pair -> pair.accept(this));
    }

    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
    }


    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
    }

    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        waitKeyValue.key.accept(this);
        waitKeyValue.valueExpr.accept(this);
    }

    public void visit(BLangXMLElementFilter xmlElementFilter) {
    }

    public void visit(BLangXMLElementAccess xmlElementAccess) {
    }

    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        if (xmlNavigation.childIndex != null) {
            xmlNavigation.childIndex.accept(this);
        }
    }

    //statements
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(statement -> statement.accept(this));
    }

    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        lockStmtNode.body.accept(this);
    }

    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        unLockNode.body.accept(this);
    }

    public void visit(BLangCompoundAssignment compoundAssignNode) {
        if (compoundAssignNode.expr != null) {
            compoundAssignNode.expr.accept(this);
        }
        if (compoundAssignNode.modifiedExpr != null) {
            compoundAssignNode.modifiedExpr.accept(this);
        }
        if (compoundAssignNode.varRef != null) {
            compoundAssignNode.varRef.accept(this);
        }
    }

    public void visit(BLangAbort abortNode) {
    }

    public void visit(BLangRetry retryNode) {
    }

    public void visit(BLangContinue continueNode) {
    }

    public void visit(BLangBreak breakNode) {
    }

    public void visit(BLangThrow throwNode) {
        throwNode.expr.accept(this);
    }

    public void visit(BLangPanic panicNode) {
        panicNode.expr.accept(this);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    public void visit(BLangIf ifNode) {
        ifNode.expr.accept(this);
        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
    }

    public void visit(BLangQueryAction queryAction) {
    }

    public void visit(BLangMatch matchNode) {
        matchNode.expr.accept(this);
        matchNode.patternClauses.forEach(pattern -> pattern.accept(this));
    }

    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        patternClauseNode.body.accept(this);
        patternClauseNode.matchExpr.accept(this);
        patternClauseNode.variable.accept(this);
    }

    public void visit(BLangForeach foreach) {
        throw new AssertionError();
    }

    public void visit(BLangFromClause fromClause) {
    }

    public void visit(BLangLetClause letClause) {
    }

    public void visit(BLangSelectClause selectClause) {
    }

    public void visit(BLangWhereClause whereClause) {
    }

    public void visit(BLangDoClause doClause) {
    }

    public void visit(BLangWhile whileNode) {
        whileNode.expr.accept(this);
        whileNode.body.accept(this);
    }

    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
    }

    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
        if (transactionNode.abortedBody != null) {
            transactionNode.abortedBody.accept(this);
        }
        if (transactionNode.committedBody != null) {
            transactionNode.committedBody.accept(this);
        }
        if (transactionNode.onRetryBody != null) {
            transactionNode.onRetryBody.accept(this);
        }
        if (transactionNode.retryCount != null) {
            transactionNode.retryCount.accept(this);
        }
    }

    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        tryNode.catchBlocks.forEach(block -> block.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
    }

    public void visit(BLangTupleDestructure stmt) {
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    public void visit(BLangRecordDestructure stmt) {
        stmt.expr.accept(this);
        stmt.varRef.accept(this);
    }

    public void visit(BLangErrorDestructure stmt) {
        stmt.expr.accept(this);
        stmt.varRef.accept(this);
    }

    public void visit(BLangCatch catchNode) {
        catchNode.param.accept(this);
        catchNode.body.accept(this);
    }

    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(worker -> worker.accept(this));
    }

    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr.accept(this);
        if (workerSendNode.keyExpr != null) {
            workerSendNode.keyExpr.accept(this);
        }
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.sendExpression.accept(this);
        if (workerReceiveNode.keyExpr != null) {
            workerReceiveNode.keyExpr.accept(this);
        }
    }

    /**
     * Create and return a lambda `function(_Frame frame) returns _Frame|error? {...; return frame;}`
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createPassthroughLambda(DiagnosticPos pos) {
        // returns (_Frame|error)?
        BLangUnionTypeNode returnType = getUnionTypeNode();
        // return frame;
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = pos;
        return createLambdaFunction(pos, returnType, returnNode, true);
    }

    /**
     * Create and return a lambda `function(_Frame frame) returns boolean {...}`.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createFilterLambda(DiagnosticPos pos) {
        // returns boolean
        BLangValueType returnType = getBooleanTypeNode();
        return createLambdaFunction(pos, returnType, null, false);
    }

    /**
     * Create and return a lambda `function(_Frame frame) {...}`.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createActionLambda(DiagnosticPos pos) {
        // returns ()
        BLangValueType returnType = getNilTypeNode();
        return createLambdaFunction(pos, returnType, null, false);
    }

    /**
     * Creates and return a lambda function without body.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createLambdaFunction(DiagnosticPos pos,
                                                     TypeNode returnType,
                                                     BLangReturn returnNode,
                                                     boolean isPassthrough) {
        // function(_Frame frame) ... and ref to frame
        BType frameType = getFrameTypeSymbol().type;
        BVarSymbol frameSymbol = new BVarSymbol(0, names.fromString(FRAME_PARAMETER_NAME),
                this.env.scope.owner.pkgID, frameType, this.env.scope.owner);
        BLangSimpleVariable frameVariable = ASTBuilderUtil.createVariable(pos, null,
                frameSymbol.type, null, frameSymbol);
        BLangVariableReference frameVarRef = ASTBuilderUtil.createVariableRef(pos, frameSymbol);

        // lambda body
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();

        // add `return x;`
        if (returnNode != null) {
            // passthrough will return same frame parameter
            if (isPassthrough) {
                returnNode.setExpression(frameVarRef);
            }
            body.addStatement(returnNode);
        }
        return createLambdaFunction(pos, Lists.of(frameVariable), returnType, body);
    }

    /**
     * Creates and returns a lambda function.
     *
     * @param pos            diagnostic pos.
     * @param requiredParams required parameters.
     * @param returnType     return type of the lambda function.
     * @param lambdaBody     body of the lambda function.
     * @return created lambda function.
     */
    private BLangLambdaFunction createLambdaFunction(DiagnosticPos pos,
                                                     List<BLangSimpleVariable> requiredParams,
                                                     TypeNode returnType,
                                                     BLangFunctionBody lambdaBody) {
        return desugar.createLambdaFunction(pos, "$streamLambda$",
                requiredParams, returnType, lambdaBody);
    }

    /**
     * Creates a variable to hold what function invocation returns,
     * and then return a varRef to that variable.
     *
     * @param blockStmt    parent block to write the varDef into.
     * @param functionName function name.
     * @param requiredArgs required args.
     * @param pos          pos diagnostic pos.
     * @return varRef to the created variable.
     */
    private BLangVariableReference getStreamFunctionVariableRef(BLangBlockStmt blockStmt,
                                                                Name functionName,
                                                                List<BLangExpression> requiredArgs,
                                                                DiagnosticPos pos) {
        return getStreamFunctionVariableRef(blockStmt, functionName, null, requiredArgs, pos);
    }

    /**
     * Creates a variable to hold what function invocation returns,
     * and then return a varRef to that variable.
     *
     * @param blockStmt    parent block to write the varDef into.
     * @param functionName function name.
     * @param type         expected type of the variable.
     * @param requiredArgs required args.
     * @param pos          pos diagnostic pos.
     * @return varRef to the created variable.
     */
    private BLangVariableReference getStreamFunctionVariableRef(BLangBlockStmt blockStmt,
                                                                Name functionName,
                                                                BType type,
                                                                List<BLangExpression> requiredArgs,
                                                                DiagnosticPos pos) {
        String name = getNewVarName();
        BLangInvocation queryLibInvocation = createQueryLibInvocation(functionName, requiredArgs, pos);
        type = (type == null) ? queryLibInvocation.type : type;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(name), env.scope.owner.pkgID, type, env.scope.owner);
        BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, name, type,
                desugar.addConversionExprIfRequired(queryLibInvocation, type), varSymbol);
        BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
        blockStmt.addStatement(variableDef);
        return ASTBuilderUtil.createVariableRef(pos, variable.symbol);
    }

    /**
     * Get unique variable name.
     *
     * @return new variable name.
     */
    private String getNewVarName() {
        return "$streamElement$" + streamElementCount++;
    }

    /**
     * Load a function invokable symbol and return a invocation for that function.
     *
     * @param functionName function name.
     * @param requiredArgs list of required args.
     * @param pos          diagnostic pos.
     * @return created invocation.
     */
    private BLangInvocation createQueryLibInvocation(Name functionName,
                                                     List<BLangExpression> requiredArgs,
                                                     DiagnosticPos pos) {
        BInvokableSymbol symbol = getQueryLibInvokableSymbol(functionName);
        BLangInvocation bLangInvocation = ASTBuilderUtil
                .createInvocationExprForMethod(pos, symbol, requiredArgs, symResolver);
        bLangInvocation.type = symbol.retType;
        return bLangInvocation;
    }

    /**
     * Load and return symbol for given functionName in query lib.
     *
     * @param functionName of the function.
     * @return symbol for the function.
     */
    private BInvokableSymbol getQueryLibInvokableSymbol(Name functionName) {
        return (BInvokableSymbol) symTable.langQueryModuleSymbol.scope
                .lookup(functionName).symbol;
    }

    private BLangStatement getAddToFrameStmt(DiagnosticPos pos,
                                             BLangVariableReference frame,
                                             String key,
                                             BLangExpression value) {
        BLangIdentifier valueIdentifier = ASTBuilderUtil.createIdentifier(pos, key);
        BLangFieldBasedAccess valueAccess = ASTBuilderUtil.createFieldAccessExpr(frame, valueIdentifier);
        valueAccess.pos = pos;
        valueAccess.type = symTable.anyOrErrorType;
        valueAccess.originalType = valueAccess.type;
        return ASTBuilderUtil.createAssignmentStmt(pos, valueAccess, value);
    }

    private void shadowSymbolScope(DiagnosticPos pos,
                                   BLangBlockFunctionBody lambdaBody,
                                   BLangSimpleVarRef frameRef,
                                   List<BVarSymbol> symbols) {
        Collections.reverse(symbols);
        for (BVarSymbol symbol : symbols) {
            // since the var decl is now within lambda, remove scope entry from encl env.
            env.scope.entries.remove(symbol.name);
            BLangStatement addToFrameStmt = getAddToFrameStmt(pos, frameRef,
                    symbol.name.value, ASTBuilderUtil.createVariableRef(pos, symbol));
            lambdaBody.stmts.add(0, addToFrameStmt);
        }
    }

    private List<BVarSymbol> getIntroducedSymbols(BLangLetClause letClause) {
        List<BVarSymbol> symbols = new ArrayList<>();
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            symbols.addAll(getIntroducedSymbols(letVariable));
        }
        return symbols;
    }

    private List<BVarSymbol> getIntroducedSymbols(BLangLetVariable variable) {
        return getIntroducedSymbols((BLangVariable) variable.definitionNode.getVariable());
    }

    private List<BVarSymbol> getIntroducedSymbols(BLangVariable variable) {
        if (variable != null) {
            List<BVarSymbol> symbols = new ArrayList<>();
            if (variable.getKind() == NodeKind.RECORD_VARIABLE) {
                // Record binding
                BLangRecordVariable record = (BLangRecordVariable) variable;
                for (BLangRecordVariable.BLangRecordVariableKeyValue keyValue : record.variableList) {
                    symbols.addAll(getIntroducedSymbols(keyValue.valueBindingPattern));
                }
                if (record.hasRestParam()) {
                    symbols.addAll(getIntroducedSymbols((BLangVariable) record.restParam));
                }
            } else if (variable.getKind() == NodeKind.TUPLE_VARIABLE) {
                // Tuple binding
                BLangTupleVariable tuple = (BLangTupleVariable) variable;
                for (BLangVariable memberVariable : tuple.memberVariables) {
                    symbols.addAll(getIntroducedSymbols(memberVariable));
                }
                if (tuple.restVariable != null) {
                    symbols.addAll(getIntroducedSymbols(tuple.restVariable));
                }
            } else if (variable.getKind() == NodeKind.ERROR_VARIABLE) {
                // Error binding
                BLangErrorVariable error = (BLangErrorVariable) variable;
                if (error.reason != null) {
                    symbols.addAll(getIntroducedSymbols(error.reason));
                }
                if (error.restDetail != null) {
                    symbols.addAll(getIntroducedSymbols(error.restDetail));
                }
                for (BLangErrorVariable.BLangErrorDetailEntry entry : error.detail) {
                    symbols.addAll(getIntroducedSymbols(entry.valueBindingPattern));
                }
            } else {
                // Simple binding
                symbols.add(((BLangSimpleVariable) variable).symbol);
            }
            return symbols;
        }
        return Collections.emptyList();
    }

    /**
     * Return BLangValueType of a nil `()` type.
     *
     * @return a nil type node.
     */
    BLangValueType getNilTypeNode() {
        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.typeKind = TypeKind.NIL;
        nilTypeNode.type = symTable.nilType;
        return nilTypeNode;
    }

    /**
     * Return BLangValueType of a any type.
     *
     * @return a any type node.
     */
    BLangValueType getAnyTypeNode() {
        BLangValueType anyTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        anyTypeNode.typeKind = TypeKind.ANY;
        anyTypeNode.type = symTable.anyType;
        return anyTypeNode;
    }

    /**
     * Return BLangErrorType node.
     *
     * @return a error type node.
     */
    BLangErrorType getErrorTypeNode() {
        BLangErrorType errorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorTypeNode.type = symTable.errorType;
        return errorTypeNode;
    }

    /**
     * Return BLangValueType of a boolean type.
     *
     * @return a boolean type node.
     */
    private BLangValueType getBooleanTypeNode() {
        BLangValueType booleanTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        booleanTypeNode.typeKind = TypeKind.BOOLEAN;
        booleanTypeNode.type = symTable.booleanType;
        return booleanTypeNode;
    }

    /**
     * Return union type node consists of _Frame & error & ().
     *
     * @return a union type node.
     */
    private BLangUnionTypeNode getUnionTypeNode() {
        BType frameType = getFrameTypeSymbol().type;
        BUnionType unionType = BUnionType.create(null, frameType, symTable.errorType, symTable.nilType);
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.type = unionType;
        unionTypeNode.desugared = true;
        unionTypeNode.memberTypeNodes.add(getFrameTypeNode());
        unionTypeNode.memberTypeNodes.add(getErrorTypeNode());
        unionTypeNode.memberTypeNodes.add(getNilTypeNode());
        return unionTypeNode;
    }

    /**
     * Return _Frame type node.
     *
     * @return a _Frame type node.
     */
    private BLangRecordTypeNode getFrameTypeNode() {
        BRecordTypeSymbol frameTypeSymbol = getFrameTypeSymbol();
        BRecordType frameType = (BRecordType) frameTypeSymbol.type;

        BLangUnionTypeNode restFieldType = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        restFieldType.type = frameType.restFieldType;
        restFieldType.memberTypeNodes.add(getErrorTypeNode());
        restFieldType.memberTypeNodes.add(getAnyTypeNode());

        BLangRecordTypeNode frameTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        frameTypeNode.type = frameType;
        frameTypeNode.restFieldType = restFieldType;
        frameTypeNode.symbol = frameType.tsymbol;
        frameTypeNode.desugared = true;
        return frameTypeNode;
    }

    /**
     * Load and return symbol for _Frame.
     *
     * @return _Frame type symbol.
     */
    private BRecordTypeSymbol getFrameTypeSymbol() {
        return (BRecordTypeSymbol) symTable.langQueryModuleSymbol
                .scope.lookup(names.fromString("_Frame")).symbol;
    }

}
