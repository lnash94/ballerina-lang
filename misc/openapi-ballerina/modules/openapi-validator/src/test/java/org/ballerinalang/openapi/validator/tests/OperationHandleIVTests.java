package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.ballerinalang.openapi.validator.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.OperationToResourceFunction;
import org.ballerinalang.openapi.validator.ResourceMethod;
import org.ballerinalang.openapi.validator.TypeMismatch;
import org.ballerinalang.openapi.validator.ValidationError;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OperationHandleIVTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/operationHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private BLangService extractBLangservice;
    private List<ValidationError> validationErrors = new ArrayList<>();
    private ResourceMethod resourceMethod;
    private Operation operation;

    @Test(description = "Operation model has path parameters with request body ")
    public void testRBwithPath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRBwithPathParameter.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "operationHandle/ballerina/invalid/petstoreRBwithPathParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        validationErrors = OperationToResourceFunction.validate(operation, resourceMethod);
        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);

    }

    @Test(description = "Operation model has path parameters with request body ")
    public void testExtraRB() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreExtraRBParameter.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "operationHandle/ballerina/invalid/petstoreExtraRBParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        validationErrors = OperationToResourceFunction.validate(operation, resourceMethod);
//        Assert.assertTrue(validationErrors.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrors.get(0).getFieldName(), "appilcation/xml");
    }

    @Test(description = "Operation model has OneOf parameters with request body ")
    public void testOneOfRB() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreOneOfTypeMismatch.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage(
                "operationHandle/ballerina/invalid/petstoreOneOfTypeMismatch.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        resourceMethod = ValidatorTest.getFunction(extractBLangservice, "post");
        operation = api.getPaths().get("/pets/{petId}").getPost();
        validationErrors = OperationToResourceFunction.validate(operation, resourceMethod);
        Assert.assertTrue(validationErrors.get(0) instanceof OneOfTypeValidation);
        Assert.assertEquals(((OneOfTypeValidation) validationErrors.get(0)).getBlockErrors().get(0).getFieldName(), "bark");
    }
}
