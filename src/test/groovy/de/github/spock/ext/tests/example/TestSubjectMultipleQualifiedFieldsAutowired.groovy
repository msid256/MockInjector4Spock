package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class TestSubjectMultipleQualifiedFieldsAutowired{

    @Autowired
    @Qualifier( 'theFirstOne' )
    private AnotherTestInterface one

    @Autowired
    @Qualifier( 'theSecondOne' )
    private AnotherTestInterface two

    boolean testOne(String val){
        one.executeSomeOperation(  'foo') == val
    }

    boolean testTwo(String val){
        two.executeSomeOperation('foo') == val
    }

}
