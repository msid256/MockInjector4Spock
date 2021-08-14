package de.github.spock.ext

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.annotation.Stub
import groovy.transform.CompileStatic
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import org.springframework.beans.factory.annotation.Autowired
import spock.mock.DetachedMockFactory

import java.lang.reflect.Constructor
import java.lang.reflect.Field

@CompileStatic
class MethodInterceptor extends AbstractMethodInterceptor{

    private final SpecInfo specInfo

    MethodInterceptor( SpecInfo specInfo ){
        this.specInfo = specInfo
    }

    @Override
    void interceptSharedInitializerMethod( IMethodInvocation invocation ){
        invocation.proceed()
    }

    @Override
    void interceptInitializerMethod( IMethodInvocation invocation ){
        invocation.proceed()
    }

    private void injectMocks( Object target ){
        Map<Class, Object> mocks = new HashMap<>()
        DetachedMockFactory mockFactory = DetachedMockFactory.newInstance()
        List<FieldInfo> targetFields = [ ]

        specInfo.fields.each{ FieldInfo info ->
            if( info.isAnnotationPresent( Mock ) ){
                Object mockObj = mockFactory.Mock( info.type )
                injectMock( info.reflection, target, mockObj )
                mocks.put( info.type, mockObj )
            } else if( info.isAnnotationPresent( Stub ) ){
                Object stub = mockFactory.Stub( info.type )
                injectMock( info.reflection, target, stub )
                mocks.put( info.type, stub )
            } else if( info.isAnnotationPresent( InjectMocks ) ){
                targetFields << info
            }
        }

        //when targetFields are empty, we do not need to inject a mock object
        if( !targetFields ){
            return
        }
        targetFields.each{ FieldInfo info ->
            Class<?> type = info.type
            List<Constructor<?>> constructors = type.getDeclaredConstructors().toList()

            if( !defaultConstructorStrategy( info.type, mocks ) ){
                List withAnnotations = constructors.findAll{ it.isAnnotationPresent( Autowired ) }
            }
        }
    }

    private static void injectMock( Field field, target, mockObj ){
        field.accessible = true
        field.set( target, mockObj )
    }

    /**
     * Find the default constructor and try to initialize the  test object with it. Afterwards, inject
     * all the mocks into it.
     *
     * @param type The type of the class under test
     * @param mocks The map containing the mocks to be injected
     * @return true if this strategy is successful, otherwise false
     */
    private static boolean defaultConstructorStrategy( Class<?> type, Map<Class<?>, Object> mocks ){
        List<Constructor<?>> constructors = type.getDeclaredConstructors().toList()
        Constructor<?> defaultConstructor = constructors.find{ it.parameterCount == 0 }
        if( !defaultConstructor ){
            return false
        }
        Object testObject = defaultConstructor.newInstance()
        callMockInjectionOnEachField( type, mocks, testObject )
        true
    }

    private static List<Field> callMockInjectionOnEachField( Class<?> type, Map<Class<?>, Object> mocks, testObject ){
        type.getDeclaredFields().toList().each{ Field declaredField ->
            if( declaredField.isAnnotationPresent( Autowired ) ){
                if( mocks.containsKey( declaredField.type ) ){
                    injectMock( declaredField, testObject, mocks[declaredField.type] )
                }
            }
        }
    }

}
