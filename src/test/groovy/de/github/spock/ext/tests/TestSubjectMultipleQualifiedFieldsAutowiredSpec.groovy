package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.AnotherTestInterface
import de.github.spock.ext.tests.example.TestSubjectMultipleQualifiedFieldsAutowired
import spock.lang.Specification
import spock.lang.Unroll

class TestSubjectMultipleQualifiedFieldsAutowiredSpec extends Specification{

    @Mock( name = 'theFirstOne' )
    private AnotherTestInterface testInterface1

    @Mock( name = 'theSecondOne' )
    private AnotherTestInterface testInterface2

    @InjectMocks
    private TestSubjectMultipleQualifiedFieldsAutowired testSubject

    def setup(){
    }

    def 'all fields are instantiated'(){
        expect: 'None of the fields are null'
        testInterface1
        testInterface2
        testSubject
    }

    @Unroll
    def 'Mocked objects behave differently'(){
        given: 'given result of mocks'
        testInterface1.executeSomeOperation( _ as String ) >> 'foo'
        testInterface2.executeSomeOperation( _ as String ) >> 'baz'

        when: 'execute first method'
        boolean result1 = testSubject.testOne( paramOne )
        boolean result2 = testSubject.testOne( paramTwo )

        then: 'The first result is true, the second one false'
        expectedResultOne == result1
        expectedResultTwo == result2

        where: 'Given data'
        paramOne | paramTwo || expectedResultOne | expectedResultTwo
        'foo'    | 'baz'    || true              | false
        'baz'    | 'foo'    || false             | true
    }

    def 'Only the first mock object is called'(){
        when: 'execute first method'
        testSubject.testOne( 'foo' )
        testSubject.testOne( 'baz' )

        then: 'The first result is true, the second one false'
        2 * testInterface1.executeSomeOperation( _ as String ) >> 'foo'
        0 * testInterface2.executeSomeOperation( _ as String ) >> 'baz'
    }

}
