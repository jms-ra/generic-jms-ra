/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsTextMessage extends JmsMessage implements TextMessage {
    /**
     * Create a new wrapper
     *
     * @param message the message
     * @param session the session
     */
    public JmsTextMessage(TextMessage message, JmsSession session) {
        super(message, session);
    }

    @Override
    public String getText() throws JMSException {
        return ((TextMessage) message).getText();
    }

    @Override
    public void setText(String string) throws JMSException {
        ((TextMessage) message).setText(string);
    }
}
