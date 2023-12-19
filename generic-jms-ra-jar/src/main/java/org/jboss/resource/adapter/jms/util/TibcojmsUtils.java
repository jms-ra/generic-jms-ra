/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import jakarta.jms.JMSException;
import org.jboss.logging.Logger;

/**
 * Tibco specific code inoked to close JMSproducers properly.
 *
 * @author Emmanuel Hugonnet (c) 2020 Red Hat, Inc.
 */
public class TibcojmsUtils {

    private static final Logger log = Logger.getLogger(TibcojmsUtils.class);

    public static final String JMS_PRODUCER_CLASSNAME = "com.tibco.tibjms.TibjmsJMSProducer";
    public static final String JMS_CONTEXT_CLASSNAME = "com.tibco.tibjms.TibjmsJMSContext";
    public static final String JMS_XACONTEXT_CLASSNAME = "com.tibco.tibjms.TibjmsXAJMSContext";
    public static final String JMS_MESSAGE_PRODUCER = "com.tibco.tibjms.TibjmsMessageProducer";

    private static Class TIBCO_JMS_PRODUCER_CLASS;
    private static Class TIBCO_JMS_MESSAGE_PRODUCER_CLASS;
    private static Method TIBCO_JMS_MESSAGE_PRODUCER_CLOSE_METHOD;
    private static Field TIBCO_JMS_PRODUCER_PRODUCER_FIELD;
    private static Class TIBCO_JMS_CONTEXT_CLASS;
    private static Field TIBCO_JMS_CONTEXT_FIELD;
    private static Field TIBCO_JMS_CONTEXT_LOCK_FIELD;
    private static Field TIBCO_JMS_CONTEXT_PRODUCERS_FIELD;

    static {
        try {
            TIBCO_JMS_PRODUCER_CLASS = SecurityActions.getThreadContextClassLoader().loadClass(JMS_PRODUCER_CLASSNAME);
            TIBCO_JMS_CONTEXT_CLASS = SecurityActions.getThreadContextClassLoader().loadClass(JMS_CONTEXT_CLASSNAME);
            TIBCO_JMS_MESSAGE_PRODUCER_CLASS = SecurityActions.getThreadContextClassLoader().loadClass(JMS_MESSAGE_PRODUCER);
            TIBCO_JMS_MESSAGE_PRODUCER_CLOSE_METHOD = SecurityActions.getMethod(TIBCO_JMS_MESSAGE_PRODUCER_CLASS, "close");
            TIBCO_JMS_PRODUCER_PRODUCER_FIELD = SecurityActions.getDeclaredField(TIBCO_JMS_PRODUCER_CLASS, "_producer");
            TIBCO_JMS_CONTEXT_FIELD = SecurityActions.getDeclaredField(TIBCO_JMS_PRODUCER_CLASS, "_context");
            TIBCO_JMS_CONTEXT_LOCK_FIELD = SecurityActions.getDeclaredField(TIBCO_JMS_CONTEXT_CLASS, "_lock");
            TIBCO_JMS_CONTEXT_PRODUCERS_FIELD = SecurityActions.getDeclaredField(TIBCO_JMS_CONTEXT_CLASS, "_producers");
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | SecurityException ex) {
            log.debugf("Not a Tibco JMSProducers", ex);
            TIBCO_JMS_PRODUCER_CLASS = null;
            TIBCO_JMS_MESSAGE_PRODUCER_CLASS = null;
            TIBCO_JMS_MESSAGE_PRODUCER_CLOSE_METHOD = null;
            TIBCO_JMS_PRODUCER_PRODUCER_FIELD = null;
            TIBCO_JMS_CONTEXT_CLASS = null;
            TIBCO_JMS_CONTEXT_FIELD = null;
            TIBCO_JMS_CONTEXT_LOCK_FIELD = null;
            TIBCO_JMS_CONTEXT_PRODUCERS_FIELD = null;
        }
    }

    static void closeProducer(final Object jmsProducer) throws JMSException {
        try {
            Object messageProducer = TIBCO_JMS_PRODUCER_PRODUCER_FIELD.get(jmsProducer);
            Object jmsContext = TIBCO_JMS_CONTEXT_FIELD.get(jmsProducer);
            Object lock = TIBCO_JMS_CONTEXT_LOCK_FIELD.get(jmsContext);
            List<?> producers = (List<?>) TIBCO_JMS_CONTEXT_PRODUCERS_FIELD.get(jmsContext);
            try {
                TIBCO_JMS_MESSAGE_PRODUCER_CLOSE_METHOD.invoke(messageProducer);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                JMSException jmsex = new JMSException(ex.getMessage());
                jmsex.setLinkedException(ex);
                throw jmsex;
            }
            synchronized (lock) {
                log.debugf("before  - producers.size=%d", producers.size());
                producers.remove(jmsProducer);
                log.debugf("after - producers.size=%d", producers.size());
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            log.infof("Error closing Tibco JMSProducers", ex);
            //Do Nothing
        }
    }
}
