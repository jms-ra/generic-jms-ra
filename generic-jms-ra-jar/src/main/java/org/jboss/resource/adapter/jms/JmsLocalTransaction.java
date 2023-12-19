/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransaction;

/**
 * JMS Local transaction
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsLocalTransaction implements LocalTransaction {

    protected JmsManagedConnection mc;

    public JmsLocalTransaction(final JmsManagedConnection mc) {
        this.mc = mc;
    }

    @Override
    public void begin() throws ResourceException {
    }

    @Override
    public void commit() throws ResourceException {
        mc.lock();
        try {
            if (mc.getSession().getTransacted()) {
                mc.getSession().commit();
            }
        } catch (JMSException e) {
            throw new ResourceException("Could not commit LocalTransaction", e);
        } finally {
            mc.unlock();
        }
    }

    @Override
    public void rollback() throws ResourceException {
        mc.lock();
        try {
            if (mc.getSession().getTransacted()) {
                mc.getSession().rollback();
            }
        } catch (JMSException ex) {
            throw new ResourceException("Could not rollback LocalTransaction", ex);
        } finally {
            mc.unlock();
        }
    }
}
