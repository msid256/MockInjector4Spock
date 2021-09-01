package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.TestInterface
import de.github.spock.ext.tests.example.TestSubjectWithConstructorInjection
import spock.lang.Specification

class ConstructorInjectionTestSpec extends Specification{

    @Mock
    private TestInterface testInterface
    @InjectMocks
    private TestSubjectWithConstructorInjection testSubject

    def 'testSubject is not null'(){
        expect: 'true'
        testSubject
    }

    def 'When testInterface.getValue() returns an empty String, testSubject.doSomething() yields false'(){
        given: 'mock response'
        testInterface.value >> ''

        when: 'execute doSomething()'
        boolean result = testSubject.doSomething()

        then: 'result is false'
        !result
    }

    def 'When testInterface.getValue() returns a String, testSubject.doSomething() yields true'(){
        when: 'execute doSomething()'
        boolean result = testSubject.doSomething()

        then: 'result is false'
        1 * testInterface.value >> 'this is a string'
        result
    }

}