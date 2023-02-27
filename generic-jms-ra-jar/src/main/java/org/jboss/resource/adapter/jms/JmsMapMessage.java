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

import java.util.Enumeration;

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;

/**
 * A wrapper for a message
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMapMessage extends JmsMessage implements MapMessage {
    /**
     * Create a new wrapper
     *
     * @param message the message
     * @param session the session
     */
    public JmsMapMessage(MapMessage message, JmsSession session) {
        super(message, session);
    }

    @Override
    public boolean getBoolean(String name) throws JMSException {
        return ((MapMessage) message).getBoolean(name);
    }

    @Override
    public byte getByte(String name) throws JMSException {
        return ((MapMessage) message).getByte(name);
    }

    @Override
    public byte[] getBytes(String name) throws JMSException {
        return ((MapMessage) message).getBytes(name);
    }

    @Override
    public char getChar(String name) throws JMSException {
        return ((MapMessage) message).getChar(name);
    }

    @Override
    public double getDouble(String name) throws JMSException {
        return ((MapMessage) message).getDouble(name);
    }

    @Override
    public float getFloat(String name) throws JMSException {
        return ((MapMessage) message).getFloat(name);
    }

    @Override
    public int getInt(String name) throws JMSException {
        return ((MapMessage) message).getInt(name);
    }

    @Override
    public long getLong(String name) throws JMSException {
        return ((MapMessage) message).getLong(name);
    }

    @Override
    public Enumeration getMapNames() throws JMSException {
        return ((MapMessage) message).getMapNames();
    }

    @Override
    public Object getObject(String name) throws JMSException {
        return ((MapMessage) message).getObject(name);
    }

    @Override
    public short getShort(String name) throws JMSException {
        return ((MapMessage) message).getShort(name);
    }

    @Override
    public String getString(String name) throws JMSException {
        return ((MapMessage) message).getString(name);
    }

    @Override
    public boolean itemExists(String name) throws JMSException {
        return ((MapMessage) message).itemExists(name);
    }

    @Override
    public void setBoolean(String name, boolean value) throws JMSException {
        ((MapMessage) message).setBoolean(name, value);
    }

    @Override
    public void setByte(String name, byte value) throws JMSException {
        ((MapMessage) message).setByte(name, value);
    }

    @Override
    public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
        ((MapMessage) message).setBytes(name, value, offset, length);
    }

    @Override
    public void setBytes(String name, byte[] value) throws JMSException {
        ((MapMessage) message).setBytes(name, value);
    }

    @Override
    public void setChar(String name, char value) throws JMSException {
        ((MapMessage) message).setChar(name, value);
    }

    @Override
    public void setDouble(String name, double value) throws JMSException {
        ((MapMessage) message).setDouble(name, value);
    }

    @Override
    public void setFloat(String name, float value) throws JMSException {
        ((MapMessage) message).setFloat(name, value);
    }

    @Override
    public void setInt(String name, int value) throws JMSException {
        ((MapMessage) message).setInt(name, value);
    }

    @Override
    public void setLong(String name, long value) throws JMSException {
        ((MapMessage) message).setLong(name, value);
    }

    @Override
    public void setObject(String name, Object value) throws JMSException {
        ((MapMessage) message).setObject(name, value);
    }

    @Override
    public void setShort(String name, short value) throws JMSException {
        ((MapMessage) message).setShort(name, value);
    }

    @Override
    public void setString(String name, String value) throws JMSException {
        ((MapMessage) message).setString(name, value);
    }
}
