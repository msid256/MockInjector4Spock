package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Qualifier

import javax.inject.Inject

class TestSubjectFieldInject{

    @Inject
    @Qualifier('richard')
    private TestInterface testInterface

    boolean doSomething(){
        if( testInterface.getValue() ){
            true
        } else{
            false
        }
    }
}
