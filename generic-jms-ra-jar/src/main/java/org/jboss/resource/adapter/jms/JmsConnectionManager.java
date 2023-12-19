/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;

import org.jboss.logging.Logger;

/**
 * The resource adapters own ConnectionManager, used in non-managed
 * environments.
 *
 * Will handle some of the houskeeping an appserver nomaly does.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsConnectionManager implements ConnectionManager {
    private static final long serialVersionUID = -3638293323045716739L;

    private static final Logger log = Logger.getLogger(JmsConnectionManager.class);

    /**
     * Construct a <tt>JmsConnectionManager</tt>.
     */
    public JmsConnectionManager() {
        super();
    }

    /**
     * Allocate a new connection.
     *
     * @param mcf
     * @param cxRequestInfo
     * @return A new connection
     * @throws ResourceException Failed to create connection.
     */
    @Override
    public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        boolean trace = log.isTraceEnabled();
        if (trace) {
            log.trace("Allocating connection; mcf=" + mcf + ", cxRequestInfo=" + cxRequestInfo);
        }

        ManagedConnection mc = mcf.createManagedConnection(null, cxRequestInfo);
        Object c = mc.getConnection(null, cxRequestInfo);

        if (trace) {
            log.trace("Allocated connection: " + c + ", with managed connection: " + mc);
        }

        return c;
    }
}
