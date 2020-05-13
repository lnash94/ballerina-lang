import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = string.convert(s[0]);
    counter += 1;
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    test:assertEquals(outputs[0], "{name:\"\", age:0, parent:()}");
    test:assertEquals(outputs[1], "{name:\"\", age:0, parent:()}");
    test:assertEquals(outputs[2], "{name:\"\", age:0, parent:()}");
}
