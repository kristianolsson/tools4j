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

import java.util.List;

/**
 * Fixture is responsible for providing fixture instances as input to parameterized test methods.
 */
public interface Fixture {
    /**
     * This is the method that produces the fixtures. 
     * The test method will be invoked once with every fixture.
     * 
     * @return The test fixtures.
     */
    public List<Fixture> createFixtures();
}
