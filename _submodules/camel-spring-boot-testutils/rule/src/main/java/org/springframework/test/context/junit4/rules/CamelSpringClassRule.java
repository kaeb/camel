package org.springframework.test.context.junit4.rules;

import org.apache.camel.test.spring.*;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

import java.util.List;

/**
 * Encapsulation of spring class rule, activating Camel using test execution listeners.
 */
public class CamelSpringClassRule extends SpringClassRule {

    @Override
    public Statement apply(final Statement base, final Description description) {

        // set the test class instead the usage of CamelSpringTestContextLoaderTestExecutionListener
        final Class<?> testClass = description.getTestClass();
        CamelSpringTestHelper.setTestClass(testClass);

        final TestContextManager testContextManager = SpringClassRule.getTestContextManager(testClass);
        final List<TestExecutionListener> list = testContextManager.getTestExecutionListeners();

        // is camel already registered?
        if (!alreadyRegistered(list)) {

            // inject Camel first, and then disable jmx and add the stop-watch
            // this is done in reverse order because inserting on zero position
            // will shift the list elements backwards.
            list.add(0, new StopWatchTestExecutionListener()); // 4
            list.add(0, new CamelSpringBootExecutionListener()); // 2
            list.add(0, new DisableJmxTestExecutionListener()); // 1
            // list.add(0, new CamelSpringTestContextLoaderTestExecutionListener()); // 0
        }
        return super.apply(base, description);
    }

    /**
     * Checks whether Camel is registered already using a CamelSpringTestContextLoaderTestExecutionListener.
     * @param list list of test execution listeners
     * @return true, if registered.
     */
    static boolean alreadyRegistered(final List<TestExecutionListener> list) {
        if (list != null) {
            for (TestExecutionListener listener : list) {
                if (listener instanceof CamelSpringTestContextLoaderTestExecutionListener) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves test context or null, if no test context is set.
     * @return test context.
     */
    public static TestContext getTestContext() {
        final Class<?> testClass = CamelSpringTestHelper.getTestClass();
        if (testClass != null) {
            return SpringClassRule.getTestContextManager(testClass).getTestContext();
        }
        return null;
    }

}
