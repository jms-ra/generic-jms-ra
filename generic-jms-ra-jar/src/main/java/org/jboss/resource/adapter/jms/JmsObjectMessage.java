/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.io.Serializable;

import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsObjectMessage extends JmsMessage implements ObjectMessage {
    /**
     * Create a new wrapper
     *
     * @param message the message
     * @param session the session
     */
    public JmsObjectMessage(ObjectMessage message, JmsSession session) {
        super(message, session);
    }

    @Override
    public Serializable getObject() throws JMSException {
        return ((ObjectMessage) message).getObject();
    }

    @Override
    public void setObject(Serializable object) throws JMSException {
        ((ObjectMessage) message).setObject(object);
    }
}
