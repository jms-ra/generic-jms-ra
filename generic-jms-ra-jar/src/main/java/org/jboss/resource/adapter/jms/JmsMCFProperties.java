/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import jakarta.resource.ResourceException;

import org.jboss.resource.adapter.jms.util.Strings;

/**
 * The MCF default properties, settable in ra.xml or in deployer.
 *
 * @author Peter Antman
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMCFProperties implements java.io.Serializable {
    static final long serialVersionUID = -7997396849692340121L;

    public static final String QUEUE_TYPE = Queue.class.getName();
    public static final String TOPIC_TYPE = Topic.class.getName();
    public static final String AGNOSTIC_TYPE = Destination.class.getName();
    public static final String JMS_CONTEXT_TYPE = JMSContext.class.getName();

    String userName;
    char[] password;
    String clientID;
    String jndiParameters;
    String connectionFactory;
    int type = JmsConnectionFactory.AGNOSTIC;

    public JmsMCFProperties() {
        // empty
    }

    /**
     * Set userName, null by default.
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * Get userName, may be null.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set password, null by default.
     * @param password
     */
    public void setPassword(final String password) {
        this.password = Strings.toCharArray(password);
    }

    /**
     * Get password, may be null.
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * Get client id, may be null.
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Set client id, null by default.
     */
    public void setClientID(final String clientID) {
        this.clientID = clientID;
    }

    /**
     * Type of the JMS Session.
     */
    public int getType() {
        return type;
    }

    /**
     * Set the default session type.
     */
    public void setType(int type) {
        this.type = type;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getJndiParameters() {
        return jndiParameters;
    }

    public void setJndiParameters(String jndiParameters) {
        this.jndiParameters = jndiParameters;
    }

    /**
     * Helper method to set the default session type.
     *
     * @param type either jakarta.jms.Topic or jakarta.jms.Queue
     * @throws ResourceException if type was not a valid type.
     */
    public void setSessionDefaultType(String type) throws ResourceException {
        if (QUEUE_TYPE.equals(type)) {
            this.type = JmsConnectionFactory.QUEUE;
        } else if (TOPIC_TYPE.equals(type)) {
            this.type = JmsConnectionFactory.TOPIC;
        } else if (JMS_CONTEXT_TYPE.equals(type)) {
            this.type = JmsConnectionFactory.JMS_CONTEXT;
        } else {
            this.type = JmsConnectionFactory.AGNOSTIC;
        }
    }

    public String getSessionDefaultType() {
        switch (type) {
            case JmsConnectionFactory.QUEUE:
                return QUEUE_TYPE;
            case JmsConnectionFactory.TOPIC:
                return TOPIC_TYPE;
            case JmsConnectionFactory.JMS_CONTEXT:
                return JMS_CONTEXT_TYPE;
            default:
                return AGNOSTIC_TYPE;
        }
    }

    /**
     * Test for equality.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof JmsMCFProperties) {
            JmsMCFProperties you = (JmsMCFProperties) obj;
            return (Strings.compare(userName, you.getUserName()) &&
                    Strings.compare(password, you.getPassword()) &&
                    this.type == you.type);
        }

        return false;
    }

    /**
     * Simple hashCode of all attributes.
     */
    @Override
    public int hashCode() {
        // FIXME
        String result = "" + userName + password + type;
        return result.hashCode();
    }
}
