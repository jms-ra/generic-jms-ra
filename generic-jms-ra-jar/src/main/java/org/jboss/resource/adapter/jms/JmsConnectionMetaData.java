/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.util.Enumeration;
import java.util.Vector;

import jakarta.jms.ConnectionMetaData;

/**
 * This class implements jakarta.jms.ConnectionMetaData
 *
 * @author Norbert Lataille (Norbert.Lataille@m4x.org)
 * @author Hiram Chirino (Norbert.Lataille@m4x.org)
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 */
public class JmsConnectionMetaData implements ConnectionMetaData {
    @Override
    public String getJMSVersion() {
        return "2.0";
    }

    @Override
    public int getJMSMajorVersion() {
        return 2;
    }

    @Override
    public int getJMSMinorVersion() {
        return 0;
    }

    @Override
    public String getJMSProviderName() {
        return "JBoss";
    }

    @Override
    public String getProviderVersion() {
        return "7.1";
    }

    @Override
    public int getProviderMajorVersion() {
        return 7;
    }

    @Override
    public int getProviderMinorVersion() {
        return 1;
    }

    @Override
    public Enumeration<String> getJMSXPropertyNames() {
        Vector<String> vector = new Vector<>();
        vector.add("JMSXGroupID");
        vector.add("JMSXGroupSeq");
        vector.add("JMSXDeliveryCount");
        return vector.elements();
    }
}
