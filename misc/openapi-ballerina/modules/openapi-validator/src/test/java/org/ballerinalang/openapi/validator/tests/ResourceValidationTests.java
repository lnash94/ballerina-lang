package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResourceValidation;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ResourceValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/resourceValidation/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private BLangService extractBLangservice;
    private List<String> tag = new ArrayList<>();
    private List<String> operation = new ArrayList<>();
    private List<String> excludeTag = new ArrayList<>();
    private List<String> excludeOperation = new ArrayList<>();
    private Diagnostic.Kind kind;
    private DiagnosticLog dLog;

    @Test(description = "test for undocumented Path in contract")
    public void testUndocumentedPath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceValidation/ballerina/valid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("resourceValidation/ballerina/valid/petstore.bal");
        ResourceValidation.validateResource(api, extractBLangservice, tag, operation, excludeTag, excludeOperation,
                kind, dLog);
    }
    @Test(description = "test for undocumented Method in contract")
    public void testUndocumentedMethod() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreMethod.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceValidation/ballerina/valid/petstoreMethod.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("resourceValidation/ballerina/valid/petstoreMethod.bal");
        ResourceValidation.validateResource(api, extractBLangservice, tag, operation, excludeTag, excludeOperation,
                kind, dLog);
    }

    @Test(description = "test for undocumented TypeMisMatch in contract")
    public void testParameterTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreParameterTM.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceValidation/ballerina/valid/petstoreParameterTM.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("resourceValidation/ballerina/valid/petstoreParameterTM.bal");
        ResourceValidation.validateResource(api, extractBLangservice, tag, operation, excludeTag, excludeOperation,
                kind, dLog);
    }

    @Test(description = "test for undocumented TypeMisMatch in contract")
    public void testRecordTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordType.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceValidation/ballerina/invalid/petstoreRecordType.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("resourceValidation/ballerina/invalid/petstoreRecordType.bal");
        ResourceValidation.validateResource(api, extractBLangservice, tag, operation, excludeTag, excludeOperation,
                kind, dLog);
    }

}
