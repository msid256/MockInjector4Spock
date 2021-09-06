package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.AnotherTestInterface
import de.github.spock.ext.tests.example.TestSubjectMultipleFields
import spock.lang.Specification
import spock.lang.Unroll

class TestSubjectMultipleFieldsSpec extends Specification{

    @Mock( name = 'dasIstEines' )
    private AnotherTestInterface mockOne
    @Mock( name = 'dasZweite' )
    private AnotherTestInterface mockTwo
    @InjectMocks
    private TestSubjectMultipleFields multFields

    def setup(){
    }

    def 'Mocks and test subject are not null'(){
        expect: 'mocks are not null'
        mockOne
        mockTwo
        multFields
    }

    def 'The expected mock has been injected'(){
        given: 'any input will yield "resultat1"'
        mockOne.executeSomeOperation( _ as String ) >> 'resultat1'

        when: 'execute first operation'
        boolean result = multFields.doSomething( 'resultat1' )

        then: 'is true'
        result
    }

    def 'Unequal results yields false'(){
        given: 'any input will yield "resultat1"'
        mockOne.executeSomeOperation( _ as String ) >> 'resultat1'

        when: 'execute first operation'
        boolean result = multFields.doSomething( 'resultat2' )

        then: 'is true'
        !result
    }

    @Unroll
    def 'specific results for mockObjectOne and mockObjectTwo should yield expected subject method "#expected"'(){
        given: 'any input will yield #mockResultOne'
        mockOne.executeSomeOperation( _ as String ) >> mockResultOne
        mockTwo.executeSomeOperation( _ as String ) >> mockResultTwo

        when: 'areTheyEqual is executed'
        boolean givenResult = multFields.areTheyEqual()

        then: 'givenResult should be "#expected"'
        expected == givenResult

        where:
        mockResultOne | mockResultTwo || expected
        'a'           | 'b'           || false
        'wieder'      | 'wieder'      || true
        'Wieder'      | 'wieder'      || false
    }

}
