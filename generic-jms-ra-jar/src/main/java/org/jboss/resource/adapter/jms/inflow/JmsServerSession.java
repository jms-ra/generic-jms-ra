/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms.inflow;

import org.jboss.logging.Logger;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ServerSession;
import jakarta.jms.Session;
import jakarta.jms.XAConnection;
import jakarta.jms.XASession;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

/**
 * 
 * A generic jms session pool.
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com>Weston Price</a>
 * @author <a href="mailto:jbertram@rehat.com>Justin Bertram</a>
 */
public class JmsServerSession implements ServerSession, MessageListener, Work, WorkListener {
    /**
     * The log
     */
    private static final Logger log = Logger.getLogger(JmsServerSession.class);

    /**
     * The session pool
     */
    JmsServerSessionPool pool;

    /**
     * The session
     */
    Session session;

    /**
     * Any XA session
     */
    XASession xaSession;

    /**
     * The endpoint
     */
    MessageEndpoint endpoint;

    TransactionManager tm;

    /**
     * Create a new JmsServerSession
     *
     * @param pool the server session pool
     */
    public JmsServerSession(JmsServerSessionPool pool) {
        this.pool = pool;
    }

    /**
     * Setup the session
     */
    public void setup() throws Exception {
        JmsActivation activation = pool.getActivation();
        JmsActivationSpec spec = activation.getActivationSpec();
        Connection connection = activation.getConnection();
        XAResource xaResource = null;
        tm = activation.getTransactionManager();

        // Get the endpoint
        MessageEndpointFactory endpointFactory = activation.getMessageEndpointFactory();

        // Create the session
        if (activation.isDeliveryTransacted) {
            if (connection instanceof XAConnection) {
                log.debug("Delivery is transacted, and client JMS implementation properly implements jakarta.jms.XAConnection.");
                xaSession = ((XAConnection) connection).createXASession();
                session = xaSession.getSession();
                xaResource = xaSession.getXAResource();
            } else {
                throw new Exception("Delivery is transacted, but client JMS implementation does not properly implement the necessary interfaces as described in section 8 of the JMS 1.1 specification.");
            }
        } else {
            session = connection.createSession(false, spec.getAcknowledgeModeInt());
        }

        endpoint = endpointFactory.createEndpoint(xaResource);

        // Set the message listener
        session.setMessageListener(this);
    }

    /**
     * Stop the session
     */
    public void teardown() {
        try {
            if (endpoint != null) {
                endpoint.release();
            }
        } catch (Throwable t) {
            log.debug("Error releasing endpoint " + endpoint, t);
        }

        try {
            if (xaSession != null) {
                xaSession.close();
            }
        } catch (Throwable t) {
            log.debug("Error closing xaSession " + xaSession, t);
        }

        try {
            if (session != null) {
                session.close();
            }
        } catch (Throwable t) {
            log.debug("Error closing session " + session, t);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            final int timeout = pool.getActivation().getActivationSpec().getTransactionTimeout();

            if (timeout > 0) {
                log.trace("Setting transactionTimeout for JMSSessionPool to " + timeout);
                tm.setTransactionTimeout(timeout);
            }

            endpoint.beforeDelivery(JmsActivation.ONMESSAGE);

            try {
                MessageListener listener = (MessageListener) endpoint;
                listener.onMessage(message);
            } finally {
                endpoint.afterDelivery();
            }
        } catch (Throwable t) {
            try {
               log.error("Unexpected error delivering message. JMSMessageID is " + message.getJMSMessageID(), t);
            } catch (JMSException e) {
               log.error("Unexpected error delivering message. JMSMessageID cannot be determined.", t);
            }
        }
    }

    @Override
    public Session getSession() throws JMSException {
        return session;
    }

    @Override
    public void start() throws JMSException {
        JmsActivation activation = pool.getActivation();
        WorkManager workManager = activation.getWorkManager();
        try {
            workManager.scheduleWork(this, 0, null, this);
        } catch (WorkException e) {
            log.error("Unable to schedule work", e);
            throw new JMSException("Unable to schedule work: " + e.toString());
        }
    }

    @Override
    public void run() {
        session.run();
    }

    @Override
    public void release() {
    }

    @Override
    public void workAccepted(WorkEvent e) {
    }

    @Override
    public void workCompleted(WorkEvent e) {
        pool.returnServerSession(this);
    }

    @Override
    public void workRejected(WorkEvent e) {
        pool.returnServerSession(this);
    }

    @Override
    public void workStarted(WorkEvent e) {
    }
}
