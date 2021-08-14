package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class SpockTestSpec extends Specification{

    @Mock
    private TestInterface testInterface
    @InjectMocks
    private TestSubject testSubject

    def setup(){
    }

    def 'Mock is initialized and not null'(){
        expect: 'mock is not null'
        assert testInterface
    }

}