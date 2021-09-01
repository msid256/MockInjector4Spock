package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Autowired

class TestSubjectWithConstructorInjection{

    private TestInterface testInterface

    @Autowired
    TestSubjectWithConstructorInjection( TestInterface testInterface ){
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
