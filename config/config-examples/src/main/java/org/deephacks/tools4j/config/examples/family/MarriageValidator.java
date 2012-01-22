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
package org.deephacks.tools4j.config.examples.family;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MarriageValidator implements ConstraintValidator<MarriageConstraint, Marriage> {

    @Override
    public void initialize(MarriageConstraint constraint) {
    }

    @Override
    public boolean isValid(Marriage marriage, ConstraintValidatorContext context) {
        if (marriage.getCouple() == null || marriage.getCouple().size() != 2) {
            context.buildConstraintViolationWithTemplate("marriage must be between two pepole")
                    .addConstraintViolation();
            return false;
        }
        Person male = marriage.getMale();
        if (male == null) {
            context.buildConstraintViolationWithTemplate("no male in marriage")
                    .addConstraintViolation();
            return false;
        }
        Person female = marriage.getFemale();
        if (female == null) {
            context.buildConstraintViolationWithTemplate("no female in marriage")
                    .addConstraintViolation();
            return false;
        }

        if (!validwifeLastname(marriage, context)) {
            return false;
        }
        if (!validChildrenLastname(marriage, context)) {
            return false;
        }
        if (!consistentChildren(marriage, context)) {
            return false;
        }

        return true;
    }

    private boolean validwifeLastname(Marriage m, ConstraintValidatorContext context) {
        if (!m.getMale().getLastName().equals(m.getFemale().getLastName())) {
            context.buildConstraintViolationWithTemplate(
                    "female [" + m.getFemale() + "] lastname is not same as man [" + m.getMale()
                            + "]").addConstraintViolation();

            return false;
        }
        return true;
    }

    private boolean validChildrenLastname(Marriage marriage, ConstraintValidatorContext context) {
        for (Person child : marriage.getChildren()) {
            if (!validChildLastname(marriage.getMale(), child)) {
                context.buildConstraintViolationWithTemplate(
                        "child [" + child + "] have lastname not matching dad ["
                                + marriage.getMale() + "]").addConstraintViolation();

                return false;
            }
            if (!validChildLastname(marriage.getFemale(), child)) {
                context.buildConstraintViolationWithTemplate(
                        "child [" + child + "] have lastname not matching mom ["
                                + marriage.getFemale() + "]").addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    private boolean validChildLastname(Person parent, Person child) {
        if (!parent.lastName.equals(child.lastName)) {
            return false;
        }
        return true;

    }

    private boolean consistentChildren(Marriage m, ConstraintValidatorContext context) {
        List<Person> marriageChildren = m.getChildren();
        List<Person> personChildren = m.getMale().getChildren();

        if (!personChildren.containsAll(marriageChildren)) {
            context.buildConstraintViolationWithTemplate(
                    "Male [" + m.getMale() + "] does not have same children as marriage.")
                    .addConstraintViolation();

            return false;
        }
        personChildren = m.getFemale().getChildren();
        if (!personChildren.containsAll(marriageChildren)) {
            context.buildConstraintViolationWithTemplate(
                    "Female [" + m.getFemale() + "] does not have same children as marriage.")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
