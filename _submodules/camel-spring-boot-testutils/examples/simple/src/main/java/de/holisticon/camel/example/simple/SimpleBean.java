package de.holisticon.camel.example.simple;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("simpleBean")
public class SimpleBean {

    @Value("${greeting}")
    private String say;

    public String saySomething() {
        return say;
    }
}
