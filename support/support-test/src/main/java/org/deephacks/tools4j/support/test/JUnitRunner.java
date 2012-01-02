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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnitRunner is responsible for making Rules and Parameterized tests easier to manage.
 * 
 */
public class JUnitRunner extends BlockJUnit4ClassRunner {

    public JUnitRunner(Class<?> type) throws InitializationError {
        super(type);
    }

    @Override
    public Object createTest() throws Exception {
        return getTestClass().getOnlyConstructor().newInstance();
    }

    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
            boolean isStatic, List<Throwable> errors) {
        // ignore no-arg methods
    }

    @Override
    public List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> currentMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<FrameworkMethod> methodsToReturn = new ArrayList<FrameworkMethod>();
        for (FrameworkMethod frameworkMethod : currentMethods) {
            Class<?>[] methodArgs = frameworkMethod.getMethod().getParameterTypes();
            if (methodArgs.length == 0) {
                methodsToReturn.add(frameworkMethod);
                continue;
            }

            Class<?>[] args = frameworkMethod.getMethod().getParameterTypes();
            List<List<Fixture>> fixtureList = new ArrayList<List<Fixture>>();
            for (int i = 0; i < args.length; i++) {
                if (!Fixture.class.isAssignableFrom(args[i])) {
                    throw new RuntimeException(frameworkMethod.getName() + " argument " + i
                            + " may only be of type Fixture.");
                }
                List<Fixture> fixtures = ((Fixture) invokeConstructor(args[i])).createFixtures();
                fixtureList.add(fixtures);
            }
            List<List<Fixture>> fixtures = getCombinations(fixtureList);

            for (List<Fixture> fixture : fixtures) {
                methodsToReturn.add(new FrameworkMethodWithArgument(frameworkMethod.getMethod(),
                        fixture));
            }
        }
        return methodsToReturn;
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }

    /**
     * There was no natural way of extend this method, so its almost completely copied from BlockJUnit4ClassRunner. 
     * 
     * @see org.junit.runners.BlockJUnit4ClassRunner#methodBlock(org.junit.runners.model.FrameworkMethod)
     */
    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
            test = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }

        Statement statement = methodInvoker(method, test);
        statement = possiblyExpectingExceptions(method, test, statement);
        statement = withPotentialTimeout(method, test, statement);
        statement = withBefores(method, test, statement);
        statement = withAfters(method, test, statement);
        statement = withRules(method, test, statement);
        // this is the only add on.
        statement = withRuleHandler(method, test, statement);

        return statement;
    }

    private Statement withRuleHandler(FrameworkMethod method, Object test, Statement statement) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            RuleHandler rulehandler = annotation.annotationType().getAnnotation(RuleHandler.class);
            if (rulehandler == null) {
                continue;
            }
            Class<? extends MethodRule> ruleclazz = rulehandler.value();
            MethodRule rule = (MethodRule) invokeConstructor(ruleclazz);
            statement = rule.apply(statement, method, test);
        }
        return statement;
    }

    /**
     * Simply copied from BlockJUnit4ClassRunner, since its not accessible.
     */
    private Statement withRules(FrameworkMethod method, Object target, Statement statement) {
        Statement result = statement;
        for (MethodRule each : getTestClass().getAnnotatedFieldValues(target, Rule.class,
                MethodRule.class))
            result = each.apply(result, method, target);
        return result;
    }

    private Object invokeConstructor(Class<?> arg) {
        try {
            Class<?> enclosing = arg.getEnclosingClass();
            if (enclosing == null) {
                return arg.getConstructor().newInstance();
            }
            Object o = enclosing.newInstance();
            Constructor<?> c = arg.getConstructor(enclosing);

            return c.newInstance(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns all unique combinations of a set of lists.  
     * 
     * @param listOfLists set of lists
     * @return all possible combinations.
     */
    private static List<List<Fixture>> getCombinations(List<List<Fixture>> listOfLists) {
        List<List<Fixture>> returned = new ArrayList<List<Fixture>>();
        if (listOfLists.size() == 1) {
            for (Fixture fixture : listOfLists.get(0)) {
                List<Fixture> list = new ArrayList<Fixture>();
                list.add(fixture);
                returned.add(list);
            }
            return returned;
        }
        List<Fixture> fixtureList = listOfLists.get(0);
        for (List<Fixture> possibleList : getCombinations(listOfLists
                .subList(1, listOfLists.size()))) {
            for (Fixture firstFixture : fixtureList) {
                List<Fixture> addedList = new ArrayList<Fixture>();
                addedList.add(firstFixture);
                addedList.addAll(possibleList);
                returned.add(addedList);
            }
        }
        return returned;
    }

    public class FrameworkMethodWithArgument extends FrameworkMethod {
        private Method myMethod;
        private List<Fixture> fixture;

        /**
         * @param method
         */
        public FrameworkMethodWithArgument(Method method) {
            super(method);
        }

        public FrameworkMethodWithArgument(Method method, List<Fixture> fixture) {
            super(method);
            myMethod = method;
            this.fixture = fixture;
        }

        @Override
        public Object invokeExplosively(final Object target, final Object... params)
                throws Throwable {
            return new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return myMethod.invoke(target, fixture.toArray(new Fixture[fixture.size()]));
                }
            }.run();
        }
    }
}
