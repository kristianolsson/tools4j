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
package org.deephacks.tools4j.config.internal.core.test;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ChannelValidator implements ConstraintValidator<ValidChannel, List<Channel>> {

    @Override
    public void initialize(ValidChannel firstUpper) {
    }

    @Override
    public boolean isValid(List<Channel> channels, ConstraintValidatorContext context) {
        if (channels == null) {
            return false;
        }
        boolean valid = true;
        for (Channel channel : channels) {
            if (!"CBS".equals(channel.name)) {
                valid = false;
            }

        }
        return valid;
    }
}
