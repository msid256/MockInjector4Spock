package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Autowired

class TestSubjectFieldAutowired{

    @Autowired
    private TestInterface testInterface

    boolean doSomething(){
        if( testInterface.getValue() ){
            true
        } else{
            false
        }
    }

}
