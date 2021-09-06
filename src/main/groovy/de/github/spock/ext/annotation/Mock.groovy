package de.github.spock.ext.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
@interface Mock {

    String name() default ''

}
