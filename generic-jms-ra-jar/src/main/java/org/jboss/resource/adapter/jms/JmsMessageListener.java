/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;

/**
 * A wrapper for a message listener
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMessageListener implements MessageListener {
    /**
     * The message listener
     */
    MessageListener listener;

    /**
     * The consumer
     */
    JmsMessageConsumer consumer;

    /**
     * Create a new wrapper
     *
     * @param listener the listener
     * @param consumer the consumer
     */
    public JmsMessageListener(MessageListener listener, JmsMessageConsumer consumer) {
        this.listener = listener;
        this.consumer = consumer;
    }

    @Override
    public void onMessage(Message message) {
        listener.onMessage(consumer.wrapMessage(message));
    }
}
