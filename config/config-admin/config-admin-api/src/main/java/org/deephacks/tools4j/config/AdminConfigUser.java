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
package org.deephacks.tools4j.config;

import java.io.Serializable;

/**
 * Users are not intended to be used for authentication, but for model access
 * control. User identification can also be used to monitor changes.
 * 
 */
public final class AdminConfigUser implements Serializable {

    private static final long serialVersionUID = -2441335728401652598L;
    private String name;
    private String role;

    AdminConfigUser(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public static AdminConfigUser create(String name) {
        return new AdminConfigUser(name, null);
    }

    public static AdminConfigUser create(String name, String role) {
        return new AdminConfigUser(name, role);
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
