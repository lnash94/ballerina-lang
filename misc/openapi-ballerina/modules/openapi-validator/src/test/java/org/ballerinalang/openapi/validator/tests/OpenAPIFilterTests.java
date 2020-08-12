package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.openapi.validator.Filters;
import org.ballerinalang.openapi.validator.OpenAPIFilter;
import org.ballerinalang.openapi.validator.OpenAPIPathSummary;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ValidatorUtil;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OpenAPIFilterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/contractValidation/")
            .toAbsolutePath();
    private OpenAPI api;
    private List<String> tag = new ArrayList<>();
    private List<String> operation = new ArrayList<>();
    private List<String> excludeTag = new ArrayList<>();
    private List<String> excludeOperation = new ArrayList<>();

    @Test(description = "")
    public void test() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ValidatorUtil.parseOpenAPIFile(contractPath.toString());
        Diagnostic.Kind kind  = Diagnostic.Kind.ERROR;
//        operation.add("showUser");
//        operation.add("postPet");
//        operation.add("listPets");

//        tag.add("pets");
//        excludeTag.add("pets");
//        excludeOperation.add("showUser");
//        excludeOperation.add("postPet");

        excludeTag.add("pets");

        Filters filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        List<OpenAPIPathSummary> results = OpenAPIFilter.filterOpenapi(api, filters);
        System.out.println(results);

    }
}
