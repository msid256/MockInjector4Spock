package de.github.spock.ext.tests

import org.springframework.beans.factory.annotation.Autowired

class TestSubject{

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
