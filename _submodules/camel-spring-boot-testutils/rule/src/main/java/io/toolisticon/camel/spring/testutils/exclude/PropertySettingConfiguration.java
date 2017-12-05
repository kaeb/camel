package io.toolisticon.camel.spring.testutils.exclude;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configurations which activates the exclusion setting listener in case the property for excludes is not set.
 */
@Configuration
@ComponentScan
@ConditionalOnProperty(prefix=PropertySettingConfiguration.PROPERTY_PREFIX, name=PropertySettingConfiguration.PROPERTY_NAME, matchIfMissing = true)
public class PropertySettingConfiguration {

    public static final String PROPERTY_PREFIX = "camel.springboot";
    public static final String PROPERTY_NAME = "javaRoutesExcludePattern";

    public static final String PROPERTY = PROPERTY_PREFIX + "." + PROPERTY_NAME;

}
