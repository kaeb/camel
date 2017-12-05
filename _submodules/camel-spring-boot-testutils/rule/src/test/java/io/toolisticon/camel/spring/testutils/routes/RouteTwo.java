package io.toolisticon.camel.spring.testutils.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RouteTwo extends RouteBuilder {

    public static final String ID = "routeTwo";

    @Override
    public void configure() throws Exception {
        from("direct:test2")
                .routeId(ID)
                .to("direct:test2");
    }
}