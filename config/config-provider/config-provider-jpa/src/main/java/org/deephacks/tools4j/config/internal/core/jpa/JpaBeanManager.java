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

import static org.deephacks.tools4j.config.internal.core.jpa.ExceptionTranslator.translateDelete;
import static org.deephacks.tools4j.config.internal.core.jpa.ExceptionTranslator.translateMerge;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaBean.deleteJpaBean;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaBean.findJpaBean;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaBean.findJpaBeans;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaContextUtils.commit;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaContextUtils.getEm;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaContextUtils.rollback;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaProperty.deleteProperties;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaProperty.deleteProperty;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaRef.deleteReference;
import static org.deephacks.tools4j.config.internal.core.jpa.JpaRef.deleteReferences;
import static org.deephacks.tools4j.config.model.BeanUtils.uniqueIndex;
import static org.deephacks.tools4j.config.model.Events.CFG301_MISSING_RUNTIME_REF;
import static org.deephacks.tools4j.config.model.Events.CFG303_BEAN_ALREADY_EXIST;
import static org.deephacks.tools4j.config.model.Events.CFG304_BEAN_DOESNT_EXIST;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.support.ServiceProvider;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaBeanManager is responsible for driving transactions, joining/starting and rolling them back.
 */
@ServiceProvider(service = BeanManager.class)
public class JpaBeanManager extends BeanManager {
    private static final long serialVersionUID = -1356093069248894779L;
    private Logger log = LoggerFactory.getLogger(JpaBeanManager.class);
    private Conversion conversion;

    public JpaBeanManager() {
        conversion = Conversion.get();
        conversion.register(new JpaBeanToBeanConverter());
    }

    @Override
    public void create(Bean bean) {
        try {
            createJpaBean(bean);
            createJpaRefs(bean);
            commit();
        } catch (Throwable e) {
            rollback();
            throw e;
        }

    }

    private JpaBean createJpaBean(Bean bean) {
        JpaBean jpaBean = findJpaBean(bean.getId());
        if (jpaBean != null) {
            throw CFG303_BEAN_ALREADY_EXIST(bean.getId());
        }
        jpaBean = new JpaBean(bean);
        getEm().persist(jpaBean);
        createJpaProperties(bean);
        return jpaBean;
    }

    private void createJpaRefs(Bean bean) {
        for (String name : bean.getReferenceNames()) {
            List<BeanId> refs = bean.getReference(name);
            if (refs == null) {
                continue;
            }
            for (BeanId id : refs) {
                JpaBean target = findJpaBean(id);
                if (target == null) {
                    throw CFG301_MISSING_RUNTIME_REF(bean.getId(), id);
                }
                getEm().persist(new JpaRef(bean.getId(), target.getId(), name));
            }
        }
    }

    private void createJpaProperties(Bean bean) {
        for (String name : bean.getPropertyNames()) {
            List<String> values = bean.getValues(name);
            if (values == null) {
                continue;
            }
            for (String value : values) {
                getEm().persist(new JpaProperty(bean.getId(), name, value));
            }
        }
    }

    @Override
    public void create(Collection<Bean> beans) {
        try {
            for (Bean bean : beans) {
                createJpaBean(bean);
            }
            getEm().flush();
            for (Bean bean : beans) {
                createJpaRefs(bean);
            }
            commit();
        } catch (Throwable e) {
            rollback();
            throw e;
        }
    }

    @Override
    public void delete(BeanId id) {
        try {
            deleteJpaBean(id);
            commit();
        } catch (PersistenceException e) {
            rollback();
            translateDelete(Arrays.asList(id), e);
        } catch (DatabaseException e) {
            rollback();
            translateDelete(Arrays.asList(id), e);

        } catch (Throwable e) {
            throw e;
        }
    }

    @Override
    public void delete(String schemaName, Collection<String> ids) {
        try {
            for (String id : ids) {
                deleteJpaBean(BeanId.create(id, schemaName));
            }
            commit();
            rollback();
        } catch (PersistenceException e) {
            rollback();
            translateDelete(ids, schemaName, e);
        } catch (DatabaseException e) {
            rollback();
            translateDelete(ids, schemaName, e);

        } catch (Throwable e) {
            throw e;
        }
    }

