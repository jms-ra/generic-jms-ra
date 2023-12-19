/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSException;
import jakarta.jms.StreamMessage;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsStreamMessage extends JmsMessage implements StreamMessage {
    /**
     * Create a new wrapper
     *
     * @param message the message
     * @param session the session
     */
    public JmsStreamMessage(StreamMessage message, JmsSession session) {
        super(message, session);
    }

    public boolean readBoolean() throws JMSException {
        return ((StreamMessage) message).readBoolean();
    }

    public byte readByte() throws JMSException {
        return ((StreamMessage) message).readByte();
    }

    public int readBytes(byte[] value) throws JMSException {
        return ((StreamMessage) message).readBytes(value);
    }

    public char readChar() throws JMSException {
        return ((StreamMessage) message).readChar();
    }

    public double readDouble() throws JMSException {
        return ((StreamMessage) message).readDouble();
    }

    public float readFloat() throws JMSException {
        return ((StreamMessage) message).readFloat();
    }

    public int readInt() throws JMSException {
        return ((StreamMessage) message).readInt();
    }

    public long readLong() throws JMSException {
        return ((StreamMessage) message).readLong();
    }

    public Object readObject() throws JMSException {
        return ((StreamMessage) message).readObject();
    }

    public short readShort() throws JMSException {
        return ((StreamMessage) message).readShort();
    }

    public String readString() throws JMSException {
        return ((StreamMessage) message).readString();
    }

    public void reset() throws JMSException {
        ((StreamMessage) message).reset();
    }

    public void writeBoolean(boolean value) throws JMSException {
        ((StreamMessage) message).writeBoolean(value);
    }

    public void writeByte(byte value) throws JMSException {
        ((StreamMessage) message).writeByte(value);
    }

    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        ((StreamMessage) message).writeBytes(value, offset, length);
    }

    public void writeBytes(byte[] value) throws JMSException {
        ((StreamMessage) message).writeBytes(value);
    }

    public void writeChar(char value) throws JMSException {
        ((StreamMessage) message).writeChar(value);
    }

    public void writeDouble(double value) throws JMSException {
        ((StreamMessage) message).writeDouble(value);
    }

    public void writeFloat(float value) throws JMSException {
        ((StreamMessage) message).writeFloat(value);
    }

    public void writeInt(int value) throws JMSException {
        ((StreamMessage) message).writeInt(value);
    }

    public void writeLong(long value) throws JMSException {
        ((StreamMessage) message).writeLong(value);
    }

    public void writeObject(Object value) throws JMSException {
        ((StreamMessage) message).writeObject(value);
    }

    public void writeShort(short value) throws JMSException {
        ((StreamMessage) message).writeShort(value);
    }

    public void writeString(String value) throws JMSException {
        ((StreamMessage) message).writeString(value);
    }
}
