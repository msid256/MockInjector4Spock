# About this Spock Extension: MockInjector4Spock

Easily annotate Mocks and inject them into the class instance under test.

## Where this extension can be used

### Dependency Inversion Pattern

Code written for a containerised environment like spring or wildfly makes usually use of the dependency inversion
pattern where the dependencies are injected via the container in to the bean. The dependencies within the bean are
marked by annotations like @Autowired, @Resource or @Inject.

Tools like Mockito help unit test authors write tests without having to manually inject the mocked dependencies into the
test subject, i.e. the bean you want to test. You just need to annotate the mock objects with @Mock to let mockito
generate the mocks. You then just annotate the test subject with @Inject and Mockito will automatically inject all
needed dependencies.

I currently miss this simple way in spock tests (that's actually why I wrote this extension). The only way I know of for
autowiring dependencies would be to use a test runner that makes use of a bean container, which means extra code is 
needed.

### Classes to test

The following injection methods (annotations) are currently supported:
- @Autowired (optionally with @Qualified)
- @Resource (optionally with name parameter)
- @Inject (optionally with @Named annotation)

Field and method annotation as well as constructor injection is supported.

#### 1. Annotated fields, constructors, and setter methods:

````groovy
class TestSubject{

/**
 * Field
 */
    @Autowired
    private IExample field

/**
 * Constructor
 */
    @Autowired
    TestSubject( IExample field ){
        this.field = field
    }

/**
 * Setter
 */
    @Autowired
    private void setField( IExample field ){
        this.field = field
    }
}
````

The specification for the above class would look like this:

````groovy
class TestSubjectSpec extends Specification{
    //specifying the mock object
    @Mock
    private IExample field
    //specifying the class under test into which the mocks are injected
    @InjectMocks
    private TestSubject testSubject

    ///... tests follow here
}
````

#### 2. The @Qualifier annotation that supports named dependency resolution.

```groovy
...
@Autowired
@Qualifier( 'resourceA' )
private IComponent resourceFirst

@Autowired
@Qualifier( 'resourceB' )
private IComponent resourceAnother
...
```

And the specification would look like this:

```groovy
@Mock( 'resourceA' )
private IComponent myMockFirst
@Mock( 'resourceB' )
private IComponent myMockSecond
```

MockInjector4Spock also supports qualified parameters:

```groovy
/**
 * Constructor injection 
 */
@Autowired
TestSubject( @Qualifier( 'resourceA' ) IComponent resourceFirst, @Qualifier( 'resourceB' ) IComponent resourceAnother ){
    ...
}
```

The specification for this code is identical with the previous example.

####3. The @Resource annotation with a name value
The Resource annotation can also have a qualification string:

````groovy
...
@Resource( name = "dasIstEines" )
private AnotherTestInterface anotherOne

@Resource( name = "dasZweite" )
private AnotherTestInterface theSecondOne
...
````

The specification would be implemented accordingly as:
```groovy
@Mock( 'dasIstEines' )
private AnotherTestInterface myMockFirst
@Mock( 'dasZweite' )
private AnotherTestInterface myMockSecond
```
####4. @Inject and @Named
The annotations @Inject and @Named can be dealt with in a similar fashion as @Autowired and @Qualifier, and hence I have
skipped any example code.

## Supported Spock test doubles
MockInjector4Spock currently supports creating the following types of test doubles:
- Mocks using @Mock
- Stubs using @Stub

Spies are currently not supported.

Any field in the specification which is annotated as a mock or a stub is instantiated and injected into the 
specification instance and attached to the specification. This means that it can used just like any other Stub or Mock
specified within the specification.

### Injection of Mocks/Stubs into the class under test
The bean you want to test doesn't need to be instantiated manually. All you need to do is to declare it as a field and
annotate it with @InjectMocks. MockInjector4Spock will then do the autowiring and instantiation for you.

## Include MockInjector4Spock into your project

### gradle
In your build.gradle, just add the following line to your dependencies:
````groovy
testImplementation 'io.github.msid256:mockinjector-spock:1.0.1'
````

### maven
In your project's pom, just add the following block to your dependencies:
```xml
<depenency>
    <groupId>io.github.msid256</groupId>
    <artifactId>mockinjector-spock</artifactId>
    <version>1.0.1</version>
    <scope>test</scope>
</depenency>
```
