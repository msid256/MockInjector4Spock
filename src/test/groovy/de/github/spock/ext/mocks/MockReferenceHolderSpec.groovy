package de.github.spock.ext.mocks

import spock.lang.Specification

class MockReferenceHolderSpec extends Specification{

    MockReferenceHolder holder

    def setup(){
        holder = new MockReferenceHolder()
    }

    def 'counter should return 2 when two references of the same type are in the list'(){
        given: 'the two references'
        holder.addReference( 'I am Test1', 'test1', String )
        holder.addReference( 'I am the 2nd String', 'secondString', String )

        when: 'count types'
        int count = holder.countTypes( String )

        then: 'There are exactly 2 items'
        2 == count
    }

    def 'counter shoud return 5 when there are five references of the smae type in the list'(){
        given: 'the five references'
        initializeHolder()

        when: 'count types'
        int count = holder.countTypes( String )

        then: 'There are exactly 5 items'
        5 == count
    }

    def 'counter is 0 when there are no references of the specified type in the list'(){
        given: 'the five references'
        initializeHolder()

        when: 'count types'
        int count = holder.countTypes( Specification )

        then: 'No items found'
        0 == count
    }

    def 'should yield the first item of the matching type'(){
        given: 'the five references'
        initializeHolder()

        when: 'get reference'
        MockReference mockReference = holder.findByType( String )

        then: 'The qualifier of the mockReference is as expected'
        'fieldNo1' == mockReference.qualifier
    }

    def 'should yield the first item of the matching qualifier'(){
        given: 'the five references'
        initializeHolder()

        when: 'get reference'
        MockReference mockReference = holder.findByQualifier( 'fieldNo5' )

        then: 'The qualifier of the mockReference is as expected'
        'I am number 5' == mockReference.mock
    }

    private void initializeHolder(){
        ( 1..5 ).each{
            holder.addReference( "I am number $it", "fieldNo$it", String )
        }
    }

}
