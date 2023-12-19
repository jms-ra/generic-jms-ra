/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;

/**
 * Mapper class for JMSProducer and JMSConsumer to the JMS 1.1 equivalent. 
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class JMSConsumerToMessageConsumer implements MessageConsumer {
    
    private final JMSConsumer jmsConsumer;

    public JMSConsumerToMessageConsumer(JMSConsumer jmsConsumer) {
        this.jmsConsumer = jmsConsumer;
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
    
}
