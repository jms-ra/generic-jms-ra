/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.jms.StreamMessage;
import jakarta.jms.TextMessage;

import org.jboss.logging.Logger;

/**
 * A wrapper for a message consumer
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMessageConsumer implements MessageConsumer {

    private static final Logger log = Logger.getLogger(JmsMessageConsumer.class);

    /**
     * The wrapped message consumer
     */
    MessageConsumer consumer;

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
     * @param consumer the consumer
     * @param session the session
     */
    public JmsMessageConsumer(MessageConsumer consumer, JmsSession session) {
        this.consumer = consumer;
        this.session = session;

        if (trace) {
            log.trace("new JmsMessageConsumer " + this + " consumer=" + consumer + " session=" + session);
        }
    }

    @Override
    public void close() throws JMSException {
        if (trace) {
            log.trace("close " + this);
        }
        try {
            closeConsumer();
        } finally {
            session.removeConsumer(this);
        }
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        session.checkStrict();
        return consumer.getMessageListener();
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return consumer.getMessageSelector();
    }

    @Override
    public Message receive() throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("receive " + this);
            }
            Message message = consumer.receive();
            if (trace) {
                log.trace("received " + this + " result=" + message);
            }
            if (message == null) {
                return null;
            } else {
                return wrapMessage(message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("receive " + this + " timeout=" + timeout);
            }
            Message message = consumer.receive(timeout);
            if (trace) {
                log.trace("received " + this + " result=" + message);
            }
            if (message == null) {
                return null;
            } else {
                return wrapMessage(message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        session.lock();
        try {
            if (trace) {
                log.trace("receiveNoWait " + this);
            }
            Message message = consumer.receiveNoWait();
            if (trace) {
                log.trace("received " + this + " result=" + message);
            }
            if (message == null) {
                return null;
            } else {
                return wrapMessage(message);
            }
        } finally {
            session.unlock();
        }
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        session.lock();
        try {
            session.checkStrict();
            if (listener == null) {
                consumer.setMessageListener(null);
            } else {
                consumer.setMessageListener(wrapMessageListener(listener));
            }
        } finally {
            session.unlock();
        }
    }

    void closeConsumer() throws JMSException {
        consumer.close();
    }

    Message wrapMessage(Message message) {
        if (message instanceof BytesMessage) {
            return new JmsBytesMessage((BytesMessage) message, session);
        }
        if (message instanceof MapMessage) {
            return new JmsMapMessage((MapMessage) message, session);
        }
        if (message instanceof ObjectMessage) {
            return new JmsObjectMessage((ObjectMessage) message, session);
        }
        if (message instanceof StreamMessage) {
            return new JmsStreamMessage((StreamMessage) message, session);
        }
        if (message instanceof TextMessage) {
            return new JmsTextMessage((TextMessage) message, session);
        }
        return new JmsMessage(message, session);
    }

    MessageListener wrapMessageListener(MessageListener listener) {
        return new JmsMessageListener(listener, this);
    }
}
