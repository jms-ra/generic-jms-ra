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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import jakarta.jms.ConnectionMetaData;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.util.Strings;

/**
 * Jms ManagedConectionFactory
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsManagedConnectionFactory implements ManagedConnectionFactory {
    private static final long serialVersionUID = -923483284031773011L;

    private static final Logger log = Logger.getLogger(JmsManagedConnection.class);

    /**
     * Settable attributes in ra.xml
     */
    private final JmsMCFProperties mcfProperties = new JmsMCFProperties();

    /**
     * Whether we are strict
     */
    private Boolean strict = true;

    /**
     * The try lock
     */
    private Integer useTryLock = 60;

    /**
     * Whether temporary destinations are deleted when a session is closed.
     */
    private Boolean deleteTemporaryDestinations = true;

    /**
     * Whether we are supporting JMS 2.0 in agnostic mode.
     */
    private Boolean jms_2_0 = true;

    public JmsManagedConnectionFactory() {
        // empty
    }

    /**
     * Create a "non managed" connection factory.No appserver involved
     * @throws jakarta.resource.ResourceException
     */
    @Override
    public Object createConnectionFactory() throws ResourceException {
        return createConnectionFactory(null);
    }

    /**
     * Create a ConnectionFactory with appserver hook
     * @throws jakarta.resource.ResourceException
     */
    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        Object cf = new JmsConnectionFactoryImpl(this, cxManager);

        if (log.isTraceEnabled()) {
            log.trace("Created connection factory: " + cf + ", using connection manager: " + cxManager);
        }

        return cf;
    }

    /**
     * Create a new connection to manage in pool
     * @param info
     * @throws jakarta.resource.ResourceException
     */
    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info) throws ResourceException {
        boolean trace = log.isTraceEnabled();

        info = getInfo(info);
        if (trace) {
            log.trace("connection request info: " + info);
        }

        JmsCred cred = JmsCred.getJmsCred(this, subject, info);
        if (trace) {
            log.trace("jms credentials: " + cred);
        }

        // OK we got autentication stuff
        JmsManagedConnection mc = new JmsManagedConnection(this, info, cred.name, Strings.fromCharArray(cred.pwd));

        if (trace) {
            log.trace("created new managed connection: " + mc);
        }

        return mc;
    }

    /**
     * Match a set of connections from the pool
     * @param info
     * @throws jakarta.resource.ResourceException
     */
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo info) throws ResourceException {
        boolean trace = log.isTraceEnabled();

        // Get cred
        info = getInfo(info);
        JmsCred cred = JmsCred.getJmsCred(this, subject, info);

        if (trace) {
            log.trace("Looking for connection matching credentials: " + cred);
        }

        // Traverse the pooled connections and look for a match, return first
        // found
        Iterator connections = connectionSet.iterator();

        while (connections.hasNext()) {
            Object obj = connections.next();

            // We only care for connections of our own type
            if (obj instanceof JmsManagedConnection) {
                // This is one from the pool
                JmsManagedConnection mc = (JmsManagedConnection) obj;

                // Check if we even created this on
                ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();

                // Only admit a connection if it has the same username as our
                // asked for creds

                // FIXME, Here we have a problem, jms connection
                // may be anonymous, have a user name

                if ((mc.getUserName() == null || (mc.getUserName() != null && mc.getUserName().equals(cred.name)))
                        && mcf.equals(this)) {
                    // Now check if ConnectionInfo equals
                    if (info.equals(mc.getInfo())) {

                        if (trace) {
                            log.trace("Found matching connection: " + mc);
                        }

                        return mc;
                    } else {
                        log.trace("Current info " + info + " don't match : " + mc.getInfo());
                    }
                }
            }
        }

        if (trace) {
            log.trace("No matching connection was found");
        }

        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    /**
     * Checks for equality ower the configured properties.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof JmsManagedConnectionFactory) {
            return mcfProperties.equals(((JmsManagedConnectionFactory) obj).getProperties());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mcfProperties.hashCode();
    }

    // --- Connfiguration API ---

    public void setJndiParameters(String jndiParameters) {
        mcfProperties.setJndiParameters(jndiParameters);
    }

    public String getJndiParameters() {
        return mcfProperties.getJndiParameters();
    }

    public void setConnectionFactory(String connectionFactory) {
        mcfProperties.setConnectionFactory(connectionFactory);
    }

    public String getConnectionFactory() {
        return mcfProperties.getConnectionFactory();
    }

    /**
     * Set userName, null by default.
     * @param userName
     */
    public void setUserName(String userName) {
        mcfProperties.setUserName(userName);
    }

    /**
     * Get userName, may be null.
     * @return 
     */
    public String getUserName() {
        return mcfProperties.getUserName();
    }

    /**
     * Set password, null by default.
     * @param password
     */
    public void setPassword(String password) {
        mcfProperties.setPassword(password);
    }

    /**
     * Get password, may be null.
     * @return 
     */
    public String getPassword() {
        return Strings.fromCharArray(mcfProperties.getPassword());
    }

    /**
     * Get client id, may be null.
     * @return 
     */
    public String getClientID() {
        return mcfProperties.getClientID();
    }

    /**
     * Set client id, null by default.
     * @param clientID
     */
    public void setClientID(final String clientID) {
        mcfProperties.setClientID(clientID);
    }

    public Boolean isStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public Boolean isJMS20() {
        return jms_2_0;
    }

    public void setJMS20(Boolean jms_2_0) {
        this.jms_2_0 = jms_2_0;
    }

    /**
     * Set the default session typ
     *
     * @param type either jakarta.jms.Topic or jakarta.jms.Queue
     * @throws ResourceException if type was not a valid type.
     */
    public void setSessionDefaultType(String type) throws ResourceException {
        mcfProperties.setSessionDefaultType(type);
    }

    public String getSessionDefaultType() {
        return mcfProperties.getSessionDefaultType();
    }

    /**
     * Get the useTryLock.
     *
     * @return the useTryLock.
     */
    public Integer getUseTryLock() {
        return useTryLock;
    }

    /**
     * Set the useTryLock.
     *
     * @param useTryLock the useTryLock.
     */
    public void setUseTryLock(Integer useTryLock) {
        this.useTryLock = useTryLock;
    }

    public void setDeleteTemporaryDestinations(Boolean deleteTemporaryDestinations) {
        this.deleteTemporaryDestinations = deleteTemporaryDestinations;
    }

    public Boolean isDeleteTemporaryDestinations() {
        return deleteTemporaryDestinations;
    }

    private ConnectionRequestInfo getInfo(ConnectionRequestInfo info) {
        if (info == null) {
            // Create a default one
            return new JmsConnectionRequestInfo(mcfProperties);
        } else {
            // Fill the one with any defaults
            ((JmsConnectionRequestInfo) info).setDefaults(mcfProperties);
            return info;
        }
    }

    public ConnectionMetaData getMetaData() {
        return new JmsConnectionMetaData();
    }

    //---- MCF to MCF API

    protected JmsMCFProperties getProperties() {
        return mcfProperties;
    }

}
