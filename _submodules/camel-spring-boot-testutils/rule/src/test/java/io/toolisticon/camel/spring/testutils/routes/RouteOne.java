package io.toolisticon.camel.spring.testutils.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RouteOne extends RouteBuilder {

    public static final String ID = "routeOne";

    @Override
    public void configure() throws Exception {
        from("direct:test1")
                .routeId(ID)
                .to("direct:test1");
    }
}