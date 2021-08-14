package de.github.spock.ext

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
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

    def 'should add an initializer interceptor when a field is annotated with "Mock"'(){
        given: 'the field annotated with "Mock"'
        fieldInfo.isAnnotationPresent( Mock ) >> true
        specInfo.fields >> [ fieldInfo ]

        when: 'executing visitSpec...'
        mockInjector.visitSpec( specInfo )

        then: 'add a method interceptor to the spec info'
        1 * specInfo.addInitializerInterceptor( _ as MethodInterceptor )
    }

    def 'should add an initializer interceptor when a field is annotated with "InjectMocks"'(){
        given: 'the field annotated with "Mock"'
        fieldInfo.isAnnotationPresent( InjectMocks ) >> true
        specInfo.fields >> [ fieldInfo ]

        when: 'executing visitSpec...'
        mockInjector.visitSpec( specInfo )

        then: 'add a method interceptor to the spec info'
        1 * specInfo.addInitializerInterceptor( _ as MethodInterceptor )
    }

    def 'should not add an initializer interceptor if there are no relevant Mocks present'(){
        given: 'the field annotated with "Mock"'
        fieldInfo.isAnnotationPresent( InjectMocks ) >> false
        fieldInfo.isAnnotationPresent( Mock ) >> false
        specInfo.fields >> [ fieldInfo ]

        when: 'executing visitSpec...'
        mockInjector.visitSpec( specInfo )

        then: 'add a method interceptor to the spec info'
        0 * specInfo.addInitializerInterceptor( _ )
    }

}
