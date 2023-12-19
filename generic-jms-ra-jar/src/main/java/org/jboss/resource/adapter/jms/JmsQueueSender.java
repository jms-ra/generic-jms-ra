/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueSender;

import org.jboss.logging.Logger;

/**
 * JmsQueueSender.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 */
public class JmsQueueSender extends JmsMessageProducer implements QueueSender {
    private static final Logger log = Logger.getLogger(JmsQueueSender.class);

    /**
     * Whether trace is enabled
     */
    private boolean trace = log.isTraceEnabled();

    /**
     * Create a new wrapper
     *
     * @param producer the producer
     * @param session  the session
     */
    public JmsQueueSender(QueueSender producer, JmsSession session) {
        super(producer, session);
    }

    @Override
    public Queue getQueue() throws JMSException {
        return ((QueueSender) producer).getQueue();
    }

    @Override
    public void send(Queue destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " destination=" + destination + " message=" + message + " deliveryMode=" + deliveryMode + " priority=" + priority + " ttl=" + timeToLive);
            }
            producer.send(destination, message, deliveryMode, priority, timeToLive);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public void send(Queue destination, Message message) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " destination=" + destination + " message=" + message);
            }
            producer.send(destination, message);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }
}
