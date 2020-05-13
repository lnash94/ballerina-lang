import ballerina/test;
import ballerina/io;

(any|error)[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any|error... s) {

    foreach any|error a in s {
        if (a is error) {
            outputs[counter] = a.detail().message;
        } else {
            outputs[counter] = a;
        }
        counter += 1;
    }
}

@test:Config
function testFunc() {
    // call the main function
    main();
    test:assertEquals(outputs[0], 12);
    test:assertEquals(outputs[1], "incompatible convert operation: 'string' value 'invalid' cannot be converted as 'int'");
    test:assertEquals(outputs[2], 120);
    test:assertEquals(outputs[3], "incompatible convert operation: 'string' value 'Invalid' cannot be converted as 'int'");
}
