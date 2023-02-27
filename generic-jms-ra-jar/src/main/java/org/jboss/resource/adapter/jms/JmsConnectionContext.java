package org.jboss.resource.adapter.jms;

import java.io.Serializable;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionConsumer;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSContext;
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
import jakarta.jms.XAJMSContext;
import jakarta.jms.XAQueueConnection;
import jakarta.jms.XAQueueSession;
import jakarta.jms.XASession;
import jakarta.jms.XATopicConnection;
import jakarta.jms.XATopicSession;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.JmsConnectionSession.JMSConsumerToTopicSubscriber;

public class JmsConnectionContext implements Connection, XAConnection, Session, XASession, QueueConnection,
        XAQueueConnection, TopicConnection, XATopicConnection, AutoCloseable {

    private static final Logger log = Logger.getLogger(JmsConnectionContext.class);

    private final JMSContext jmsContext;
    private final XAJMSContext xaJmsContext;

    public JmsConnectionContext(JMSContext jmsContext) {
        if (jmsContext == null) {
            throw new ExceptionInInitializerError("JMS 2.0 JMSContext must not be null");
        }
        this.jmsContext = jmsContext;
        if (jmsContext instanceof XAJMSContext) {
            xaJmsContext = (XAJMSContext) jmsContext;
        } else {
            xaJmsContext = null;
        }
    }

    // XASession API
    @Override
    public Session getSession() throws JMSException {
        if (xaJmsContext != null) {
            return new JmsConnectionContext(xaJmsContext.getContext());
        }
        throw new JMSException("Not an XA compliant JMS session context");
    }

    @Override
    public XAResource getXAResource() {
        if (xaJmsContext != null) {
            return xaJmsContext.getXAResource();
        }
        throw new IllegalStateException("Not an XA compliant JMS session context");
    }

    // XAConnection API
    @Override
    public XASession createXASession() throws JMSException {
        if (xaJmsContext != null) {
            return this;
        }
        throw new JMSException("Not an XA compliant JMS session context");
    }

    // Session API
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        try {
            return jmsContext.createBytesMessage();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        try {
            return jmsContext.createMapMessage();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public Message createMessage() throws JMSException {
        try {
            return jmsContext.createMessage();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        try {
            return jmsContext.createObjectMessage();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        try {
            return jmsContext.createObjectMessage(object);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        try {
            return jmsContext.createStreamMessage();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        try {
            return jmsContext.createTextMessage();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        try {
            return jmsContext.createTextMessage(text);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public boolean getTransacted() throws JMSException {
        try {
            return jmsContext.getTransacted();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        try {
            int sessionMode = jmsContext.getSessionMode();
            switch (sessionMode) {
                case JMSContext.AUTO_ACKNOWLEDGE:
                    return Session.AUTO_ACKNOWLEDGE;
                case JMSContext.CLIENT_ACKNOWLEDGE:
                    return Session.CLIENT_ACKNOWLEDGE;
                case JMSContext.DUPS_OK_ACKNOWLEDGE:
                    return Session.DUPS_OK_ACKNOWLEDGE;
                case JMSContext.SESSION_TRANSACTED:
                    return Session.SESSION_TRANSACTED;
            }
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
        throw new JMSException("No valid JMS session context");
    }

    @Override
    public void commit() throws JMSException {
        try {
            jmsContext.commit();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void rollback() throws JMSException {
        try {
            jmsContext.rollback();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void recover() throws JMSException {
        try {
            jmsContext.recover();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
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
        throw new UnsupportedOperationException("JMSContext does not support run()");
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        try {
            return new JMSProducerToMessageProducer(jmsContext.createProducer(), destination);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(jmsContext.createConsumer(destination));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(jmsContext.createConsumer(destination, messageSelector));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal)
            throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(
                    jmsContext.createConsumer(destination, messageSelector, noLocal));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(
                    jmsContext.createSharedConsumer(topic, sharedSubscriptionName));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector)
            throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(
                    jmsContext.createSharedConsumer(topic, sharedSubscriptionName, messageSelector));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        try {
            return jmsContext.createQueue(queueName);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        try {
            return jmsContext.createTopic(topicName);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        try {
            return new JMSConsumerToTopicSubscriber(jmsContext.createDurableConsumer(topic, name), topic);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal)
            throws JMSException {
        try {
            return new JMSConsumerToTopicSubscriber(
                    jmsContext.createDurableConsumer(topic, name, messageSelector, noLocal), topic);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(jmsContext.createDurableConsumer(topic, name));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal)
            throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(
                    jmsContext.createDurableConsumer(topic, name, messageSelector, noLocal));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(jmsContext.createSharedDurableConsumer(topic, name));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector)
            throws JMSException {
        try {
            return new JMSConsumerToMessageConsumer(
                    jmsContext.createSharedDurableConsumer(topic, name, messageSelector));
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        try {
            return jmsContext.createBrowser(queue);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        try {
            return jmsContext.createBrowser(queue, messageSelector);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        try {
            return jmsContext.createTemporaryQueue();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        try {
            return jmsContext.createTemporaryTopic();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void unsubscribe(String name) throws JMSException {
        try {
            jmsContext.unsubscribe(name);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

// Connection AP
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        try {
            log.debug(
                    "Ignoring parameters to createSession(boolean, int); JMSContext has been created with sessionMode="
                    + jmsContext.getSessionMode());
            return this;
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public Session createSession(int sessionMode) throws JMSException {
        try {
            log.debug(
                    "Ignoring parameters to createSession(boolean, int); JMSContext has been created with sessionMode="
                    + jmsContext.getSessionMode());
            return this;
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public Session createSession() throws JMSException {
        return this;
    }

    @Override
    public String getClientID() throws JMSException {
        try {
            return jmsContext.getClientID();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        try {
            jmsContext.setClientID(clientID);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        try {
            return jmsContext.getMetaData();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        try {
            return jmsContext.getExceptionListener();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        try {
            jmsContext.setExceptionListener(listener);
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void start() throws JMSException {
        try {
            jmsContext.start();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void stop() throws JMSException {
        try {
            jmsContext.stop();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public void close() throws JMSException {
        try {
            jmsContext.close();
        } catch (JMSRuntimeException jmsre) {
            throw new JMSException(jmsre.getLocalizedMessage(), jmsre.getErrorCode());
        }
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new JMSException("JMSContext does not support ConnectionConsumers");
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new JMSException("JMSContext does not support ConnectionConsumers");
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new JMSException("JMSContext does not support ConnectionConsumers");
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName,
            String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new JMSException("JMSContext does not support ConnectionConsumers");
    }

    // (XA)QueueConnectionAPI
    @Override
    public XAQueueSession createXAQueueSession() throws JMSException {
        throw new JMSException("JMS connection context does not implement XAQueueConnection");
    }

    @Override
    public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
        throw new JMSException("JMS connection context does not implement QueueConnection");
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Queue queue, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new JMSException("JMS connection context does not implement QueueConnection");
    }

    // (XA)TopicConnection API
    @Override
    public XATopicSession createXATopicSession() throws JMSException {
        throw new JMSException("JMS connection context does not implement XATopicConnection");
    }

    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        throw new JMSException("JMS connection context does not implement TopicConnection");
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Topic topic, String messageSelector,
            ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new JMSException("JMS connection context does not implement TopicConnection");
    }
}
