/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
