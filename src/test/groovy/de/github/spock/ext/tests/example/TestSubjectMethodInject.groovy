package de.github.spock.ext.tests.example

import javax.inject.Inject

class TestSubjectMethodInject{

    private TestInterface testInterface

    @Inject
    private void setTestInterface( TestInterface testInterface ){
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
