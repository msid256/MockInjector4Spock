package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.TestInterface
import de.github.spock.ext.tests.example.TestSubjectConstructorAtInject
import spock.lang.Specification

class ConstructorInjectSpec extends Specification{

    @Mock
    private TestInterface testInterface
    @InjectMocks
    private TestSubjectConstructorAtInject testSubject

    def 'testSubject is not null'(){
        expect: 'true'
        testSubject
    }

    def 'testSubject has been injected'(){
        when: ''
        boolean result = testSubject.doSomething()

        then: ''
        1 * testInterface.value >> 'this is a test'
        result
    }
}