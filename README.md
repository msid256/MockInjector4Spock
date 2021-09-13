# MockInjector4Spock
Easily annotate Mocks and inject them into the class instance under test.

## Annotations
This extension provides three annotations:

* **@Mock** Mark a field as a mock. This will create a mock object using the type of the annotated field.
* **@Stub** Mark a field as a stub. This will create a stub object using the type of the annotated field.
* **@InjectMocks** Mark the class under test. This will create an instance of the type and try to locate all fields within
this object that can be injected with a mock identified in this specification.

## Writing a Spock Test
### Dependency Inversion Pattern
Code written for a containerised environment like spring or wildfly makes usually use of the dependency inversion pattern
where the dependencies are injected via the container in to the bean. The dependencies within the bean are marked by 
annotations like @Autowired, @Resource or @Inject.

Tools like Mockito help unit test authors write tests without having to manually inject the mocked dependencies into the
test subject, i.e. the bean you want to test. You just need to annotate the mock objects with @Mock to let mockito 
generate the mocks. You then just annotate the test subject with @Inject and Mockito will automatically inject all 
needed dependencies.

I currently miss this simple way in spock tests (that's actually why I wrote this extension). The only way I know of 
for autowiring dependencies would be to use a test runner that makes use of a bean container. But I would just like to
unit test a component.

### Classes to test
The following injection methods are currently supported

####1. Annotated fields, constructors, and setter methods:
````groovy
class TestSubject {

/**
 * Field
 */
    @Autowired
    private IExample field

/**
 * Constructor
 */
    @Autowired
    TestSubject(IExample field){
        this.field = field
    }

/**
 * Setter
 */
    @Autowired
    private void setField(IExample field){
        this.field=field
    }
}
````
The specification for the above class would look like this:
````groovy
class TestSubjectSpec extends Specification {
    
    @Mock
    private IExample field
    @InjectMocks
    private TestSubject testSubject
    
    ///... tests follow here
}
````

####2. The @Qualifier annotation that supports named dependency resolution.