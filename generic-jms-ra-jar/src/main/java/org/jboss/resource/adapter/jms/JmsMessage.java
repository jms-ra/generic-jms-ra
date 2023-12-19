/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.util.Enumeration;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMessage implements Message {
    /**
     * The message
     */
    Message message;

    /**
     * The session
     */
    JmsSession session;

    /**
     * Create a new wrapper
     *
     * @param message the message
     * @param session the session
     */
    public JmsMessage(Message message, JmsSession session) {
        this.message = message;
        this.session = session;
    }

    @Override
    public void acknowledge() throws JMSException {
        session.getSession(); // Check for closed
        message.acknowledge();
    }

    @Override
    public void clearBody() throws JMSException {
        message.clearBody();
    }

    @Override
    public void clearProperties() throws JMSException {
        message.clearProperties();
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        return message.getBooleanProperty(name);
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        return message.getByteProperty(name);
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        return message.getDoubleProperty(name);
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        return message.getFloatProperty(name);
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        return message.getIntProperty(name);
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return message.getJMSCorrelationID();
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        return message.getJMSCorrelationIDAsBytes();
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return message.getJMSDeliveryMode();
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return message.getJMSDestination();
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return message.getJMSExpiration();
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return message.getJMSMessageID();
    }

    @Override
    public int getJMSPriority() throws JMSException {
        return message.getJMSPriority();
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return message.getJMSRedelivered();
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return message.getJMSReplyTo();
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return message.getJMSTimestamp();
    }

    @Override
    public String getJMSType() throws JMSException {
        return message.getJMSType();
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        return message.getLongProperty(name);
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        return message.getObjectProperty(name);
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        return message.getPropertyNames();
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        return message.getShortProperty(name);
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        return message.getStringProperty(name);
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        return message.propertyExists(name);
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        message.setBooleanProperty(name, value);
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        message.setByteProperty(name, value);
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        message.setDoubleProperty(name, value);
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        message.setFloatProperty(name, value);
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        message.setIntProperty(name, value);
    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        message.setJMSCorrelationID(correlationID);
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        message.setJMSCorrelationIDAsBytes(correlationID);
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        message.setJMSDeliveryMode(deliveryMode);
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        message.setJMSDestination(destination);
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        message.setJMSExpiration(expiration);
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        message.setJMSMessageID(id);
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        message.setJMSPriority(priority);
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        message.setJMSRedelivered(redelivered);
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        message.setJMSReplyTo(replyTo);
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        message.setJMSTimestamp(timestamp);
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        message.setJMSType(type);
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        message.setLongProperty(name, value);
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        message.setObjectProperty(name, value);
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        message.setShortProperty(name, value);
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        message.setStringProperty(name, value);
    }

    // -- JMS 2.0 API


    @Override
    public long getJMSDeliveryTime() throws JMSException {
        return message.getJMSDeliveryTime();
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
        message.setJMSDeliveryTime(deliveryTime);
    }

    @Override
    public <T> T getBody(Class<T> c) throws JMSException {
        return message.getBody(c);
    }

    @Override
    public boolean isBodyAssignableTo(Class c) throws JMSException {
        return message.isBodyAssignableTo(c);
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof JmsMessage) {
            return message.equals(((JmsMessage) object).message);
        } else {
            return message.equals(object);
        }
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
