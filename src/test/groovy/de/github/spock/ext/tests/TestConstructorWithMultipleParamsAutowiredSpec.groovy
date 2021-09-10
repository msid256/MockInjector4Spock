package de.github.spock.ext.tests

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.tests.example.AlternativeTestInterface
import de.github.spock.ext.tests.example.AnotherTestInterface
import de.github.spock.ext.tests.example.TestInterface
import de.github.spock.ext.tests.example.TestSubjectConstructorMultipleParametersAutowired
import spock.lang.Specification
import spock.lang.Unroll

class TestConstructorWithMultipleParamsAutowiredSpec extends Specification{

    @Mock
    private TestInterface ti1
    @Mock
    private AlternativeTestInterface ti2
    @Mock
    private AnotherTestInterface ti3

    @InjectMocks
    private TestSubjectConstructorMultipleParametersAutowired testSubject

    def 'All fields in this spec have been initialized'(){
        expect: 'none to be null'
        ti1
        ti2
        ti3
        testSubject
    }

    @Unroll
    def 'beans can be mocked as expected'(){
        given: 'bean results'
        ti1.getValue() >> ti1Result
        ti2.countStuff() >> ti2Result
        ti3.executeSomeOperation( _ as String ) >> ti3Result

        when: 'execute'
        boolean doSomethingResult = testSubject.doSomething()
        boolean doSomethingWithAllResult = testSubject.doSomethingWithAll()

        then: 'validate the results'
        doSomethingExpected == doSomethingResult
        doSomethingWithAllExpected == doSomethingWithAllResult

        where: 'given data'
        ti2Result | ti1Result | ti3Result || doSomethingExpected | doSomethingWithAllExpected
        5         | 'foo'     | 'yeah'    || false               | true
        7         | 'no'      | 'yes'     || false               | false
        10        | 'abc'     | 'abc'     || true                | true
    }

}