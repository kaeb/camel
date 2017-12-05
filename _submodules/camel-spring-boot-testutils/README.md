# Camel Springboot Test Utils
[![Build Status](https://travis-ci.org/toolisticon/camel-spring-boot-testutils.svg?branch=master)](https://travis-ci.org/toolisticon/camel-spring-boot-testutils.svg?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.toolisticon.camel/camel-spring-boot-test-rule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.toolisticon.camel/camel-spring-boot-test-rule)

This projects supplies a small library making testing of Apache Camel components easier. 

## Installation and requirements

Minimal tested version:

- Apache Camel 2.20.1
- Springboot 1.5.8-RELEASE

Put the following dependency to your Maven POM file:

    <dependency>
      <groupId>io.toolisticon.camel</groupId>
      <artifactId>camel-spring-boot-test-rule</artifactId>
      <version>0.0.2</version>
      <scope>test</scope>
    </dependency>

## Features

### Test rule

There is a lot written about Camel Spring Testing, but all approaches are based on inheritance (`SpringTestSupport`)
or using a special runner (`CamelSpringBootRunner`). There are good reasons for doing so, but if you are not able to do so,
for example using a different runner , you may use the supplied JUnit test rule for this:

    ...
    import org.springframework.test.context.junit4.rules.CamelSpringClassRule;
    import org.springframework.test.context.junit4.rules.SpringMethodRule;
  
  
    @SpringBootTest
    public class SimpleRuleTest {
    
        @ClassRule
        public static final CamelSpringClassRule springClassRule = new CamelSpringClassRule();
    
        @Rule
        public final SpringMethodRule springMethodRule = new SpringMethodRule();
  
        @Autowired
        private CamelContext camelContext;
    
        @Test
        @DirtiesContext
        public void testMe() throws Exception {
          // do something with camelContext here
        }
    } 
      
### ExcludeRoutes

There is a standard annotation in Camel-Spring to exclude routes `org.apache.camel.test.spring.ExcludeRoutes`. This is not 
working using a standard Springboot test. If you use this library, the support is restored. Just annotate
the `org.apache.camel.test.spring.ExcludeRoutes` to your test class containing the `CamelSpringClassRule`
and list the excluded route classes.
 
