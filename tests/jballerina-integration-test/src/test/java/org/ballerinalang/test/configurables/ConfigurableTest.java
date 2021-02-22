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

package org.ballerinalang.test.configurables;

import io.ballerina.runtime.internal.configurable.ConfigurableConstants;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.packaging.PackerinaTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.ballerinalang.test.context.LogLeecher.LeecherType.ERROR;

/**
 * Test cases for checking configurable variables in Ballerina.
 */
public class ConfigurableTest extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "configurables")
            .toAbsolutePath().toString();
    private static final String negativeTestFileLocation =
            Paths.get(testFileLocation, "negative_tests").toAbsolutePath().toString();
    private BMainInstance bMainInstance;
    private final LogLeecher testsPassed = new LogLeecher("Tests passed");

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testAccessConfigurableVariables() throws BallerinaTestException {
        executeBalCommand("/configurableProject", testsPassed, "run", "main", null);
    }

    @Test
    public void testAccessForImportedModules() throws BallerinaTestException {
        executeBalCommand("/multiModuleProject", testsPassed, "run", "configPkg", null);
    }

    @Test
    public void testBallerinaTestAPIWithConfigurableVariables() throws BallerinaTestException {
        executeBalCommand("/testProject", new LogLeecher("4 passing"), "test", "configPkg", null);
    }

    @Test
    public void testAPIConfigFilePathOverRiding() throws BallerinaTestException {
        executeBalCommand("/testPathProject", new LogLeecher("4 passing"), "test", "configPkg", null);
    }

    @Test
    public void testAPICNegativeTest() throws BallerinaTestException {
        String errorMsg = "configurable feature is yet to be supported for type '(int[] & readonly)[] & readonly' " +
                "used in variable 'configPkg:invalidArr'";
        executeBalCommand("/testErrorProject", new LogLeecher(errorMsg, ERROR), "test",
                "configPkg", null);
    }

    @Test
    public void testEnvironmentVariableBasedConfigFile() throws BallerinaTestException {
        String configFilePath = Paths.get(testFileLocation, "config_files", "Config.toml").toString();
        executeBalCommand("", testsPassed, "run", "envVarPkg", addEnvVariables(configFilePath));
    }

    @Test
    public void testSingleBalFileWithConfigurables() throws BallerinaTestException {
        String filePath = testFileLocation + "/configTest.bal";
        executeBalCommand("", testsPassed, "run", filePath, null);
    }

    @Test
    public void testRecordValueWithModuleClash() throws BallerinaTestException {
        executeBalCommand("/recordModuleProject", testsPassed, "run", "main", null);
    }

    /** Negative test cases. */
    @Test
    public void testNoConfigFile() throws BallerinaTestException {
        Path filePath = Paths.get(negativeTestFileLocation, "no_config.bal").toAbsolutePath();
        LogLeecher errorLeecher = new LogLeecher("error: Value not provided for required configurable variable 'name'",
                ERROR);
        bMainInstance.runMain("run", new String[]{filePath.toString()}, null, new String[]{},
                new LogLeecher[]{errorLeecher}, testFileLocation + "/negative_tests");
        errorLeecher.waitForText(5000);
    }

    @Test
    public void testInvalidTomlFile() throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "invalidTomlFile").toAbsolutePath();
        String tomlError1 = "missing identifier [Config.toml:(0:9,0:9)]";
        String tomlError2 = "missing identifier [Config.toml:(0:20,0:20)]";
        String tomlError3 = "missing identifier [Config.toml:(0:21,0:21)]";
        String errorMsg = "error: invalid `Config.toml` file : ";
        LogLeecher errorLeecher1 = new LogLeecher(errorMsg, ERROR);
        LogLeecher errorLeecher2 = new LogLeecher(tomlError1, ERROR);
        LogLeecher errorLeecher3 = new LogLeecher(tomlError2, ERROR);
        LogLeecher errorLeecher4 = new LogLeecher(tomlError3, ERROR);

        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLeecher1, errorLeecher2, errorLeecher3, errorLeecher4}, projectPath.toString());
        errorLeecher1.waitForText(5000);
        errorLeecher2.waitForText(5000);
        errorLeecher3.waitForText(5000);
        errorLeecher4.waitForText(5000);
    }

    @DataProvider(name = "negative-projects")
    public Object[][] getNegativeTestProjects() {
        return new Object[][]{
                {"invalidComplexArray", "configurable feature is yet to be supported for type " +
                        "'(int[] & readonly)[] & readonly' used in variable 'main:intComplexArr'" },
                {"invalidRecordField", "configurable feature is yet to be supported for field type " +
                        "'string[][]' in variable 'main:testUser' of record 'main:AuthInfo'"},
                {"invalidByteRange", "Value provided for byte variable 'byteVar' is out of range. Expected " +
                        "range is (0-255), found '355'"},
                {"invalidMapType", "configurable feature is yet to be supported for type " +
                        "'map<int> & readonly' used in variable 'main:intMap'"},
                {"invalidTableConstraint", "configurable feature is yet to be supported for table " +
                        "constraint type 'string' used in variable 'main:tab'"}
        };
    }

    @Test(dataProvider = "negative-projects")
    public void testNegativeCasesInProjects(String projectName, String errorMsg) throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, projectName).toAbsolutePath();
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, null, new String[]{},
                new LogLeecher[]{errorLog}, projectPath.toString());
        errorLog.waitForText(5000);
    }

    @Test(dataProvider = "negative-tests")
    public void testNegativeCases(String tomlFileName, String errorMsg) throws BallerinaTestException {
        Path projectPath = Paths.get(negativeTestFileLocation, "configProject").toAbsolutePath();
        Path tomlPath = Paths.get(negativeTestFileLocation, "config_files", tomlFileName  + ".toml").toAbsolutePath();
        LogLeecher errorLog = new LogLeecher(errorMsg, ERROR);
        bMainInstance.runMain("run", new String[]{"main"}, addEnvVariables(tomlPath.toString()), new String[]{},
                new LogLeecher[]{errorLog}, projectPath.toString());
        errorLog.waitForText(5000);
    }

    @DataProvider(name = "negative-tests")
    public Object[][] getNegativeTests() {
        return new Object[][]{
                {"no_module_config", "Value not provided for required configurable variable 'main:stringVar'"},
                {"invalid_org_name", "Value not provided for required configurable variable 'main:stringVar'" },
                {"invalid_org_structure", "invalid module structure found for module 'testOrg.main'. " +
                        "Please provide the module name as '[testOrg.main]'" },
                {"invalid_module_structure", "invalid module structure found for module 'main'. " +
                        "Please provide the module name as '[main]'" },
                {"invalid_sub_module_structure", "invalid module structure found for module 'main.foo'. " +
                        "Please provide the module name as '[main.foo]'" },
                {"required_negative", "Value not provided for required configurable variable 'main:stringVar'"},
                {"primitive_type_error", "configurable variable 'main:intVar' is expected to be of type 'int', " +
                        "but found 'float'"},
                {"primitive_structure_error", "configurable variable 'main:intVar' is expected to be of type 'int', " +
                        "but found 'record'"},
                {"array_type_error", "configurable variable 'main:intArr' is expected to be of type " +
                        "'int[] & readonly', but found 'string'"},
                {"array_structure_error", "configurable variable 'main:intArr' is expected to be of type " +
                        "'int[] & readonly', but found 'record'"},
                {"array_element_structure", "configurable variable 'main:intArr[2]' is expected to be of type 'int'," +
                        " but found 'array'"},
                {"array_multi_type", "configurable variable 'main:intArr[1]' is expected to be of type 'int'," +
                        " but found 'string'"},
                {"additional_field", "additional field 'scopes' provided for configurable variable 'main:testUser'" +
                        " of record 'main:AuthInfo' is not supported"},
                {"missing_record_field", "value not provided for non-defaultable required field 'username' of record" +
                        " 'main:AuthInfo' in configurable variable 'main:testUser'"},
                {"record_type_error", "configurable variable 'main:testUser' is expected to be of type " +
                        "'main:(testOrg/main:0.1.0:AuthInfo & readonly)', but found 'string'"},
                {"record_field_structure_error", "record field 'username' from configurable variable 'main:testUser' " +
                        "is expected to be of type 'string', but found 'record'"},
                {"record_field_type_error", "record field 'username' from configurable variable 'main:testUser' " +
                        "is expected to be of type 'string', but found 'int'"},
                {"missing_table_key", "value required for key 'username' of type 'table<(main:AuthInfo & readonly)>" +
                        " key(username) & readonly' in configurable variable 'main:users'"},
                {"table_type_error", "configurable variable 'main:users' is expected to be of type " +
                        "'table<(main:AuthInfo & readonly)> key(username) & readonly', but found 'record'"},
        };
    }

    // Encrypted Config related tests
    @Test
    public void testSingleBalFileWithEncryptedConfigs() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        executeBalCommand("/encryptedSingleBalFile", testsPassed, "run", "encryptedConfig.bal",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testEncryptedConfigs() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        executeBalCommand("/encryptedConfigProject", testsPassed, "run", "main",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testEncryptedConfigsWithIncorrectSecret() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "incorrectSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to retrieve the encrypted value for variable: " +
                "'main:password' : Given final block not properly padded. Such " +
                "issues can arise if a bad key is used during decryption.", ERROR);
        executeBalCommand("/encryptedConfigProject", runLeecher, "run", "main",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testEncryptedConfigsWithEmptySecret() throws BallerinaTestException {
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "emptySecret.txt").toString();
        LogLeecher runLeecher =
                new LogLeecher("error: failed to initialize the cipher tool due to empty secret text", ERROR);
        executeBalCommand("/encryptedConfigProject", runLeecher, "run", "main",
                addSecretEnvVariable(secretFilePath));
    }

    @Test
    public void testInvalidAccessEncryptedConfigs() throws BallerinaTestException {
        String configFilePath = Paths.get(testFileLocation, "ConfigFiles", "InvalidEncryptedConfig.toml").toString();
        String secretFilePath = Paths.get(testFileLocation, "Secrets", "correctSecret.txt").toString();
        LogLeecher runLeecher = new LogLeecher("error: failed to retrieve the encrypted value for variable: " +
                "'main:password' : Input byte array has wrong 4-byte ending unit", ERROR);
        executeBalCommand("/encryptedConfigProject", runLeecher, "run", "main",
                addEnvVariables(configFilePath, secretFilePath));
    }

    private void executeBalCommand(String projectPath, LogLeecher log, String command, String packageName,
                                   Map<String, String> envProperties) throws BallerinaTestException {
        bMainInstance.runMain(command, new String[]{packageName}, envProperties, new String[]{},
                new LogLeecher[]{log}, testFileLocation + projectPath);
        log.waitForText(5000);
    }

    /**
     * Get environment variables and add config file path as an env variable.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(String configFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigurableConstants.CONFIG_ENV_VARIABLE, configFilePath);
        return envVariables;
    }

    private Map<String, String> addSecretEnvVariable(String secretFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigurableConstants.CONFIG_SECRET_ENV_VARIABLE, secretFilePath);
        return envVariables;
    }

    private Map<String, String> addEnvVariables(String configFilePath, String secretFilePath) {
        Map<String, String> envVariables = PackerinaTestUtils.getEnvVariables();
        envVariables.put(ConfigurableConstants.CONFIG_ENV_VARIABLE, configFilePath);
        envVariables.put(ConfigurableConstants.CONFIG_SECRET_ENV_VARIABLE, secretFilePath);
        return envVariables;
    }
}
