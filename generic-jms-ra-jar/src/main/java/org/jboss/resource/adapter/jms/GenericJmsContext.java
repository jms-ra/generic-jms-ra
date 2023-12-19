/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.resource.adapter.jms;

import java.io.Serializable;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.util.JMSProducerUtils;
import org.jboss.resource.adapter.jms.util.TibcojmsUtils;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2016 Red Hat inc.
 */
public class GenericJmsContext implements JMSContext {

    private static final Logger log = Logger.getLogger(GenericJmsContext.class);
    private static final String ILLEGAL_METHOD = "This method is not applicable inside the application server. See the JEE spec, e.g. JEE 7 Section 6.7";

    private final JmsSessionFactory sessionFactory;
    private final JmsSession session;
    private final Set<JMSProducer> producers = Collections.synchronizedSet(new HashSet<JMSProducer>());

    GenericJmsContext(JmsSessionFactory sessionFactory, JmsSession session) {
        this.sessionFactory = sessionFactory;
        this.session = session;
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        throw new JMSRuntimeException(ILLEGAL_METHOD);
    }

    @Override
    public JMSProducer createProducer() {
        JMSProducer producer = session.getJMSContext().createProducer();
        producers.add(producer);
        return producer;
    }

    @Override
    public String getClientID() {
        return session.getJMSContext().getClientID();
    }

    @Override
    public void setClientID(String clientID) {
        throw new JMSRuntimeException(ILLEGAL_METHOD);
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return session.getJMSContext().getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return session.getJMSContext().getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        throw new JMSRuntimeException(ILLEGAL_METHOD);
    }

    @Override
    public void start() {
        session.getJMSContext().start();
    }

    @Override
    public void stop() {
        throw new JMSRuntimeException(ILLEGAL_METHOD);
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        session.getJMSContext().setAutoStart(autoStart);
    }

    @Override
    public boolean getAutoStart() {
        return session.getJMSContext().getAutoStart();
    }

    @Override
    public void close() {
        // #17 - close the session factory to return the managed connection to the pool
        try {
            synchronized (producers) {
                for (JMSProducer producer : producers) {
                    if (producer != null) {
                        try {
                            JMSProducerUtils.close(producer);
                        } catch (PrivilegedActionException ex) {
                            throw (JMSException) ex.getException();
                        }
                    }
                }
                producers.clear();
            }
            session.close();
            sessionFactory.close();
        } catch (JMSException e) {
            log.debugf(e, "Error closing the JMSContext");
        }
    }

    @Override
    public BytesMessage createBytesMessage() {
        return session.getJMSContext().createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        return session.getJMSContext().createMapMessage();
    }

    @Override
    public Message createMessage() {
        return session.getJMSContext().createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return session.getJMSContext().createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return session.getJMSContext().createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return session.getJMSContext().createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        return session.getJMSContext().createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return session.getJMSContext().createTextMessage(text);
    }

    @Override
    public boolean getTransacted() {
        return session.getJMSContext().getTransacted();
    }

    @Override
    public int getSessionMode() {
        return session.getJMSContext().getSessionMode();
    }

    @Override
    public void commit() {
        session.getJMSContext().commit();
    }

    @Override
    public void rollback() {
        session.getJMSContext().rollback();

    }

    @Override
    public void recover() {
        session.getJMSContext().recover();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return session.getJMSContext().createConsumer(destination);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return session.getJMSContext().createConsumer(destination, messageSelector);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return session.getJMSContext().createConsumer(destination, messageSelector, noLocal);
    }

    @Override
    public Queue createQueue(String queueName) {
        return session.getJMSContext().createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) {
        return session.getJMSContext().createTopic(topicName);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return session.getJMSContext().createDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return session.getJMSContext().createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return session.getJMSContext().createSharedDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return session.getJMSContext().createSharedDurableConsumer(topic, name, messageSelector);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return session.getJMSContext().createSharedConsumer(topic, sharedSubscriptionName);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return session.getJMSContext().createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return session.getJMSContext().createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return session.getJMSContext().createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return session.getJMSContext().createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return session.getJMSContext().createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) {
        session.getJMSContext().unsubscribe(name);
    }

    @Override
    public void acknowledge() {
        session.getJMSContext().acknowledge();
    }
}
