package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.TestInterface
import de.github.spock.ext.tests.example.TestSubjectFieldInject
import spock.lang.Specification

class FieldInjectSpec extends Specification{

    @Mock
    private TestInterface testInterface
    @InjectMocks
    private TestSubjectFieldInject testSubject

    def 'TestSubject is initialized and hence not null'(){
        expect: 'Initialized testSubject'
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