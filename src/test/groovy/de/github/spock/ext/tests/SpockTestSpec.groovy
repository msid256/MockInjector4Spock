package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import spock.lang.Specification

class SpockTestSpec extends Specification{

    @Mock
    private TestInterface testInterface
    @InjectMocks
    private TestSubject testSubject

    def setup(){
        println "Nothing to do"
    }

    def 'Mock is initialized and not null'(){
        expect: 'mock is not null'
        assert testInterface
    }

}