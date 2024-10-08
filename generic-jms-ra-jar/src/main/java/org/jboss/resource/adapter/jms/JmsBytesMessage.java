/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsBytesMessage extends JmsMessage implements BytesMessage {
    /**
     * Create a new wrapper
     *
     * @param message the message
     * @param session the session
     */
    public JmsBytesMessage(BytesMessage message, JmsSession session) {
        super(message, session);
    }

    @Override
    public long getBodyLength() throws JMSException {
        return ((BytesMessage) message).getBodyLength();
    }

    @Override
    public boolean readBoolean() throws JMSException {
        return ((BytesMessage) message).readBoolean();
    }

    @Override
    public byte readByte() throws JMSException {
        return ((BytesMessage) message).readByte();
    }

    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        return ((BytesMessage) message).readBytes(value, length);
    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        return ((BytesMessage) message).readBytes(value);
    }

    @Override
    public char readChar() throws JMSException {
        return ((BytesMessage) message).readChar();
    }

    @Override
    public double readDouble() throws JMSException {
        return ((BytesMessage) message).readDouble();
    }

    @Override
    public float readFloat() throws JMSException {
        return ((BytesMessage) message).readFloat();
    }

    @Override
    public int readInt() throws JMSException {
        return ((BytesMessage) message).readInt();
    }

    @Override
    public long readLong() throws JMSException {
        return ((BytesMessage) message).readLong();
    }

    @Override
    public short readShort() throws JMSException {
        return ((BytesMessage) message).readShort();
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        return ((BytesMessage) message).readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        return ((BytesMessage) message).readUnsignedShort();
    }

    @Override
    public String readUTF() throws JMSException {
        return ((BytesMessage) message).readUTF();
    }

    @Override
    public void reset() throws JMSException {
        ((BytesMessage) message).reset();
    }

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        ((BytesMessage) message).writeBoolean(value);
    }

    @Override
    public void writeByte(byte value) throws JMSException {
        ((BytesMessage) message).writeByte(value);
    }

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        ((BytesMessage) message).writeBytes(value, offset, length);
    }

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        ((BytesMessage) message).writeBytes(value);
    }

    @Override
    public void writeChar(char value) throws JMSException {
        ((BytesMessage) message).writeChar(value);
    }

    @Override
    public void writeDouble(double value) throws JMSException {
        ((BytesMessage) message).writeDouble(value);
    }

    @Override
    public void writeFloat(float value) throws JMSException {
        ((BytesMessage) message).writeFloat(value);
    }

    @Override
    public void writeInt(int value) throws JMSException {
        ((BytesMessage) message).writeInt(value);
    }

    @Override
    public void writeLong(long value) throws JMSException {
        ((BytesMessage) message).writeLong(value);
    }

    @Override
    public void writeObject(Object value) throws JMSException {
        ((BytesMessage) message).writeObject(value);
    }

    @Override
    public void writeShort(short value) throws JMSException {
        ((BytesMessage) message).writeShort(value);
    }

    @Override
    public void writeUTF(String value) throws JMSException {
        ((BytesMessage) message).writeUTF(value);
    }
}
