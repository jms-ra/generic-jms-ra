/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.io.Serializable;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.TopicConnectionFactory;

/**
 * An aggregate interface for QueueConnectionFactory and
 * TopicConnectionFactory.  Also marks as serializable.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public interface JmsConnectionFactory extends ConnectionFactory, TopicConnectionFactory, QueueConnectionFactory, Serializable
{
   int AGNOSTIC = 0;
   int QUEUE = 1;
   int TOPIC = 2;
   int JMS_CONTEXT = 3;
}
