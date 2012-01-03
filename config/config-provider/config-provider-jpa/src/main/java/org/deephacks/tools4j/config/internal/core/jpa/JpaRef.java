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
import javax.persistence.Transient;

import org.deephacks.tools4j.config.model.Bean.BeanId;

import com.google.common.base.Objects;

@Entity
@Table(name = "CONFIG_BEAN_REF")
@NamedQueries({
        @NamedQuery(name = JpaRef.DELETE_REF_USING_BEANID_NAME,
                query = JpaRef.DELETE_REF_USING_BEANID),
        @NamedQuery(name = JpaRef.DELETE_REF_USING_PROPNAME_NAME,
                query = JpaRef.DELETE_REF_USING_PROPNAME),
        @NamedQuery(name = JpaRef.FIND_REFS_FOR_BEAN_NAME, query = JpaRef.FIND_REFS_FOR_BEAN) })
public class JpaRef implements Serializable {

    private static final long serialVersionUID = -3528959706883881047L;

    @Id
    @Column(name = "UUID")
    String id;

    @Column(name = "FK_BEAN_ID", nullable = false)
    protected String sourceId;

    @Column(name = "FK_BEAN_SCHEMA_NAME", nullable = false)
    protected String sourceSchemaName;

    @Column(name = "FK_REF_BEAN_ID", nullable = false)
    protected String targetId;

    @Column(name = "FK_REF_BEAN_SCHEMA_NAME", nullable = false)
    protected String targetSchemaName;

    @Column(name = "PROP_NAME")
    private String propertyName;

    protected static final String DELETE_REF_USING_BEANID = "DELETE FROM JpaRef e WHERE e.sourceId = ?1 AND e.sourceSchemaName= ?2";
    protected static final String DELETE_REF_USING_BEANID_NAME = "DELETE_REF_USING_BEANID_NAME";
    @Transient
    private JpaBean target;

    public static void deleteReferences(BeanId id) {
        Query query = getEm().createNamedQuery(DELETE_REF_USING_BEANID_NAME);
        query.setParameter(1, id.getInstanceId());
        query.setParameter(2, id.getSchemaName());
        query.executeUpdate();
    }

    protected static final String DELETE_REF_USING_PROPNAME = "DELETE FROM JpaRef e WHERE e.sourceId = ?1 AND e.sourceSchemaName= ?2 AND  e.propertyName= ?3";
    protected static final String DELETE_REF_USING_PROPNAME_NAME = "DELETE_REF_USING_PROPNAME_NAME";

    public static void deleteReference(BeanId id, String propName) {
        Query query = getEm().createNamedQuery(DELETE_REF_USING_PROPNAME_NAME);
        query.setParameter(1, id.getInstanceId());
        query.setParameter(2, id.getSchemaName());
        query.setParameter(3, propName);
        query.executeUpdate();
    }

    protected static final String FIND_REFS_FOR_BEAN = "SELECT e FROM JpaRef e WHERE e.sourceId= ?1 AND e.sourceSchemaName= ?2";
    protected static final String FIND_REFS_FOR_BEAN_NAME = "FIND_REFS_FOR_BEAN_NAME";

    @SuppressWarnings("unchecked")
    public static List<JpaRef> findReferences(BeanId id) {
        Query query = getEm().createNamedQuery(FIND_REFS_FOR_BEAN_NAME);
        query.setParameter(1, id.getInstanceId());
        query.setParameter(2, id.getSchemaName());
        return (List<JpaRef>) query.getResultList();
    }

    public JpaRef() {

    }

    public JpaRef(JpaBean source, JpaBean target, String propName) {
        this.sourceId = source.getId().getInstanceId();
        this.sourceSchemaName = source.getId().getSchemaName();
        this.targetId = target.getId().getInstanceId();
        this.targetSchemaName = target.getId().getSchemaName();
        this.propertyName = propName;
        this.id = UUID.randomUUID().toString();
    }

    public JpaRef(BeanId source, BeanId target, String propName) {
        this.sourceId = source.getInstanceId();
        this.sourceSchemaName = source.getSchemaName();
        this.targetId = target.getInstanceId();
        this.targetSchemaName = target.getSchemaName();
        this.propertyName = propName;
        this.id = UUID.randomUUID().toString();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public BeanId getSource() {
        return BeanId.create(sourceId, sourceSchemaName);
    }

    public BeanId getTarget() {
        return BeanId.create(targetId, targetSchemaName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JpaRef)) {
            return false;
        }
        JpaRef o = (JpaRef) obj;
        return equal(id, o.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void setTargetBean(JpaBean target) {
        this.target = target;
    }

    public JpaBean getTargetBean() {
        return target;
    }

}
