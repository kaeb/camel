package de.holisticon.camel.example.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.DisableJmx;
import org.apache.camel.test.spring.ExcludeRoutes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {SimpleCamelApplication.class}, properties = {"greeting = Hello foo"})
@ExcludeRoutes(AnotherCamelRouter.class)
@Slf4j
public class SimpleRunnerTest {

    @Autowired
    private CamelContext camelContext;

    @Test
    @DirtiesContext
    public void shouldSayFoo() throws Exception {
        // we expect that one or more messages is automatic done by the Camel
        // route as it uses a timer to trigger
        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();
        assertTrue(notify.matches(10, TimeUnit.SECONDS));
    }

}
