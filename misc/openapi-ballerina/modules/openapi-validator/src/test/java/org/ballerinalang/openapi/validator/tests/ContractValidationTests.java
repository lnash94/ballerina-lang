package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
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

public class ContractValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/contractValidation/")
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

    @Test(description = "Test for missing path in resource file")
    public void testUndocumentedPath() throws OpenApiValidatorException, UnsupportedEncodingException {

        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("resourceValidation/ballerina/valid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("resourceValidation/ballerina/valid/petstore.bal");
    }

}