    @Override
    public Bean get(BeanId id) {
        try {
            JpaBean bean = findJpaBean(id);
            if (bean == null) {
                throw CFG304_BEAN_DOESNT_EXIST(id);
            }
            commit();
            return conversion.convert(bean, Bean.class);
        } catch (Throwable e) {
            rollback();
            throw e;
        }
    }

    @Override
    public Map<BeanId, Bean> list(String schemaName) {
        try {
            List<JpaBean> beans = findJpaBeans(schemaName);
            Map<BeanId, Bean> map = toBeans(beans);
            commit();
            return map;
        } catch (Throwable e) {
            rollback();
            throw e;
        }
    }

    @Override
    public void merge(Bean bean) {
        try {
            JpaBean stored = findJpaBean(bean.getId());
            if (stored == null) {
                throw CFG304_BEAN_DOESNT_EXIST(bean.getId());
            }
            getEm().detach(stored);
            for (String name : bean.getPropertyNames()) {
                deleteProperty(stored.getId(), name);
                List<String> values = bean.getValues(name);
                if (values == null) {
                    continue;
                }
                for (String value : values) {
                    getEm().persist(new JpaProperty(stored.getId(), name, value));
                }
            }

            for (String name : bean.getReferenceNames()) {
                deleteReference(stored.getId(), name);
                List<BeanId> refs = bean.getReference(name);
                if (refs == null) {
                    continue;
                }
                for (BeanId beanId : refs) {
                    JpaBean target = findJpaBean(beanId);
                    if (target == null) {
                        throw CFG301_MISSING_RUNTIME_REF(bean.getId(), beanId);
                    }
                    getEm().persist(new JpaRef(bean.getId(), target.getId(), name));
                }
            }
            commit();
        } catch (PersistenceException e) {
            rollback();
            log.debug("Merge failed.", e);
            translateMerge(bean.getId(), e);
        } catch (Throwable e) {
            rollback();
            log.debug("Merge failed.", e);
            throw e;
        }
    }

    @Override
    public void merge(Collection<Bean> beans) {
        try {
            for (Bean bean : beans) {
                JpaBean stored = findJpaBean(bean.getId());
                if (stored == null) {
                    throw CFG304_BEAN_DOESNT_EXIST(bean.getId());
                }
                getEm().detach(stored);
                for (String name : bean.getPropertyNames()) {
                    deleteProperty(stored.getId(), name);
                    List<String> values = bean.getValues(name);
                    if (values == null) {
                        continue;
                    }
                    for (String v : values) {
                        getEm().persist(new JpaProperty(stored.getId(), name, v));
                    }
                }

                for (String name : bean.getReferenceNames()) {
                    deleteReference(stored.getId(), name);
                    List<BeanId> refs = bean.getReference(name);
                    if (refs == null) {
                        continue;
                    }
                    for (BeanId beanId : refs) {
                        getEm().persist(new JpaRef(bean.getId(), beanId, name));
                    }
                }
                getEm().merge(stored);

            }
            commit();
        } catch (Throwable e) {
            rollback();
            throw e;
        }

    }

    @Override
    public void set(Bean bean) {
        try {
            JpaBean stored = findJpaBean(bean.getId());
            if (stored == null) {
                throw CFG304_BEAN_DOESNT_EXIST(bean.getId());
            }
            deleteProperties(bean.getId());
            deleteReferences(bean.getId());
            createJpaProperties(bean);
            createJpaRefs(bean);
            commit();
        } catch (Throwable e) {
            rollback();
            throw e;
        }

    }

    @Override
    public void set(Collection<Bean> beans) {
        try {
            for (Bean bean : beans) {
                JpaBean stored = findJpaBean(bean.getId());
                if (stored == null) {
                    throw CFG304_BEAN_DOESNT_EXIST(bean.getId());
                }
                deleteProperties(bean.getId());
                deleteReferences(bean.getId());
                createJpaProperties(bean);
                createJpaRefs(bean);
            }
            commit();
        } catch (Throwable e) {
            rollback();
            throw e;
        }
    }

    private Map<BeanId, Bean> toBeans(List<JpaBean> jpabeans) {
        return uniqueIndex(conversion.convert(jpabeans, Bean.class));
    }
}
