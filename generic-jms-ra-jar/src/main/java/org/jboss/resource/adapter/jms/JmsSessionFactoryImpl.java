/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.util.HashSet;
import java.util.Iterator;

import jakarta.jms.ConnectionConsumer;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.IllegalStateException;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.QueueSession;
import jakarta.jms.ServerSessionPool;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.Topic;
import jakarta.jms.TopicSession;
import javax.naming.Reference;
import jakarta.resource.Referenceable;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
//import org.jboss.resource.connectionmanager.JTATransactionChecker;
import org.jboss.resource.adapter.jms.util.TransactionUtils;

/**
 * Implements the JMS Connection API and produces {@link JmsSession} objects.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsSessionFactoryImpl implements JmsSessionFactory, Referenceable {

    private static final Logger log = Logger.getLogger(JmsSessionFactoryImpl.class);

    /**
     * Are we closed?
     */
    private boolean closed = false;

    /**
     * Whether trace is enabled
     */
    private boolean trace = log.isTraceEnabled();

    private Reference reference;

    // Used from JmsConnectionFactory
    private String userName;
    private char[] password;
    private String clientID;
    private int type;

    /* Whether we are started */
    private boolean started = false;

    /**
     * JmsRa own factory
     */
    private JmsManagedConnectionFactory mcf;

    /**
     * Hook to the appserver
     */
    private ConnectionManager cm;

    /**
     * The sessions
     */
    private final HashSet<JmsSession> sessions = new HashSet<>();

    /**
     * The temporary queues
     */
    private final HashSet<TemporaryQueue> tempQueues = new HashSet<>();

    /**
     * The temporary topics
     */
    private final HashSet<TemporaryTopic> tempTopics = new HashSet<>();

    public JmsSessionFactoryImpl(final ManagedConnectionFactory mcf,
            final ConnectionManager cm,
            final int type) {
        this.mcf = (JmsManagedConnectionFactory) mcf;
        this.cm = cm;

        if (cm == null) {
            // This is standalone usage, no appserver
            this.cm = new JmsConnectionManager();
        } else {
            this.cm = cm;
        }

        this.type = type;

        if (trace) {
            log.trace("mcf=" + mcf + ", cm=" + cm + ", type=" + type);
        }
    }

    @Override
    public void setReference(final Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    // --- API for JmsConnectionFactoryImpl
    public void setUserName(final String name) {
        userName = name;
    }

    public void setPassword(final char[] password) {
        this.password = password;
    }

    //---- QueueConnection ---
    @Override
    public QueueSession createQueueSession(final boolean transacted,
            final int acknowledgeMode)
            throws JMSException {
        checkClosed();
        if (type == JmsConnectionFactory.TOPIC) {
            throw new IllegalStateException("Can not get a queue session from a topic connection");
        }
        return allocateConnection(transacted, acknowledgeMode, type);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Queue queue,
            String messageSelector,
            ServerSessionPool sessionPool,
            int maxMessages)
            throws JMSException {
        throw new IllegalStateException(ISE);
    }

    //--- TopicConnection ---
    @Override
    public TopicSession createTopicSession(final boolean transacted,
            final int acknowledgeMode)
            throws JMSException {
        checkClosed();
        if (type == JmsConnectionFactory.QUEUE) {
            throw new IllegalStateException("Can not get a topic session from a queue connection");
        }
        return allocateConnection(transacted, acknowledgeMode, type);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Topic topic,
            String messageSelector,
            ServerSessionPool sessionPool,
            int maxMessages)
            throws JMSException {
        throw new IllegalStateException(ISE);
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(
            Topic topic,
            String subscriptionName,
            String messageSelector,
            ServerSessionPool sessionPool,
            int maxMessages)
            throws JMSException {
        throw new IllegalStateException(ISE);
    }

    //--- All the Connection methods
    @Override
    public String getClientID() throws JMSException {
        checkClosed();

        if (clientID == null) {
            return mcf.getClientID();
        }

        return clientID;
    }

    @Override
    public void setClientID(String cID) throws JMSException {
        if (mcf.isStrict()) {
            throw new IllegalStateException(ISE);
        }

        checkClosed();
        if (clientID != null) {
            throw new IllegalStateException("Cannot change client id");
        }
        clientID = cID;
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        checkClosed();
        return mcf.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        throw new IllegalStateException(ISE);
    }

    @Override
    public void setExceptionListener(ExceptionListener listener)
            throws JMSException {
        throw new IllegalStateException(ISE);
    }

    @Override
    public void start() throws JMSException {
        checkClosed();
        if (trace) {
            log.trace("start() " + this);
        }
        synchronized (sessions) {
            if (started) {
                return;
            }
            started = true;
            for (Iterator<JmsSession> i = sessions.iterator(); i.hasNext();) {
                i.next().start();
            }
        }
    }

    @Override
    public void stop() throws JMSException {
        if (mcf.isStrict()) {
            throw new IllegalStateException(ISE);
        }
        checkClosed();
        if (trace) {
            log.trace("stop() " + this);
        }
        synchronized (sessions) {
            if (started == false) {
                return;
            }
            started = true;
            for (Iterator<JmsSession> i = sessions.iterator(); i.hasNext();) {
                i.next().stop();
            }
        }
    }

    public void close() throws JMSException {
        if (closed) {
            return;
        }
        closed = true;

        if (trace) {
            log.trace("close() " + this);
        }

        synchronized (sessions) {
            for (Iterator<JmsSession> i = sessions.iterator(); i.hasNext();) {
                try {
                    i.next().closeSession();
                } catch (Throwable t) {
                    log.trace("Error closing session", t);
                }
                i.remove();
            }
        }

        if (mcf.isDeleteTemporaryDestinations()) {
            synchronized (tempQueues) {
                for (Iterator<TemporaryQueue> i = tempQueues.iterator(); i.hasNext();) {
                    TemporaryQueue temp = i.next();
                    try {
                        if (trace) {
                            log.trace("Closing temporary queue " + temp + " for " + this);
                        }
                        temp.delete();
                    } catch (Throwable t) {
                        log.trace("Error deleting temporary queue", t);
                    }
                    i.remove();
                }
            }

            synchronized (tempTopics) {
                for (Iterator<TemporaryTopic> i = tempTopics.iterator(); i.hasNext();) {
                    TemporaryTopic temp = i.next();
                    try {
                        if (trace) {
                            log.trace("Closing temporary topic " + temp + " for " + this);
                        }
                        temp.delete();
                    } catch (Throwable t) {
                        log.trace("Error deleting temporary topic", t);
                    }
                    i.remove();
                }
            }
        }
    }

    @Override
    public void closeSession(JmsSession session) throws JMSException {
        synchronized (sessions) {
            sessions.remove(session);
        }
    }

    @Override
    public void addTemporaryQueue(TemporaryQueue temp) {
        synchronized (tempQueues) {
            tempQueues.add(temp);
        }
    }

    public void addTemporaryTopic(TemporaryTopic temp) {
        synchronized (tempTopics) {
            tempTopics.add(temp);
        }
    }

    // -- JMS 1.1
    public ConnectionConsumer createConnectionConsumer(Destination destination, ServerSessionPool pool, int maxMessages) throws JMSException {
        throw new IllegalStateException(ISE);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String name, ServerSessionPool pool, int maxMessages) throws JMSException {
        throw new IllegalStateException(ISE);
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode)
            throws JMSException {
        checkClosed();

        if (TransactionUtils.isInTransaction()) {
            transacted = true;
            acknowledgeMode = Session.SESSION_TRANSACTED;
        }
        return allocateConnection(transacted, acknowledgeMode, type);
    }

    // -- JMS 2.0
    @Override
    public Session createSession(int sessionMode) throws JMSException {
        checkClosed();

        boolean transacted = sessionMode == Session.SESSION_TRANSACTED;
        if (TransactionUtils.isInTransaction()) {
            transacted = true;
            sessionMode = Session.SESSION_TRANSACTED;
        }
        return allocateConnection(transacted, sessionMode, type);
    }

    @Override
    public Session createSession() throws JMSException {
        boolean transacted = false;
        int sessionMode = Session.AUTO_ACKNOWLEDGE;

        if (TransactionUtils.isInTransaction()) {
            transacted = true;
            sessionMode = Session.SESSION_TRANSACTED;
        }
        return allocateConnection(transacted, sessionMode, type);
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new IllegalStateException(ISE);
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new IllegalStateException(ISE);
    }

    protected JmsSession allocateConnection(boolean transacted, int acknowledgeMode, int sessionType) throws JMSException {
        try {
            synchronized (sessions) {
                if (mcf.isStrict() && sessions.isEmpty() == false) {
                    throw new IllegalStateException("Only allowed one session per connection. See the J2EE spec, e.g. J2EE1.4 Section 6.6");
                }

                if (transacted) {
                    acknowledgeMode = Session.SESSION_TRANSACTED;
                }

                JmsConnectionRequestInfo info = new JmsConnectionRequestInfo(transacted, acknowledgeMode, sessionType);
                info.setUserName(userName);
                info.setPassword(password);
                info.setClientID(clientID);
                info.setDefaults(mcf.getProperties());

                if (trace) {
                    log.trace("Allocating session for " + this + " with request info=" + info);
                }
                JmsSession session = (JmsSession) cm.allocateConnection(mcf, info);
                try {
                    if (trace) {
                        log.trace("Allocated  " + this + " session=" + session);
                    }
                    session.setJmsSessionFactory(this);
                    if (started) {
                        session.start();
                    }
                    sessions.add(session);
                    return session;
                } catch (Throwable t) {
                    try {
                        session.close();
                    } catch (Throwable ignored) {
                    }
                    if (t instanceof Exception) {
                        throw (Exception) t;
                    } else {
                        throw new RuntimeException("Unexpected error: ", t);
                    }
                }
            }
        } catch (Exception e) {
            log.error("could not create session", e);

            JMSException je = new JMSException("Could not create a session: " + e);
            je.initCause(e);
            je.setLinkedException(e);
            throw je;
        }
    }

    protected void checkClosed() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The connection is closed");
        }
    }
}
