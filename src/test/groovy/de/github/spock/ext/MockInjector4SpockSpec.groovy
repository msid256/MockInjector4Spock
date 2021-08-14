package de.github.spock.ext

import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

class MockInjector4SpockSpec extends Specification{

    MockInjector4Spock mockInjector
    SpecInfo specInfo
    FieldInfo fieldInfo

    def setup(){
        specInfo = Mock()
        fieldInfo = Mock()

        mockInjector = new MockInjector4Spock()
    }

    def 'should load all fieldInfos from spec'(){
        when: 'executing visitSpec...'
        mockInjector.visitSpec( specInfo )

        then: '... access all fields of the spec'
        1 * specInfo.fields >> [ fieldInfo ]
    }

}
