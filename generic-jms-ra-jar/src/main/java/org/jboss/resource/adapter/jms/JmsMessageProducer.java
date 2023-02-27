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

import jakarta.jms.CompletionListener;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;

import org.jboss.logging.Logger;

/**
 * JmsMessageProducer.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMessageProducer implements MessageProducer {
    private static final Logger log = Logger.getLogger(JmsMessageConsumer.class);

    /**
     * The wrapped message producer
     */
    MessageProducer producer;

    /**
     * The session for this consumer
     */
    JmsSession session;

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
    public JmsMessageProducer(MessageProducer producer, JmsSession session) {
        this.producer = producer;
        this.session = session;

        if (trace) {
            log.trace("new JmsMessageProducer " + this + " producer=" + producer + " session=" + session);
        }
    }

    @Override
    public void close() throws JMSException {
        if (trace) {
            log.trace("close " + this);
        }
        try {
            closeProducer();
        } finally {
            session.removeProducer(this);
        }
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive)
            throws JMSException {
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
    public void send(Destination destination, Message message) throws JMSException {
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

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " message=" + message + " deliveryMode=" + deliveryMode + " priority=" + priority + " ttl=" + timeToLive);
            }
            producer.send(message, deliveryMode, priority, timeToLive);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public void send(Message message) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("send " + this + " message=" + message);
            }
            producer.send(message);
            if (trace) {
                log.trace("sent " + this + " result=" + message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return producer.getDeliveryMode();
    }

    @Override
    public Destination getDestination() throws JMSException {
        return producer.getDestination();
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return producer.getDisableMessageID();
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return producer.getDisableMessageTimestamp();
    }

    @Override
    public int getPriority() throws JMSException {
        return producer.getPriority();
    }

    @Override
    public long getTimeToLive() throws JMSException {
        return producer.getTimeToLive();
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        producer.setDeliveryMode(deliveryMode);
    }

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        producer.setDisableMessageID(value);
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        producer.setDisableMessageTimestamp(value);
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        producer.setPriority(defaultPriority);
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        producer.setTimeToLive(timeToLive);
    }

    // -- JMS 2.0 API


    @Override
    public void setDeliveryDelay(long deliveryDelay) throws JMSException {
        producer.setDeliveryDelay(deliveryDelay);
    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        return producer.getDeliveryDelay();
    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {
        producer.send(message, completionListener);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {
        producer.send(message, deliveryMode, priority, timeToLive, completionListener);
    }

    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {
        producer.send(destination, message, completionListener);
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {
        producer.send(destination, message, deliveryMode, priority, timeToLive, completionListener);
    }

    void closeProducer() throws JMSException {
        producer.close();
    }
}
