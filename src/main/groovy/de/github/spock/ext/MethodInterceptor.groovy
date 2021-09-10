package de.github.spock.ext

import de.github.spock.ext.annotation.InjectMocks
import de.github.spock.ext.annotation.Mock
import de.github.spock.ext.annotation.Stub
import de.github.spock.ext.exception.NoSuchBeanDefinitionException
import de.github.spock.ext.exception.NoUniqueBeanDefinitionException
import de.github.spock.ext.exception.UnresolvedBeansException
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

import javax.annotation.Resource
import javax.inject.Inject
import javax.inject.Named
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
            Autowired, Inject, Resource
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
        MockReferenceHolder referenceHolder = new MockReferenceHolder()

        MockUtil mockUtil = new MockUtil()
        DetachedMockFactory mockFactory = new DetachedMockFactory()
        List<FieldInfo> targetFields = [ ]

        Closure handleAnnotation = { FieldInfo info, String name ->
            Object mockObj = mockFactory.Mock( info.type )
            injectMock( info.reflection, target, mockObj )
            mockUtil.attachMock( mockObj, target )
            referenceHolder.addReference( mockObj, name ?: info.name, info.type )
        }

        specInfo.fields.each{ FieldInfo info ->
            if( info.isAnnotationPresent( Mock ) ){
                handleAnnotation( info, info.getAnnotation( Mock ).name() )
            } else if( info.isAnnotationPresent( Stub ) ){
                handleAnnotation( info, info.getAnnotation( Stub ).name() )
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
        if( !field.trySetAccessible() ){
            throw new IllegalAccessException( 'No access rights for field "' + field.name + '"' )
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
        resolveParameterBeans( first.parameterTypes, referenceHolder, params )
        Object testSubject = first.newInstance( params.toArray() )
        injectMock( info.reflection, target, testSubject )
        true
    }

    private static void callMockInjectionOnEachSetter( Class<?> type, MockReferenceHolder referenceHolder,
                                                       testObject ){
        type.getDeclaredMethods().toList().each{ Method declaredMethod ->
            if( areAnnotationsPresent( declaredMethod ) ){
                List resolvedParams = [ ]

                Closure handleResolvedParams = { String annotation ->
                    MockReference mockReference = referenceHolder.findByQualifier( annotation )
                    if( !mockReference ){
                        Class<?> firstParamType = declaredMethod.parameterTypes.first()
                        throw new NoSuchBeanDefinitionException( firstParamType, annotation )
                    }
                    resolvedParams << mockReference.mock
                }

                Class<?>[] parameterTypes = declaredMethod.parameterTypes
                if( declaredMethod.isAnnotationPresent( Qualifier ) ){
                    handleResolvedParams( declaredMethod.getAnnotation( Qualifier ).value() )
                } else if( declaredMethod.isAnnotationPresent( Named ) ){
                    handleResolvedParams( declaredMethod.getAnnotation( Named ).value() )
                } else if( declaredMethod.isAnnotationPresent( Resource ) &&
                        declaredMethod.getAnnotation( Resource ).name() ){
                    handleResolvedParams( declaredMethod.getAnnotation( Resource ).name() )
                } else{
                    resolveParameterBeans( parameterTypes, referenceHolder, resolvedParams )
                }
                if( resolvedParams.size() == parameterTypes.size() ){
                    declaredMethod.accessible = true
                    declaredMethod.invoke( testObject, resolvedParams.toArray() )
                } else{
                    throw new UnresolvedBeansException()
                }
            }
        }
    }

    private static void resolveParameterBeans( Class<?>[] parameterTypes, MockReferenceHolder referenceHolder,
                                               List resolvedParams ){
        Closure handleResolvedParams = { Class<?> paramType, String annotation ->
            MockReference mockReference = referenceHolder.findByQualifier( annotation )
            if( !mockReference ){
                throw new NoSuchBeanDefinitionException( paramType, annotation )
            }
            resolvedParams << mockReference.mock
        }

        parameterTypes.each{ Class<?> paramType ->
            //Qualifier strategy...
            if( paramType.isAnnotationPresent( Qualifier ) ){
                handleResolvedParams( paramType, paramType.getAnnotation( Qualifier ).value() )
            } else if( paramType.isAnnotationPresent( Named ) ){
                handleResolvedParams( paramType, paramType.getAnnotation( Named ).value() )
            } else if( paramType.isAnnotationPresent( Resource ) &&
                    paramType.getAnnotation( Resource ).name() ){
                handleResolvedParams( paramType, paramType.getAnnotation( Resource ).name() )
            } else{
                MockReference mockReference = referenceHolder.findByType( paramType )
                if( !mockReference ){
                    throw new NoSuchBeanDefinitionException( paramType, '' )
                }
                resolvedParams << mockReference.mock
            }
            int typeCount = referenceHolder.countTypes( paramType )
            if( typeCount > 1 ){
                throw new NoUniqueBeanDefinitionException( paramType, typeCount )
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

                    Closure handleQualifier = { String qualifier ->
                        MockReference mockReference = referenceHolder.findByQualifier( qualifier )
                        if( !mockReference ){
                            throw new NoSuchBeanDefinitionException( declaredField.type, qualifier )
                        }
                        injectMock( declaredField, testObject, mockReference.mock )
                    }

                    if( declaredField.isAnnotationPresent( Qualifier ) ){
                        //Qualifier strategy
                        handleQualifier( declaredField.getAnnotation( Qualifier ).value() )
                    } else if( declaredField.isAnnotationPresent( Named ) ){
                        //Named strategy
                        handleQualifier( declaredField.getAnnotation( Named ).value() )
                    } else if( declaredField.isAnnotationPresent( Resource ) &&
                            declaredField.getAnnotation( Resource ).name() ){
                        handleQualifier( declaredField.getAnnotation( Resource ).name() )
                    }
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
