package de.github.spock.ext

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.annotation.Stub
import groovy.transform.CompileStatic
import org.spockframework.mock.MockUtil
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.util.logging.Logger

@CompileStatic
class MethodInterceptor extends AbstractMethodInterceptor{

    private static final Logger logger = Logger.getLogger( MethodInterceptor.class.getName() )

    @Override
    void interceptInitializerMethod( IMethodInvocation invocation ){
        logger.finest 'interceptInitializerMethod called'
        injectMocks( invocation.spec, invocation.instance as Specification )
        invocation.proceed()
    }

    @Override
    void interceptSetupSpecMethod( IMethodInvocation invocation ){
        logger.finest 'interceptSetupSpecMethod called'
        invocation.proceed()
    }

    @Override
    void interceptSpecExecution( IMethodInvocation invocation ){
        logger.finest 'interceptSpecExecution called'
        invocation.proceed()
    }

    @Override
    void interceptFeatureExecution( IMethodInvocation invocation ){
        logger.finest 'interceptFeatureExecution called'
        invocation.proceed()
    }

    @Override
    void interceptIterationExecution( IMethodInvocation invocation ){
        logger.finest 'interceptIterationExecution called'
        injectMocks( invocation.spec, invocation.instance as Specification )
        invocation.proceed()
    }

    @Override
    void interceptCleanupMethod( IMethodInvocation invocation ){
        logger.finest 'interceptCleanupMethod called'
        cleanupFields( invocation.spec, invocation.instance )
        invocation.proceed()
    }

    private static void cleanupFields( SpecInfo specInfo, Object target ){
        specInfo.fields.each{
            if( it.isAnnotationPresent( Mock ) ||
                    it.isAnnotationPresent( Stub ) ||
                    it.isAnnotationPresent( InjectMocks ) ){
                it.reflection.set( target, null )
            }
        }
    }

    private static <E extends Specification> void injectMocks( SpecInfo specInfo, E target ){
        Map<Class, Object> mocks = new HashMap<>()

        MockUtil mockUtil = new MockUtil()
        DetachedMockFactory mockFactory = new DetachedMockFactory()
        List<FieldInfo> targetFields = [ ]

        specInfo.fields.each{ FieldInfo info ->
            if( info.isAnnotationPresent( Mock ) ){
                Object mockObj = mockFactory.Mock( info.type )
                injectMock( info.reflection, target, mockObj )
                mockUtil.attachMock( mockObj, target )
                mocks.put( info.type, mockObj )
            } else if( info.isAnnotationPresent( Stub ) ){
                Object stub = mockFactory.Stub( info.type )
                injectMock( info.reflection, target, stub )
                mockUtil.attachMock( stub, target )
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
            if( !defaultConstructorStrategy( info, target, mocks ) ){
                autowiredConstructorStrategy( info, target, mocks )
            }
        }
    }

    private static void injectMock( Field field, target, Object mockObj ){
        if( !field.accessible ){
            field.accessible = true
        }
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
    private static boolean defaultConstructorStrategy( FieldInfo info, Object target, Map<Class<?>, Object> mocks ){
        Class<?> type = info.type
        List<Constructor<?>> constructors = type.getDeclaredConstructors().toList()
        Constructor<?> defaultConstructor = constructors.find{ it.parameterCount == 0 }
        if( !defaultConstructor ){
            return false
        }
        Object testObject = defaultConstructor.newInstance()
        callMockInjectionOnEachField( type, mocks, testObject )
        injectMock( info.reflection, target, testObject )
        true
    }

    private static boolean autowiredConstructorStrategy( FieldInfo info, Object target, Map<Class<?>, Object> mocks ){
        List<Constructor<?>> constructors = info.type.getDeclaredConstructors().toList()
        List<Constructor<?>> withAnnotations = constructors.findAll{ it.isAnnotationPresent( Autowired ) }
        if( !withAnnotations ){
            return false
        }
        Constructor<?> first = withAnnotations.first()
        List params = [ ]
        for( Class<?> it : first.parameterTypes ){
            if( mocks.containsKey( it ) ){
                params.add( mocks.get( it ) )
            } else{
                return false
            }
        }
        Object testSubject = first.newInstance( params.toArray() )
        injectMock( info.reflection, target, testSubject )
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
