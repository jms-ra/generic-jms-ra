/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.jms.demo.qpid.mdb.remote;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

/**
 * 
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
@MessageDriven(name = "RemoteQueueMDB", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "${remote.in.queue.fqn}"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "${qpid.cf}"),
    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "${max.session}"),
    @ActivationConfigProperty(propertyName = "user", propertyValue = "admin"),
    @ActivationConfigProperty(propertyName = "password", propertyValue = "admin")
}, mappedName = "java:/jms/qpid/queue/testQueue")
@ResourceAdapter("${ra.bind.name}")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RemoteQueueMDB implements MessageListener {

    private static final String MESSAGE_CONSUMER_DELAY = "ConsumerDelay";
    private final static Logger LOG = Logger.getLogger(RemoteQueueMDB.class);
    private static final AtomicInteger mdbCnt = new AtomicInteger(0);
    private int msgCnt = 0;
    private int mdbID = 0;
    private TextMessage txtMsg = null;

    @Resource(name = "${remote.out.queue.fqn}")
    private Queue outQueue;

    @Resource(lookup = "${qpid.cf}")
    private QueueConnectionFactory qcf;

    public RemoteQueueMDB() {
        String className = this.getClass().getName();
        if (className.equals("org.acme.jms.mdb.remote.RemoteQueueMDB")) {
            mdbID = mdbCnt.getAndIncrement();
        }
        LOG.infof("MDB[%d] MDB class %s created", mdbID, className);

    }

    /**
     * @see jakarta.jms.MessageListener#onMessage(jakarta.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        try (QueueConnection queueConnection = qcf.createQueueConnection("admin", "admin");
                QueueSession queueSession = queueConnection.createQueueSession(true, Session.SESSION_TRANSACTED);
                QueueSender queueSender = queueSession.createSender(outQueue)) {
            if (message instanceof TextMessage) {
                txtMsg = (TextMessage) message;
                if (LOG.isDebugEnabled()) {
                    LOG.debugf("MDB[%d] Received Message[%s]: with text '%s'.", mdbID, txtMsg.toString(), txtMsg.getText());
                }
                msgCnt++;
                queueSender.send(message);
                msgCnt++;
                queueSession.commit();
            } else {
                LOG.warnf("MDB[%d] Message of wrong type: %s", mdbID, message.getClass().getName());
            }
        } catch (JMSException jmsException) {
            LOG.errorf(jmsException, "MDB[%d] Got error while excuting onMessage() method.", mdbID);
            throw new RuntimeException(jmsException);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debugf("MDB[%d] JMS resources closed.", mdbID);
        }

    }

    @PreDestroy
    public void printStats() {
        LOG.infof("MDB[%d] Processed %d messages.", mdbID, msgCnt);
        LOG.infof("MDB[%d] Closing.", mdbID);
        if (LOG.isDebugEnabled()) {
            LOG.debugf("MDB[%d] MDB count is ", mdbID, mdbCnt.get());
        }
        mdbCnt.decrementAndGet();
    }

    @PostConstruct
    public void init() {
        LOG.infof("MDB[%d] created.", mdbID);
    }

}
