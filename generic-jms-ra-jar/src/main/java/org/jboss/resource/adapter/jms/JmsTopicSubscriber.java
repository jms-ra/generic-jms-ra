/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Topic;
import jakarta.jms.TopicSubscriber;

/**
 * A wrapper for a topic subscriber
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsTopicSubscriber extends JmsMessageConsumer implements TopicSubscriber {
    /**
     * Create a new wrapper
     *
     * @param consumer the topic subscriber
     * @param session  the session
     */
    public JmsTopicSubscriber(TopicSubscriber consumer, JmsSession session) {
        super(consumer, session);
    }

    @Override
    public boolean getNoLocal() throws JMSException {
        return ((TopicSubscriber) consumer).getNoLocal();
    }

    @Override
    public Topic getTopic() throws JMSException {
        return ((TopicSubscriber) consumer).getTopic();
    }
}
