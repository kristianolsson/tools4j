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
package org.deephacks.tools4j.config.test;

import static org.deephacks.tools4j.config.test.BeanUnitils.toBeans;

import java.util.Collection;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.test.ConfigTestData.Child;
import org.deephacks.tools4j.config.test.ConfigTestData.Grandfather;
import org.deephacks.tools4j.config.test.ConfigTestData.JSR303Validation;
import org.deephacks.tools4j.config.test.ConfigTestData.Parent;
import org.deephacks.tools4j.config.test.ConfigTestData.Singleton;
import org.deephacks.tools4j.config.test.ConfigTestData.SingletonParent;

import com.google.common.collect.ImmutableList;

public abstract class ConfigDefaultSetup {
    protected RuntimeContext runtime;
    protected AdminContext admin;
    protected ConfigTestData testdata = new ConfigTestData();
    protected Child c1;
    protected Child c2;
    protected Parent p1;
    protected Parent p2;
    protected Grandfather g1;
    protected Grandfather g2;
    protected SingletonParent sp1;
    protected Singleton s1;
    protected JSR303Validation jsr303;
    protected static Collection<Bean> defaultBeans;

    protected void setupDefaultConfigData() {
        if (runtime == null) {
            runtime = RuntimeContext.get();
        }

        if (admin == null) {
            admin = AdminContext.get();
        }
        sp1 = testdata.getSingletonParent("sp1");
        s1 = testdata.getSingleton();

        c1 = testdata.getChild("c1");
        c2 = testdata.getChild("c2");

        p1 = testdata.getParent("p1");
        p1.add(c2, c1);
        p1.set(c1);
        p1.put(c1);
        p1.put(c2);

        p2 = testdata.getParent("p2");
        p2.add(c1, c2);
        p2.set(c2);
        p2.put(c1);
        p2.put(c2);

        g1 = testdata.getGrandfather("g1");
        g1.add(p1, p2);

        g2 = testdata.getGrandfather("g2");
        g2.add(p1, p2);
        g2.put(p1);

        jsr303 = testdata.getJSR303Validation("jsr303");

        runtime.register(Grandfather.class, Parent.class, Child.class, Singleton.class,
                SingletonParent.class, JSR303Validation.class);
        if (defaultBeans == null) {
            // toBeans steals quite a bit of performance when having larger hierarchies. 
            defaultBeans = ImmutableList.copyOf(toBeans(c1, c2, p1, p2, g1, g2));
        }
    }
}
