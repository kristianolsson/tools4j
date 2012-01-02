/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.tools4j.support.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Demonstration of JUnitRunner capabilities.
 */
@RunWith(JUnitRunner.class)
public class JUnitRunnerDemoTest {
    @Test
    @ExampleAnnotation
    public void demoRuleMethodInterception() {
        System.out.println("demonstrateRuleMethodInterception()");
    }

    @Test
    public void demoFixtureArguments(FixtureExample one, FixtureExample two, FixtureExample three) {
        System.out.println("demonstrateFixtureArguments(" + one + ", " + two + ", " + three + ")");
    }

    @Test
    @ExampleAnnotation
    public void demoFixtureAndRuleCombination(FixtureExample f) {
        System.out.println("demoFixtureAndRuleCombination(" + f + ")");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @RuleHandler(RuleExample.class)
    public @interface ExampleAnnotation {
        // notice the @RuleHandler annotation that indicate the MethodRule.
    }

    public class RuleExample implements MethodRule {

        /**
         * An example implementation of a Rule. 
         *  
         * @see org.junit.rules.MethodRule#apply(org.junit.runners.model.Statement, org.junit.runners.model.FrameworkMethod, java.lang.Object)
         */
        @Override
        public Statement apply(Statement statement, final FrameworkMethod frameworkMethod,
                final Object o) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {

                    ExampleAnnotation params = frameworkMethod
                            .getAnnotation(ExampleAnnotation.class);
                    if (params == null) {
                        frameworkMethod.invokeExplosively(o);
                    } else {
                        System.out.println("Example rule got invoked for method "
                                + frameworkMethod.getName());
                        try {
                            frameworkMethod.invokeExplosively(o);
                        } catch (Throwable throwable) {
                            if (throwable instanceof RuntimeException)
                                throw (RuntimeException) throwable;
                            if (throwable instanceof Error)
                                throw (Error) throwable;
                            RuntimeException r = new RuntimeException(throwable.getMessage(),
                                    throwable);
                            r.setStackTrace(throwable.getStackTrace());
                            throw r;
                        }
                    }
                }
            };
        }

    }

    public class FixtureExample implements Fixture {
        private Integer id;

        public FixtureExample() {
        }

        /**
         * An example implementation of a fixture producer. 
         * 
         * @see org.deephacks.openconfig.internal.junit.Fixture#createFixtures()
         */
        @Override
        public List<Fixture> createFixtures() {
            List<Fixture> fixtures = new ArrayList<Fixture>();
            for (int i = 0; i < 3; i++) {
                FixtureExample fix = new FixtureExample();
                fix.id = i;
                fixtures.add(fix);
            }
            return fixtures;
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
