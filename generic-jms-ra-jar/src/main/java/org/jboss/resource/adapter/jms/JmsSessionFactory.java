/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.QueueConnection;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TopicConnection;

/**
 * A marker interface to join topics and queues into one factory.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public interface JmsSessionFactory extends Connection, TopicConnection, QueueConnection {
    /**
     * Error message for strict behaviour
     */
    String ISE = "This method is not applicable inside the application server. See the J2EE spec, e.g. J2EE1.4 Section 6.6";

    /**
     * Add a temporary queue
     *
     * @param temp the temporary queue
     */
    void addTemporaryQueue(TemporaryQueue temp);

    /**
     * Add a temporary topic
     *
     * @param temp the temporary topic
     */
    void addTemporaryTopic(TemporaryTopic temp);

    /**
     * Notification that a session is closed
     *
     * @throws JMSException for any error
     */
    void closeSession(JmsSession session) throws JMSException;
}
