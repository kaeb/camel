package io.toolisticon.camel.spring.testutils;

import io.toolisticon.camel.spring.testutils.routes.RouteOne;
import io.toolisticon.camel.spring.testutils.routes.RouteTwo;
import org.apache.camel.CamelContext;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.CamelSpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = CamelRuleTest.CamelApplication.class)
public class CamelRuleTest {

    @ClassRule
    public static final CamelSpringClassRule springClassRule = new CamelSpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CamelContext camelContext;

    @Test
    @DirtiesContext
    public void startCamel() {
        assertThat(camelContext).isNotNull();
        assertThat(camelContext.getRoute(RouteOne.ID)).isNotNull();
        assertThat(camelContext.getRoute(RouteTwo.ID)).isNotNull();
    }


    @SpringBootApplication
    static class CamelApplication {

    }
}

