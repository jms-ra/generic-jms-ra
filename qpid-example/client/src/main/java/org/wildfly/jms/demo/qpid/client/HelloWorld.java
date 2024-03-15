/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wildfly.jms.demo.qpid.client;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class HelloWorld {

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Starting HelloWorld Client");
            // The configuration for the Qpid InitialContextFactory has been supplied in
            // a jndi.properties file in the classpath, which results in it being picked
            // up automatically by the InitialContext constructor.           
            Context context = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
            Destination queue = (Destination) context.lookup("myQueueLookup");
            Destination outQueue = (Destination) context.lookup("myOutQueueLookup");
            try (Connection connection = factory.createConnection("admin", "admin")) {
                connection.setExceptionListener(new MyExceptionListener());
                connection.start();
                System.out.println("Connection to QPID done");
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                MessageProducer messageProducer = session.createProducer(queue);
                MessageConsumer messageConsumer = session.createConsumer(outQueue);

                TextMessage message = session.createTextMessage("Hello world!");
                messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
                TextMessage receivedMessage = (TextMessage) messageConsumer.receive(60000L);
                if (receivedMessage != null) {
                    System.out.println("Message received " + receivedMessage.getJMSMessageID() + " with text " + receivedMessage.getText());
                } else {
                    System.out.println("No message received within the given timeout!!!!!");
                    System.exit(1);
                }
            }
        } catch (JMSException | NamingException exp) {
            System.out.println("Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static class MyExceptionListener implements ExceptionListener {

        @Override
        public void onException(JMSException exception) {
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
