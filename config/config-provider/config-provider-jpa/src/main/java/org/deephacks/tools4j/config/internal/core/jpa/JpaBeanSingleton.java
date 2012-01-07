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

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Objects.toStringHelper;
import static org.deephacks.tools4j.support.web.jpa.ThreadLocalEntityManager.getEm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "CONFIG_BEAN_SINGLETON")
@NamedQueries({ @NamedQuery(name = JpaBeanSingleton.FIND_BEAN_FROM_SCHEMA_NAME,
        query = JpaBeanSingleton.FIND_BEAN_FROM_SCHEMA), })
public class JpaBeanSingleton implements Serializable {
    @Id
    @Column(name = "BEAN_SCHEMA_NAME", nullable = false)
    protected String schemaName;

    protected static final String FIND_BEAN_FROM_SCHEMA = "SELECT DISTINCT e FROM JpaBeanSingleton e WHERE e.schemaName = ?1";
    protected static final String FIND_BEAN_FROM_SCHEMA_NAME = "FIND_BEAN_FROM_SCHEMA_NAME";

    public static boolean isJpaBeanSingleton(String schemaName) {
        Query query = getEm().createNamedQuery(FIND_BEAN_FROM_SCHEMA_NAME);
        query.setParameter(1, schemaName);
        try {
            query.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public JpaBeanSingleton() {

    }

    JpaBeanSingleton(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JpaBeanSingleton)) {
            return false;
        }
        JpaBeanSingleton o = (JpaBeanSingleton) obj;
        return equal(schemaName, o.schemaName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(schemaName);
    }

    @Override
    public String toString() {
        return toStringHelper(JpaBeanSingleton.class).add("schemaName", schemaName).toString();
    }

}
