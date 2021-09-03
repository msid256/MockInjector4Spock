package de.github.spock.ext.tests.example

import javax.inject.Inject

class TestSubjectConstructorAtInject{

    private TestInterface testInterface

    @Inject
    TestSubjectConstructorAtInject( TestInterface testInterface ){
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
