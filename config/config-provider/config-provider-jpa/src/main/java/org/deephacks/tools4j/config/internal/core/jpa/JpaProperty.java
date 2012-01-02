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
import static org.deephacks.tools4j.config.internal.core.jpa.JpaContextUtils.getEm;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import org.deephacks.tools4j.config.Bean.BeanId;

import com.google.common.base.Objects;

/**
 * 
 * @author Kristoffer Sjogren / ekrisjo
 */
@Entity
@Table(name = "CONFIG_PROPERTY")
@NamedQueries({
        @NamedQuery(name = JpaProperty.DELETE_ALL_PROPERTIES_FOR_BEANID_NAME,
                query = JpaProperty.DELETE_ALL_PROPERTIES_FOR_BEANID),
        @NamedQuery(name = JpaProperty.DELETE_PROPERTY_FOR_BEANID_NAME,
                query = JpaProperty.DELETE_PROPERTY_FOR_BEANID),
        @NamedQuery(name = JpaProperty.FIND_PROPERTIES_FOR_BEAN_NAME,
                query = JpaProperty.FIND_PROPERTIES_FOR_BEAN) })
public class JpaProperty implements Serializable {
    private static final long serialVersionUID = -8467786505761160478L;

    @Id
    @Column(name = "UUID")
    private String uuid;

    @Column(name = "FK_BEAN_ID", nullable = false)
    private String id;

    @Column(name = "FK_BEAN_SCHEMA_NAME", nullable = false)
    private String schemaName;

    @Column(name = "PROP_NAME", nullable = false)
    private String propName;

    @Column(name = "PROP_VALUE")
    private String value;

    protected static final String DELETE_ALL_PROPERTIES_FOR_BEANID = "DELETE FROM JpaProperty e WHERE e.id = ?1 AND e.schemaName= ?2";
    protected static final String DELETE_ALL_PROPERTIES_FOR_BEANID_NAME = "DELETE_ALL_PROPERTIES_FOR_BEANID";

    public static void deleteProperties(BeanId id) {
        Query query = getEm().createNamedQuery(DELETE_ALL_PROPERTIES_FOR_BEANID_NAME);
        query.setParameter(1, id.getInstanceId());
        query.setParameter(2, id.getSchemaName());
        query.executeUpdate();
    }

    protected static final String DELETE_PROPERTY_FOR_BEANID = "DELETE FROM JpaProperty e WHERE e.id = ?1 AND e.schemaName= ?2 AND e.propName = ?3";
    protected static final String DELETE_PROPERTY_FOR_BEANID_NAME = "DELETE_PROPERTY_FOR_BEANID_NAME";

    public static void deleteProperty(BeanId id, String propName) {
        Query query = getEm().createNamedQuery(DELETE_PROPERTY_FOR_BEANID_NAME);
        query.setParameter(1, id.getInstanceId());
        query.setParameter(2, id.getSchemaName());
        query.setParameter(3, propName);
        query.executeUpdate();
    }

    protected static final String FIND_PROPERTIES_FOR_BEAN = "SELECT e FROM JpaProperty e WHERE e.id= ?1 AND e.schemaName= ?2";
    protected static final String FIND_PROPERTIES_FOR_BEAN_NAME = "FIND_PROPERTIES_FOR_BEAN_NAME";

    @SuppressWarnings("unchecked")
    public static List<JpaProperty> findProperties(BeanId id) {
        Query query = getEm().createNamedQuery(FIND_PROPERTIES_FOR_BEAN_NAME);
        query.setParameter(1, id.getInstanceId());
        query.setParameter(2, id.getSchemaName());
        return (List<JpaProperty>) query.getResultList();
    }

    public JpaProperty() {

    }

    JpaProperty(BeanId owner, String name, String value) {
        this.uuid = UUID.randomUUID().toString();
        this.id = owner.getInstanceId();
        this.schemaName = owner.getSchemaName();
        this.propName = name;
        this.value = value;
    }

    public BeanId getId() {
        return BeanId.create(id, schemaName);
    }

    public String getPropertyName() {
        return propName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JpaProperty)) {
            return false;
        }
        JpaProperty o = (JpaProperty) obj;
        return equal(uuid, o.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
