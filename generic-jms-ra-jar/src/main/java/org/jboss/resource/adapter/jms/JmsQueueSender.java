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

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueSender;

import org.jboss.logging.Logger;

/**
 * JmsQueueSender.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 */
public class JmsQueueSender extends JmsMessageProducer implements QueueSender {
    private static final Logger log = Logger.getLogger(JmsQueueSender.class);

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
    public JmsQueueSender(QueueSender producer, JmsSession session) {
        super(producer, session);
    }

    @Override
    public Queue getQueue() throws JMSException {
        return ((QueueSender) producer).getQueue();
    }

    @Override
    public void send(Queue destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
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
    public void send(Queue destination, Message message) throws JMSException {
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
}
