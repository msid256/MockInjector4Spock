package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Autowired

class TestSubjectConstructorAutowired{

    private TestInterface testInterface

    @Autowired
    TestSubjectConstructorAutowired( TestInterface testInterface ){
        this.testInterface = testInterface
    }

    boolean doSomething(){
        if( testInterface.getValue() ){
            true
        } else{
            false
        }
    }
}
