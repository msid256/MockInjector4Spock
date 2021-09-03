package de.github.spock.ext

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.annotation.Stub
import de.github.spock.ext.mocks.MockReference
import de.github.spock.ext.mocks.MockReferenceHolder
import groovy.transform.CompileStatic
import org.spockframework.mock.MockUtil
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import javax.inject.Inject
import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.logging.Logger

@CompileStatic
class MethodInterceptor extends AbstractMethodInterceptor{

    private static final Logger logger = Logger.getLogger( MethodInterceptor.class.getName() )
    private static final Class<? extends Annotation>[] ANNOTATIONS = [
            Autowired, Inject
    ].toArray( new Class<? extends Annotation>[0] )

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
        MockReferenceHolder referenceHolder = new MockReferenceHolder()

        MockUtil mockUtil = new MockUtil()
        DetachedMockFactory mockFactory = new DetachedMockFactory()
        List<FieldInfo> targetFields = [ ]

        specInfo.fields.each{ FieldInfo info ->
            if( info.isAnnotationPresent( Mock ) ){
                Object mockObj = mockFactory.Mock( info.type )
                injectMock( info.reflection, target, mockObj )
                mockUtil.attachMock( mockObj, target )
                mocks.put( info.type, mockObj )
                referenceHolder.addReference( mockObj, info.name, info.type )
            } else if( info.isAnnotationPresent( Stub ) ){
                Object stub = mockFactory.Stub( info.type )
                injectMock( info.reflection, target, stub )
                mockUtil.attachMock( stub, target )
                mocks.put( info.type, stub )
                referenceHolder.addReference( stub, info.name, info.type )
            } else if( info.isAnnotationPresent( InjectMocks ) ){
                targetFields << info
            }
        }

        //when targetFields are empty, we do not need to inject a mock object
        if( !targetFields ){
            return
        }
        targetFields.each{ FieldInfo info ->
            if( !defaultConstructorStrategy( info, target, referenceHolder ) ){
                autowiredConstructorStrategy( info, target, referenceHolder )
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
    private static boolean defaultConstructorStrategy( FieldInfo info, Object target,
                                                       MockReferenceHolder referenceHolder ){
        Class<?> type = info.type
        List<Constructor<?>> constructors = type.getDeclaredConstructors().toList()
        Constructor<?> defaultConstructor = constructors.find{ it.parameterCount == 0 }
        if( !defaultConstructor ){
            return false
        }
        Object testObject = defaultConstructor.newInstance()
        callMockInjectionOnEachField( type, referenceHolder, testObject )
        callMockInjectionOnEachSetter( type, referenceHolder, testObject )
        injectMock( info.reflection, target, testObject )
        true
    }

    private static boolean autowiredConstructorStrategy( FieldInfo info, Object target,
                                                         MockReferenceHolder referenceHolder ){
        List<Constructor<?>> constructors = info.type.getDeclaredConstructors().toList()
        List<Constructor<?>> withAnnotations = constructors.findAll{ areAnnotationsPresent( it ) }
        if( !withAnnotations ){
            return false
        }
        Constructor<?> first = withAnnotations.first()
        List params = [ ]
        for( Class<?> it : first.parameterTypes ){
            MockReference reference = referenceHolder.findByType( it )
            if( reference ){
                params.add( reference.mock )
            } else{
                return false
            }
        }
        Object testSubject = first.newInstance( params.toArray() )
        injectMock( info.reflection, target, testSubject )
        true
    }

    private static void callMockInjectionOnEachSetter( Class<?> type, MockReferenceHolder referenceHolder,
                                                       testObject ){
        type.getDeclaredMethods().toList().each{ Method declaredMethod ->
            if( areAnnotationsPresent( declaredMethod ) ){
                List params = [ ]
                if( declaredMethod.isAnnotationPresent( Qualifier ) ){
                    Qualifier annotation = declaredMethod.getAnnotation( Qualifier )
                    MockReference mock = referenceHolder.findByQualifier( annotation.value() )
                    if( !mock){
                        //TODO: throw new exception, as no bean could be found
                    }
                    params << mock.mock
                } else{
                    declaredMethod.parameterTypes.each{ Class<?> paramType ->
                        //check for annotated params...
                        //Qualifier strategy...
                        if( paramType.isAnnotationPresent( Qualifier ) ){
                            String qualifier = paramType.getAnnotation( Qualifier ).value()

                            MockReference mockReference = referenceHolder.findByQualifier( qualifier )
                            if( !mockReference){
                                //TODO: throw new Exception, as there is no bean
                            }
                            params << mockReference.mock
                        }
                        int typeCount = referenceHolder.countTypes( paramType )
                        if( typeCount > 1 ){
                            //TODO: also throw an exception, as there is a unique bean (UniqueBeanDefinitionException)
                        }
                        params << referenceHolder.findByType( paramType ).mock
                    }
                }
                if( params.size() == declaredMethod.parameterTypes.size() ){
                    declaredMethod.accessible = true
                    declaredMethod.invoke( testObject, params.toArray() )
                } else{
                    //TODO: throw an exception, as setting is not possible
                }
            }
        }

    }

    private static List<Field> callMockInjectionOnEachField( Class<?> type, MockReferenceHolder referenceHolder,
                                                             testObject ){
        type.getDeclaredFields().toList().each{ Field declaredField ->
            if( areAnnotationsPresent( declaredField ) ){
                int typeCount = referenceHolder.countTypes( declaredField.type )
                if( typeCount == 1 ){
                    MockReference reference = referenceHolder.findByType( declaredField.type )
                    injectMock( declaredField, testObject, reference.mock )
                } else if( typeCount > 1 ){
                    //Qualifier-Strategy
                    if( declaredField.isAnnotationPresent( Qualifier ) ){
                        Qualifier annotation = declaredField.getAnnotation( Qualifier )
                        String qualifier = annotation.value()
                        MockReference mockReference = referenceHolder.findByQualifier( qualifier )
                        if( mockReference ){
                            injectMock( declaredField, testObject, mockReference.mock )
                        }

                    }
                    //Named-Strategy
                }
            }
        }
    }

    private static boolean areAnnotationsPresent( AnnotatedElement annotatedElement ){
        boolean result = false
        for( Class<? extends Annotation> annotation : ANNOTATIONS ){
            if( annotatedElement.isAnnotationPresent( annotation ) ){
                result = true
                break
            }
        }
        result
    }

}
