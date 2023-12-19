/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;

/**
 * Jms Metadata
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMetaData implements ManagedConnectionMetaData {
    private final JmsManagedConnection mc;

    public JmsMetaData(final JmsManagedConnection mc) {
        this.mc = mc;
    }

    @Override
    public String getEISProductName() throws ResourceException {
        return "JBoss Generic JMS JCA Resource Adapter";
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        return "0.1";//Is this possible to get another way
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        // Dont know how to get this, from Jms, we
        // set it to unlimited
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException {
        return mc.getUserName();
    }
}
