package de.github.spock.ext.exception

class UnresolvedBeansException extends Exception{

    UnresolvedBeansException(  ){
        super( "Not all beans could be resolved." )
    }

}
