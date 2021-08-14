package de.github.spock.ext

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

class MockInjector4Spock implements IGlobalExtension{

    void start( SpecInfo spec ){
        boolean containsAnnotations = false
        for( FieldInfo info : spec.fields ){
            if( info.isAnnotationPresent( Mock ) ){
                containsAnnotations = true
                break
            } else if( info.isAnnotationPresent( InjectMocks ) ){
                containsAnnotations = true
                break
            }
        }
        if( containsAnnotations ){
            spec.addInitializerInterceptor( new MethodInterceptor() )
        }
    }

}
