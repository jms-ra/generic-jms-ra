/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.resource.adapter.jms;

import java.io.Serializable;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionConsumer;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueSession;
import jakarta.jms.ServerSessionPool;
import jakarta.jms.Session;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicSession;
import jakarta.jms.TopicSubscriber;
import jakarta.jms.XAConnection;
import jakarta.jms.XAQueueConnection;
import jakarta.jms.XAQueueSession;
import jakarta.jms.XASession;
import jakarta.jms.XATopicConnection;
import jakarta.jms.XATopicSession;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.util.TransactionUtils;

public class JmsConnectionSession implements Connection, XAConnection, Session, XASession, QueueConnection,
        XAQueueConnection, TopicConnection, XATopicConnection, AutoCloseable {

    private static final Logger log = Logger.getLogger(JmsConnectionSession.class);

    private final Connection connection;
    private final Session session;
    private XAConnection xaConnection;
    private XASession xaSession;

    public JmsConnectionSession(Connection connection, Session session) {
        if (connection == null || session == null) {
            throw new ExceptionInInitializerError("JMS 1.1 Connection and Session both must not be null");
        }
        this.connection = connection;
        this.session = session;
        if (connection instanceof XAConnection) {
            this.xaConnection = (XAConnection) connection;
        }
        if (session instanceof XASession) {
            this.xaSession = (XASession) session;
        }
    }

    // XASession API
    @Override
    public Session getSession() throws JMSException {
        if (xaSession == null) {
            throw new JMSException("Not an XA compliant JMS session context");
        }
        return xaSession.getSession();
    }

    @Override
    public XAResource getXAResource() {
        if (xaSession == null) {
            throw new IllegalStateException("Not an XA compliant JMS session context");
        }
        return xaSession.getXAResource();
    }

    // XAConnection API
    @Override
    public XASession createXASession() throws JMSException {
        if (xaConnection != null) {
            return xaConnection.createXASession();
        }
        throw new JMSException("Not an XA compliant JMS session context");
    }

    // Session API
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        if (session == null) {
            throw new JMSException("No valid JMS session context");
        }
        return session.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        if (session == null) {
            throw new JMSException("No valid JMS session context");
        }
        return session.createMapMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        if (session != null) {
            return session.createMessage();
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        if (session == null) {
            throw new JMSException("No valid JMS session context");
        }
        return session.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        if (session == null) {
            throw new JMSException("No valid JMS session context");
        }
        return session.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        if (session != null) {
            return session.createStreamMessage();
        } else {
            throw new JMSException("No valid JMS session context");
        }

    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        if (session != null) {
            return session.createTextMessage();
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        if (session != null) {
            return session.createTextMessage(text);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public boolean getTransacted() throws JMSException {
        if (session != null) {
            return session.getTransacted();
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        if (session != null) {
            return session.getAcknowledgeMode();
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public void commit() throws JMSException {
        if (session != null) {
            session.commit();
        } else {
            throw new JMSException("No valid JMS session context");
        }
    }

    @Override
    public void rollback() throws JMSException {
        if (session != null) {
            session.rollback();
        } else {
            throw new JMSException("No valid JMS session context");
        }
    }

    @Override
    public void recover() throws JMSException {
        if (session != null) {
            session.recover();
        } else {
            throw new JMSException("No valid JMS session context");
        }
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        throw new JMSException("Must not be used in a Java EE application");
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        throw new JMSException("Must not be used in a Java EE application");
    }

    @Override
    public void run() {
        if (session != null) {
            session.run();
        } else {
            throw new IllegalStateException("No valid JMS session context");
        }
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        if (session != null) {
            return session.createProducer(destination);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        if (session != null) {
            return session.createConsumer(destination);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        if (session != null) {
            return session.createConsumer(destination, messageSelector);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal)
            throws JMSException {
        if (session != null) {
            return session.createConsumer(destination, messageSelector, noLocal);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
        if (session != null) {
            return session.createSharedConsumer(topic, sharedSubscriptionName);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector)
            throws JMSException {
        if (session != null) {
            return session.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        if (session != null) {
            return session.createQueue(queueName);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        if (session != null) {
            return session.createTopic(topicName);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        if (session != null) {
            return session.createDurableSubscriber(topic, name);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal)
            throws JMSException {
        if (session != null) {
            return session.createDurableSubscriber(topic, name, messageSelector, noLocal);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        if (session != null) {
            return session.createDurableConsumer(topic, name);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal)
            throws JMSException {
        if (session != null) {
            return session.createDurableConsumer(topic, name, messageSelector, noLocal);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        if (session != null) {
            return session.createSharedDurableConsumer(topic, name);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector)
            throws JMSException {
        if (session != null) {
            return session.createSharedDurableConsumer(topic, name, messageSelector);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        if (session != null) {
            return session.createBrowser(queue);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        if (session != null) {
            return session.createBrowser(queue, messageSelector);
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        if (session != null) {
            return session.createTemporaryQueue();
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        if (session != null) {
            return session.createTemporaryTopic();
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public void unsubscribe(String name) throws JMSException {
        if (session != null) {
            session.unsubscribe(name);
        } else {
            throw new JMSException("No valid JMS session context");
        }
    }

    // Connection API
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (session != null) {
            if ((transacted && connection instanceof XAConnection) || TransactionUtils.isInTransaction()) {
                log.debug("Connection is JTA transacted; setting acknowledgeMode to SESSION_TRANSACTED");
                return connection.createSession(true, Session.SESSION_TRANSACTED);
            }
            log.debug("Connection is NOT transacted; setting acknowledgeMode=" + acknowledgeMode);
            return connection.createSession(false, acknowledgeMode);
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public Session createSession(int sessionMode) throws JMSException {
        if (session != null) {
            if ((sessionMode == Session.SESSION_TRANSACTED && connection instanceof XAConnection) || TransactionUtils.isInTransaction()) {
                log.debug("Connection is JTA transacted; setting acknowledgeMode to SESSION_TRANSACTED");
                return connection.createSession(Session.SESSION_TRANSACTED);
            }
            int acknowledgeMode = -1;
            switch (sessionMode) {
                case Session.SESSION_TRANSACTED:
                    acknowledgeMode = Session.SESSION_TRANSACTED;
                    break;
                case Session.AUTO_ACKNOWLEDGE:
                    acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
                    break;
                case Session.DUPS_OK_ACKNOWLEDGE:
                    acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
                    break;
                case Session.CLIENT_ACKNOWLEDGE:
                    acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
                    break;
            }
            log.debug("Connection is NOT transacted; setting acknowledgeMode to" + getAckMode(sessionMode));
            return connection.createSession(false, acknowledgeMode);
        }
        throw new JMSException("No valid JMS connection session");
    }

    private String getAckMode(int ack) {
        switch (ack) {
                case Session.SESSION_TRANSACTED:
                    return "SESSION_TRANSACTED";
                case Session.AUTO_ACKNOWLEDGE:
                    return "AUTO_ACKNOWLEDGE";
                case Session.DUPS_OK_ACKNOWLEDGE:
                    return "DUPS_OK_ACKNOWLEDGE";
                case Session.CLIENT_ACKNOWLEDGE:
                    return "CLIENT_ACKNOWLEDGE";
            }
        return "UNKNOWN_ACKNOWLEDGE";
    }

    @Override
    public Session createSession() throws JMSException {
        if (session != null) {
            return connection.createSession();
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public String getClientID() throws JMSException {
        if (session != null) {
            return connection.getClientID();
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        if (session != null) {
            connection.setClientID(clientID);
        } else {
            throw new JMSException("No valid JMS connection session");
        }
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        if (session != null) {
            return connection.getMetaData();
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        if (session != null) {
            return connection.getExceptionListener();
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        if (session != null) {
            connection.setExceptionListener(listener);
        } else {
            throw new JMSException("No valid JMS connection session");
        }
    }

    @Override
    public void start() throws JMSException {
        if (session != null) {
            connection.start();
        } else {
            throw new JMSException("No valid JMS connection session");
        }
    }

    @Override
    public void stop() throws JMSException {
        if (session != null) {
            connection.stop();
        } else {
            throw new JMSException("No valid JMS connection session");
        }
    }

    @Override
    public void close() throws JMSException {
        if (session != null) {
            connection.close();
        } else {
            throw new JMSException("No valid JMS connection session");
        }
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (session != null) {
            return connection.createConnectionConsumer(destination, messageSelector, sessionPool, maxMessages);
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (session != null) {
            return connection.createSharedConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool,
                    maxMessages);
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (session != null) {
            return connection.createDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool,
                    maxMessages);
        }
        throw new JMSException("No valid JMS connection session");
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (session != null) {
            return connection.createSharedDurableConnectionConsumer(topic, subscriptionName, messageSelector,
                    sessionPool, maxMessages);
        }
        throw new JMSException("No valid JMS connection session");
    }

    // (XA)QueueConnectionAPI
    @Override
    public XAQueueSession createXAQueueSession() throws JMSException {
        if (xaConnection != null && xaConnection instanceof XAQueueConnection) {
            return ((XAQueueConnection) xaConnection).createXAQueueSession();
        }
        throw new JMSException("JMS connection context does not implement XAQueueConnection");
    }

    @Override
    public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (connection != null && connection instanceof QueueConnection) {
            return ((QueueConnection) connection).createQueueSession(transacted, acknowledgeMode);
        }
        throw new JMSException("JMS connection context does not implement QueueConnection");
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Queue queue, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (connection != null && connection instanceof QueueConnection) {
            return ((QueueConnection) connection).createConnectionConsumer(queue, messageSelector, sessionPool,
                    maxMessages);
        }
        throw new JMSException("JMS connection context does not implement QueueConnection");
    }

    // (XA)TopicConnection API
    @Override
    public XATopicSession createXATopicSession() throws JMSException {
        if (xaConnection != null && xaConnection instanceof XATopicConnection) {
            return ((XATopicConnection) xaConnection).createXATopicSession();
        }
        throw new JMSException("JMS connection context does not implement XATopicConnection");
    }

    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (connection != null && connection instanceof TopicConnection) {
            return ((TopicConnection) connection).createTopicSession(transacted, acknowledgeMode);
        }
        throw new JMSException("JMS connection context does not implement TopicConnection");
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Topic topic, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        if (connection != null && connection instanceof TopicConnection) {
            return ((TopicConnection) connection).createConnectionConsumer(topic, messageSelector, sessionPool,
                    maxMessages);
        }
        throw new JMSException("JMS connection context does not implement TopicConnection");
    }

    public static class JMSConsumerToTopicSubscriber implements TopicSubscriber {

        private final JMSConsumer jmsConsumer;

        private final Topic topic;

        public JMSConsumerToTopicSubscriber(JMSConsumer jmsConsumer, Topic topic) {
            this.jmsConsumer = jmsConsumer;
            this.topic = topic;
        }

        @Override
        public String getMessageSelector() throws JMSException {
            try {
                return jmsConsumer.getMessageSelector();
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }
        }

        @Override
        public MessageListener getMessageListener() throws JMSException {
            try {
                return jmsConsumer.getMessageListener();
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }
        }

        @Override
        public void setMessageListener(MessageListener listener) throws JMSException {
            try {
                jmsConsumer.setMessageListener(listener);
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }
        }

        @Override
        public Message receive() throws JMSException {
            try {
                return jmsConsumer.receive();
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }
        }

        @Override
        public Message receive(long timeout) throws JMSException {
            try {
                return jmsConsumer.receive(timeout);
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }

        }

        @Override
        public Message receiveNoWait() throws JMSException {
            try {
                return jmsConsumer.receiveNoWait();
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }
        }

        @Override
        public void close() throws JMSException {
            try {
                jmsConsumer.close();
            } catch (JMSRuntimeException jmsre) {
                throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
            }
        }

        @Override
        public Topic getTopic() throws JMSException {
            if (topic == null) {
                throw new JMSException("JMSConsumer was configured without a topic");
            }
            return topic;
        }

        @Override
        public boolean getNoLocal() throws JMSException {
            throw new JMSException("JMSConsumer does not support getNoLocal()");
        }

    }

}
