/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.QueueReceiver;

/**
 * A wrapper for a queue receiver
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsQueueReceiver extends JmsMessageConsumer implements QueueReceiver {
    /**
     * Create a new wrapper
     *
     * @param consumer the queue receiver
     * @param session  the session
     */
    public JmsQueueReceiver(QueueReceiver consumer, JmsSession session) {
        super(consumer, session);
    }

    @Override
    public Queue getQueue() throws JMSException {
        return ((QueueReceiver) consumer).getQueue();
    }
}
