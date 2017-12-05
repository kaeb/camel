package io.toolisticon.camel.spring.testutils.exclude;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.boot.CamelConfigurationProperties;
import org.apache.camel.test.spring.ExcludeRoutes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.rules.CamelSpringClassRule;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sets property to exclude rules for Camel Spring Test Route Collector.
 */
@Slf4j
@Component
public class PropertySettingListener implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    private final CamelConfigurationProperties configurationProperties;
    private final String listOfClassPatterns;

    public PropertySettingListener(final CamelConfigurationProperties configurationProperties) {

        this.configurationProperties = configurationProperties;
        this.listOfClassPatterns = getExcludedClassesAsString(CamelSpringClassRule.getTestContext());
        // this looks fishy, but we MUST set the properties before the RouteCollector is called,
        // since it caches the configuration properties in constructor.

        // for this purpose the order is set to value higher than Camel's Route Collector.

        // set excludes
        if (!StringUtils.isEmpty(listOfClassPatterns)) {
            this.configurationProperties.setJavaRoutesExcludePattern(listOfClassPatterns);
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        final ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext instanceof ConfigurableApplicationContext && !StringUtils.isEmpty(listOfClassPatterns)) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment((ConfigurableApplicationContext) applicationContext,
                    PropertySettingConfiguration.PROPERTY + "=" + listOfClassPatterns);
        }
    }

    @Override
    public int getOrder() {
        // just to be before camel (camel has -2)
        return LOWEST_PRECEDENCE - 5;
    }


    /**
     * Retrieves the set of classes representing route builders marked for exclusion by {@link ExcludeRoutes} annotation
     * placed on the test class.
     * @param testContext test context for retrieving the test class.
     * @return a set of classes or an empty set.
     */
    static Set<Class<?>> getExcludedClasses(final TestContext testContext) {
        final Class<?> testClass = testContext.getTestClass();
        if (testClass.isAnnotationPresent(ExcludeRoutes.class)) {
            final Class<?>[] excludedClasses = testClass.getAnnotation(ExcludeRoutes.class).value();

            if (excludedClasses != null) {
                return Arrays.stream(excludedClasses).collect(Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }

    /**
     * Retrieves the set of classes representing route builders marked for exclusion by {@link ExcludeRoutes} annotation
     * placed on the test class.
     * @param testContext test context for retrieving the test class.
     * @return a string of comma-separated Ant patterns pointing to excluded classes or an empty string.
     */
    static String getExcludedClassesAsString(final TestContext testContext) {
        if (testContext != null) {
            final Set<Class<?>> classesSet = getExcludedClasses(testContext);
            final String listOfClassPatterns = StringUtils.collectionToCommaDelimitedString(
                    classesSet
                            .stream()
                            .map(clazz -> ClassUtils.convertClassNameToResourcePath(clazz.getName()))
                            .collect(Collectors.toList()));
            if (!classesSet.isEmpty()) {
                log.info("Set exclude for {} classes to '{}'.", classesSet.size(), listOfClassPatterns);
                return listOfClassPatterns;
            }
        } else {
            log.error("Testing context is unset. Maybe you forgot to put the class rule CamelSpringClassRule into your test.");
        }
        return "";
    }

}
