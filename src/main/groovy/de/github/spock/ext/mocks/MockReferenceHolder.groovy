package de.github.spock.ext.mocks

class MockReferenceHolder{

    private List<MockReference> mockReferenceList = [ ]

    void addReference( Object mock, String qualifier, Class<?> type ){
        mockReferenceList.add( new MockReference( mock, qualifier, type ) )
    }

    MockReference findByType( Class<?> type ){
        mockReferenceList.find{ it.type == type }
    }

    MockReference findByQualifier( String qualifier ){
        mockReferenceList.find{ it.qualifier == qualifier }
    }

    int countTypes( Class<?> type ){
        mockReferenceList.inject( 0 ){ int counter, MockReference it -> it.type == type ? 1 + counter : counter }
    }

}
