package de.github.spock.ext.tests.example

import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

class PostConstruction{

    @Autowired
    private AnotherTestInterface testInterface
    private String unitializedField

    @PostConstruct
    private void afterConstruction(){
        unitializedField = 'finished'
    }
}
