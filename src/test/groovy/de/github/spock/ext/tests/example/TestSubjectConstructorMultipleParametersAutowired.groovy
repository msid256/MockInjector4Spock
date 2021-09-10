package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Autowired

class TestSubjectConstructorMultipleParametersAutowired{

    AlternativeTestInterface testInterface2
    AnotherTestInterface testInterface3
    TestInterface testInterface1

    @Autowired
    TestSubjectConstructorMultipleParametersAutowired( TestInterface testInterface1,
                                                       AlternativeTestInterface testInterface2,
                                                       AnotherTestInterface testInterface3 ){
        this.testInterface1 = testInterface1
        this.testInterface2 = testInterface2
        this.testInterface3 = testInterface3
    }

    boolean doSomething(){
        testInterface1.getValue() == testInterface3.executeSomeOperation( '' )
    }

    boolean doSomethingWithAll(){
        testInterface1.getValue() == testInterface3.executeSomeOperation( '' ) ||
                testInterface2.countStuff() == 5
    }
}
