# MockInjector4Spock
Easily annotate Mocks and inject them into the class instance under test.

## Annotations
Right now, this extension provides three annotations:

* **@Mock** Mark a field as a mock. This will create a mock object using the type of the annotated field.
* **@Stub** Mark a field as a stub. This will create a stub object using the type of the annotated field.
* **@InjectMocks** Mark the class under test. This will create an instance of the type and try to locate all fields within
this object that can be injected with a mock identified in this specification.


