package de.github.spock.ext.tests.example

import javax.inject.Inject

class TestSubjectFieldInject{

    @Inject
    private TestInterface testInterface

    boolean doSomething(){
        if( testInterface.getValue() ){
            true
        } else{
            false
        }
    }
}
