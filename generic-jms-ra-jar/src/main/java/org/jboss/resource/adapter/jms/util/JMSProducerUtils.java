/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.resource.adapter.jms.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import jakarta.jms.JMSException;
import static org.jboss.resource.adapter.jms.util.TibcojmsUtils.JMS_PRODUCER_CLASSNAME;
import static org.jboss.resource.adapter.jms.util.TibcojmsUtils.closeProducer;

/**
 * JMSProducer utility class.
 * @author Emmanuel Hugonnet (c) 2020 Red Hat, Inc.
 */
public class JMSProducerUtils {
    
    public static void close(final Object jmsProducer) throws PrivilegedActionException, JMSException {
        Class jmsProducerClass = jmsProducer.getClass();
        if (JMS_PRODUCER_CLASSNAME.equals(jmsProducerClass.getName())) {
            if (System.getSecurityManager() == null) {
                closeProducer(jmsProducer);
            } else {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws JMSException {
                        closeProducer(jmsProducer);
                        return null;
                    }
                });
            }
        }
    }
}
