package de.github.spock.ext.tests.example

import javax.annotation.Resource

class TestSubjectMultipleFieldsResource{

    @Resource( name = "dasIstEines" )
    private AnotherTestInterface anotherOne
    @Resource( name = "dasZweite" )
    private AnotherTestInterface theSecondOne

    boolean doSomething( String isThisEqual ){
        anotherOne.executeSomeOperation( 'test' ) == isThisEqual
    }

    boolean areTheyEqual(){
        theSecondOne.executeSomeOperation( 'foo' ) == anotherOne.executeSomeOperation( 'test' )
    }

}
