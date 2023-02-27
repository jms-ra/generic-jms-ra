/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.JMSSecurityException;
import jakarta.jms.JMSSecurityRuntimeException;
import jakarta.jms.QueueConnection;
import jakarta.jms.Session;
import jakarta.jms.TopicConnection;
import javax.naming.Reference;
import jakarta.resource.Referenceable;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.util.Strings;
import org.jboss.resource.adapter.jms.util.TransactionUtils;

/**
 * The the connection factory implementation for the JMS RA.
 *
 *
 * This object will be the QueueConnectionFactory or TopicConnectionFactory
 * which clients will use to create connections.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsConnectionFactoryImpl implements JmsConnectionFactory, Referenceable {

    private static final long serialVersionUID = -5135366013101194277L;

    private static final Logger log = Logger.getLogger(JmsConnectionFactoryImpl.class);

    private ManagedConnectionFactory mcf;

    private ConnectionManager cm;

    private Reference reference;

    public JmsConnectionFactoryImpl(final ManagedConnectionFactory mcf, final ConnectionManager cm) {
        this.mcf = mcf;

        boolean trace = log.isTraceEnabled();
        if (cm == null) {
            // This is standalone usage, no appserver
            this.cm = new JmsConnectionManager();
            if (trace) {
                log.trace("Created new connection manager");
            }
        } else {
            this.cm = cm;
        }

        if (trace) {
            log.trace("Using ManagedConnectionFactory=" + mcf + ", ConnectionManager=" + cm);
        }
    }

    @Override
    public void setReference(final Reference reference) {
        this.reference = reference;

        if (log.isTraceEnabled()) {
            log.trace("Using Reference=" + reference);
        }
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        QueueConnection qc = new JmsSessionFactoryImpl(mcf, cm, QUEUE);

        if (log.isTraceEnabled()) {
            log.trace("Created queue connection: " + qc);
        }

        return qc;
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, QUEUE);
        s.setUserName(userName);
        s.setPassword(Strings.toCharArray(password));

        if (log.isTraceEnabled()) {
            log.trace("Created queue connection: " + s);
        }

        checkUserCredentials(s);
        return s;
    }

    /**
     * Check user credentials by creating an actual JMS connection and closes it
     */
    private void checkUserCredentials(JmsSessionFactoryImpl sessionFactory) throws JMSException {
        Session session = null;
        try {
            session = sessionFactory.createSession();
        } catch (JMSException e) {
            JMSException jmse = findRootJMSException(e);
            if (jmse != null) {
                throw jmse;
            } else {
                throw e;
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private static JMSException findRootJMSException(JMSException jmse) {
        if (jmse.getLinkedException() != null) {
            Throwable rootCause = getRootCause(jmse.getLinkedException());
            if (rootCause instanceof JMSException) {
                return (JMSException) rootCause;
            }
        }
        return jmse;
    }

    private static Throwable getRootCause(Throwable t) {
        Throwable cause;
        Throwable result = t;

        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }
        return result;
    }

    // --- TopicConnectionFactory
    public TopicConnection createTopicConnection() throws JMSException {
        TopicConnection tc = new JmsSessionFactoryImpl(mcf, cm, TOPIC);

        if (log.isTraceEnabled()) {
            log.trace("Created topic connection: " + tc);
        }

        return tc;
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, TOPIC);
        s.setUserName(userName);
        s.setPassword(Strings.toCharArray(password));

        if (log.isTraceEnabled()) {
            log.trace("Created topic connection: " + s);
        }

        checkUserCredentials(s);
        return s;
    }

    // --- JMS 1.1
    @Override
    public Connection createConnection() throws JMSException {
        Connection c = new JmsSessionFactoryImpl(mcf, cm, AGNOSTIC);

        if (log.isTraceEnabled()) {
            log.trace("Created connection: " + c);
        }

        return c;
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, AGNOSTIC);
        s.setUserName(userName);
        s.setPassword(Strings.toCharArray(password));

        if (log.isTraceEnabled()) {
            log.trace("Created connection: " + s);
        }

        checkUserCredentials(s);
        return s;
    }

    // -- JMS 2.0
    @Override
    public JMSContext createContext() {
        return createContext(null, null);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        return createContext(userName, password, TransactionUtils.isInTransaction() ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return createContext(null, null, sessionMode);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf, cm, JMS_CONTEXT);
        s.setUserName(userName);
        s.setPassword(Strings.toCharArray(password));
        int effectiveSessionMode = sessionMode;
        if (TransactionUtils.isInTransaction()) {
            effectiveSessionMode = Session.SESSION_TRANSACTED;
        }
        try {
            JmsSession session = s.allocateConnection((effectiveSessionMode == Session.SESSION_TRANSACTED), effectiveSessionMode, JMS_CONTEXT);
            return new GenericJmsContext(s, session);
        } catch (JMSException e) {
            try {
                s.close();
            } catch (JMSException e2) {
                // ignored by intention
            }
            JMSException jmse = findRootJMSException(e);
            if (jmse instanceof JMSSecurityException) {
                JMSSecurityRuntimeException rte = new JMSSecurityRuntimeException(jmse.getMessage(), jmse.getErrorCode(), jmse);
                throw rte;
            } else {
                JMSRuntimeException rte = new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
                throw rte;
            }
        }
    }
}
