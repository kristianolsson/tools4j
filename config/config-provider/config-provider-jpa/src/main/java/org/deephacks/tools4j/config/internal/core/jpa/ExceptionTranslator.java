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

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.PersistenceException;

import org.deephacks.tools4j.config.Bean.BeanId;
import org.deephacks.tools4j.config.Events;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public class ExceptionTranslator {
    private static Logger LOG = LoggerFactory.getLogger(ExceptionTranslator.class);

    private static final String MYSQL_INTEGRITY_CONSTRAINT_VIOLATION = "com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException";

    public static void translateDelete(Collection<BeanId> ids, PersistenceException e) {
        String rootCauseClassname = Throwables.getRootCause(e).getClass().getName();
        LOG.debug("", e);
        if (MYSQL_INTEGRITY_CONSTRAINT_VIOLATION.equals(rootCauseClassname)) {
            throw Events.CFG302_CANNOT_DELETE_BEAN(ids);
        } else {

        }

    }

    public static void translateDelete(Collection<BeanId> ids, DatabaseException e) {
        String rootCauseClassname = Throwables.getRootCause(e).getClass().getName();
        LOG.debug("", e);
        if (MYSQL_INTEGRITY_CONSTRAINT_VIOLATION.equals(rootCauseClassname)) {
            throw Events.CFG302_CANNOT_DELETE_BEAN(ids);
        } else {

        }

    }

    public static void translateMerge(BeanId id, PersistenceException e) {
        String rootCauseClassname = Throwables.getRootCause(e).getClass().getName();
        LOG.debug("", e);
        if (MYSQL_INTEGRITY_CONSTRAINT_VIOLATION.equals(rootCauseClassname)) {
            throw Events.CFG301_MISSING_RUNTIME_REF(id);
        } else {

        }
        throw e;

    }

    public static void translateDelete(Collection<String> ids, String schemaName,
            PersistenceException e) {
        Collection<BeanId> beanIds = new ArrayList<BeanId>();
        for (String id : ids) {
            beanIds.add(BeanId.create(id, schemaName));
        }
        String rootCauseClassname = Throwables.getRootCause(e).getClass().getName();
        LOG.debug("", e);
        if (MYSQL_INTEGRITY_CONSTRAINT_VIOLATION.equals(rootCauseClassname)) {
            throw Events.CFG302_CANNOT_DELETE_BEAN(beanIds);
        } else {

        }
        throw e;
    }

    public static void translateDelete(Collection<String> ids, String schemaName,
            DatabaseException e) {
        Collection<BeanId> beanIds = new ArrayList<BeanId>();
        for (String id : ids) {
            beanIds.add(BeanId.create(id, schemaName));
        }
        String rootCauseClassname = Throwables.getRootCause(e).getClass().getName();
        LOG.debug("", e);
        if (MYSQL_INTEGRITY_CONSTRAINT_VIOLATION.equals(rootCauseClassname)) {
            throw Events.CFG302_CANNOT_DELETE_BEAN(beanIds);
        } else {

        }
        throw e;
    }

}
