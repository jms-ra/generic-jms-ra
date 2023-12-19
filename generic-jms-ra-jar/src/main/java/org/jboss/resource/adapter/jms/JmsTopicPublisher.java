/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import org.jboss.logging.Logger;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Topic;
import jakarta.jms.TopicPublisher;

/**
 * JmsQueueSender.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 */
public class JmsTopicPublisher extends JmsMessageProducer implements TopicPublisher {
    private static final Logger log = Logger.getLogger(JmsTopicPublisher.class);

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
    public JmsTopicPublisher(TopicPublisher producer, JmsSession session) {
        super(producer, session);
    }

    @Override
    public Topic getTopic() throws JMSException {
        return ((TopicPublisher) producer).getTopic();
    }

    @Override
    public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        session.lock();
        try {
        } finally {
            session.unlock();
        }
        if (trace) {
            log.trace("send " + this + " message=" + message + " deliveryMode=" + deliveryMode + " priority=" + priority + " ttl=" + timeToLive);
        }
        ((TopicPublisher) producer).publish(message, deliveryMode, priority, timeToLive);
        if (trace) {
            log.trace("sent " + this + " result=" + message);
        }
    }

    @Override
    public void publish(Message message) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " message=" + message);
            }
            ((TopicPublisher) producer).publish(message);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public void publish(Topic destination, Message message, int deliveryMode, int priority, long timeToLive)
            throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " destination=" + destination + " message=" + message + " deliveryMode=" + deliveryMode + " priority=" + priority + " ttl=" + timeToLive);
            }
            ((TopicPublisher) producer).publish(destination, message, deliveryMode, priority, timeToLive);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public void publish(Topic destination, Message message) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " destination=" + destination + " message=" + message);
            }
            ((TopicPublisher) producer).publish(destination, message);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }
}
