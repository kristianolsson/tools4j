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
package org.deephacks.tools4j.config.internal.core.jpa;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.Converter;

public class JpaBeanToBeanConverter implements Converter<JpaBean, Bean> {
    private Conversion conversion = Conversion.get();

    @Override
    public Bean convert(JpaBean source, Class<? extends Bean> specificType) {
        if (source == null) {
            return null;
        }
        BeanId id = BeanId.create(source.getPk().id, source.getPk().schemaName);
        Bean bean = Bean.create(id);
        if (bean.getId().getInstanceId().equals("g3")) {
            System.out.println("ff");
        }
        for (JpaRef ref : source.getReferences()) {
            JpaBean target = ref.getTargetBean();
            if (target == null) {
                continue;
            }
            Bean beanTarget = conversion.convert(target, Bean.class);
            BeanId idTarget = target.getId();
            idTarget.setBean(beanTarget);
            bean.addReference(ref.getPropertyName(), idTarget);
        }
        for (JpaProperty prop : source.getProperties()) {
            bean.addProperty(prop.getPropertyName(), prop.getValue());
        }
        return bean;
    }
}
