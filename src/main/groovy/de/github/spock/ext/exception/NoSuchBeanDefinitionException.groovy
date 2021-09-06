package de.github.spock.ext.exception

class NoSuchBeanDefinitionException extends Exception{

    NoSuchBeanDefinitionException( Class<?> paramType, String qualifier ){
        super( "Could not find a bean of type '${ paramType.getCanonicalName() }'" +
                " with qualifier '$qualifier'." )
    }
}
