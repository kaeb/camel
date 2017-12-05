package de.holisticon.camel.example.simple;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.test.spring.*;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.CamelSpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

@SpringBootTest(classes = {SimpleCamelApplication.class}, properties = { "greeting = Hello foo" })
@ExcludeRoutes(AnotherCamelRouter.class)
@Slf4j
public class SimpleRuleTest {

    @ClassRule
    public static final CamelSpringClassRule springClassRule = new CamelSpringClassRule();

    @Value("${camel.springboot.javaRoutesExcludePattern}")
    private String excluded;

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CamelContext camelContext;

    @Before
    public void foo() {
        log.info("Excluded routes are: {}", excluded);
    }

    @Test
    @DirtiesContext
    public void shouldSayFoo() throws Exception {
        // we expect that one or more messages is automatic done by the Camel
        // route as it uses a timer to trigger
        NotifyBuilder notify = new NotifyBuilder(camelContext).whenDone(1).create();
        assertTrue(notify.matches(10, TimeUnit.SECONDS));
    }
}
