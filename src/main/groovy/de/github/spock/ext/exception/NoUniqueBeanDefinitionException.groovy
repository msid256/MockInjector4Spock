package de.github.spock.ext.exception

class NoUniqueBeanDefinitionException extends Exception{

    NoUniqueBeanDefinitionException( Class<?> type, int count ){
        super( "No qualifying bean of type '" + type.getName() +
                "' available. Expected single matching bean, but instead found $count instances." )
    }
}
