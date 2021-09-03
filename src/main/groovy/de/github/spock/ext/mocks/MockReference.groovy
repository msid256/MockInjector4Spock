package de.github.spock.ext.mocks

class MockReference{

    final Object mock
    final String qualifier
    final Class<?> type

    MockReference( Object mock, String qualifier, Class<?> type ){
        this.qualifier = qualifier
        this.mock = mock
        this.type = type
    }

    @Override
    boolean equals( Object other ){
        if( !( other instanceof MockReference ) ){
            return false
        }
        if( is( other ) ){
            return true
        }
        return other.mock.is( mock ) && other.qualifier == qualifier && other.type.is( type )
    }

    @Override
    int hashCode(){
        int code = 31
        code *= mock ? mock.hashCode() : 17
        code *= qualifier ? qualifier.hashCode() : 19
        code *= type ? type.hashCode() : 13
        code
    }
}
