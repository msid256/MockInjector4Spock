package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.AnotherTestInterface
import de.github.spock.ext.tests.example.PostConstruction
import spock.lang.Specification

import java.lang.reflect.Field

class TestPostConstructSpec extends Specification{

    @InjectMocks
    private PostConstruction postConstruction

    def 'verify that the method in testInterface was called'(){
        setup: ''
        Field field = PostConstruction.getDeclaredField( 'unitializedField' )
        field.accessible = true
        String fieldVal = field.get(postConstruction) as String

        expect: 'Field in postConstruction is initialized'
        fieldVal == 'finished'
    }

}
