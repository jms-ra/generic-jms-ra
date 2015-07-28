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
package org.jboss.resource.adapter.jms.inflow;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.JmsResourceAdapter;
import org.jboss.resource.adapter.jms.SecurityActions;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.jms.XAConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A generic jms Activation.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="jbertram@redhat.com">Justin Bertram</a>
 */
public class JmsActivation implements ExceptionListener {
    /**
     * The log
     */
    private static final Logger log = Logger.getLogger(JmsActivation.class);

    // this should work in AS7, not sure about any other container
    private static final String JNDI_NAME = "java:jboss/TransactionManager";

    /**
     * The onMessage method
     */
    public static final Method ONMESSAGE;

    /**
     * The resource adapter
     */
    protected JmsResourceAdapter ra;

    /**
     * The activation spec
     */
    protected JmsActivationSpec spec;

    /**
     * The message endpoint factory
     */
    protected MessageEndpointFactory endpointFactory;

    /**
     * Whether delivery is active
     */
    protected AtomicBoolean deliveryActive = new AtomicBoolean(false);

    /**
     * Whether we are in the failure recovery loop
     */
    private AtomicBoolean inFailure = new AtomicBoolean(false);

    /**
     * The destination
     */
    protected Destination destination;

    /**
     * The destination type
     */
    protected boolean isTopic = false;

    /**
     * The connection
     */
    protected Connection connection;

    /**
     * The server session pool
     */
    protected JmsServerSessionPool pool;

    /**
     * Is the delivery transacted
     */
    protected boolean isDeliveryTransacted;

    /**
     * The TransactionManager
     */
    protected TransactionManager tm;


    static {
        try {
            ONMESSAGE = MessageListener.class.getMethod("onMessage", new Class[]{Message.class});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JmsActivation(JmsResourceAdapter ra, MessageEndpointFactory endpointFactory, JmsActivationSpec spec) throws ResourceException {
        this.ra = ra;
        this.endpointFactory = endpointFactory;
        this.spec = spec;
        try {
            this.isDeliveryTransacted = endpointFactory.isDeliveryTransacted(ONMESSAGE);
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    /**
     * @return the activation spec
     */
    public JmsActivationSpec getActivationSpec() {
        return spec;
    }

    /**
     * @return the message endpoint factory
     */
    public MessageEndpointFactory getMessageEndpointFactory() {
        return endpointFactory;
    }

    /**
     * @return whether delivery is transacted
     */
    public boolean isDeliveryTransacted() {
        return isDeliveryTransacted;
    }

    /**
     * @return the work manager
     */
    public WorkManager getWorkManager() {
        return ra.getWorkManager();
    }

    public TransactionManager getTransactionManager() {
        if (tm == null) {
            ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
            try {
                SecurityActions.setThreadContextClassLoader(JmsActivation.class.getClassLoader());
                InitialContext ctx = new InitialContext();
                tm = (TransactionManager) ctx.lookup(JNDI_NAME);
                if (log.isTraceEnabled()) {
                    log.trace("Got a transaction manager from jndi " + tm);
                }
            } catch (NamingException e) {
                log.debug("Unable to lookup: " + JNDI_NAME, e);
            } finally {
                SecurityActions.setThreadContextClassLoader(oldTCCL);
            }
        }
        return tm;
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * @return the destination type
     */
    public boolean isTopic() {
        return isTopic;
    }

    /**
     * Start the activation
     *
     * @throws ResourceException for any error
     */
    public void start() throws ResourceException {
        deliveryActive.set(true);
        ra.getWorkManager().scheduleWork(new SetupActivation());
    }

    /**
     * Stop the activation
     */
    public void stop() {
        deliveryActive.set(false);
        teardown();
    }

    /**
     * Handles any failure by trying to reconnect
     *
     * @param failure the reason for the failure
     */
    public void handleFailure(Throwable failure) {
        log.warn("Failure in jms activation " + spec, failure);
        int reconnectCount = 0;

        // Only enter the failure loop once
        if (inFailure.getAndSet(true)) {
            return;
        }
        try {
            while (deliveryActive.get() && (reconnectCount < spec.getReconnectAttempts() || spec.getReconnectAttempts() == -1)) {
                teardown();

                try {
                    Thread.sleep(spec.getReconnectIntervalLong());
                } catch (InterruptedException e) {
                    log.debug("Interrupted trying to reconnect " + spec, e);
                    break;
                }

                log.info("Attempting to reconnect " + spec);
                try {
                    setupActivation();
                    log.info("Reconnected with messaging provider.");
                    break;
                } catch (Throwable t) {
                    log.error("Unable to reconnect " + spec, t);
                }
                ++reconnectCount;
            }
        } finally {
            // Leaving failure recovery loop
            inFailure.set(false);
        }
    }

    public void onException(JMSException exception) {
        handleFailure(exception);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(defaultToString(this)).append('(');
        buffer.append("spec=").append(defaultToString(spec));
        buffer.append(" endpointFactory=").append(defaultToString(endpointFactory));
        buffer.append(" deliveryActive=").append(deliveryActive.get());
        if (destination != null) {
            buffer.append(" destination=").append(destination);
        }
        if (connection != null) {
            buffer.append(" connection=").append(connection);
        }
        if (pool != null) {
            buffer.append(" pool=").append(defaultToString(pool));
        }
        buffer.append(" isDeliveryTransacted=").append(isDeliveryTransacted);
        buffer.append(')');
        return buffer.toString();
    }

    /**
     * Setup the activation
     *
     * @throws Exception for any error
     */
    private void setupActivation() throws Exception {
        ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
        try {
            // Set the TCCL to the JmsActivation class loader
            // to ensure that the underlying initial context factory can be instantiated
            SecurityActions.setThreadContextClassLoader(JmsActivation.class.getClassLoader());

            log.debug("Setting up " + spec);
            Context ctx = convertStringToContext(spec.getJndiParameters());
            log.debug("Using context " + ctx.getEnvironment() + " for " + spec);
            try {
                setupDestination(ctx);
                setupConnection(ctx);
            } finally {
                ctx.close();
            }
            setupSessionPool();

            log.debug("Setup complete " + this);
        } finally {
            SecurityActions.setThreadContextClassLoader(oldTCCL);
        }

    }

    public static Context convertStringToContext(String jndiParameters) throws NamingException {
        Properties properties = convertStringToProperties(jndiParameters);

        if (properties.isEmpty()) {
            return new InitialContext();
        } else {
            return new InitialContext(properties);
        }
    }

    static Properties convertStringToProperties(String jndiParameters) {
        Properties properties = new Properties();
        if (jndiParameters != null) {
            String[] elements = jndiParameters.split(";");
            for (String element : elements) {
                String[] nameValue = element.split("=", 2);
                if (nameValue.length == 2) {
                    properties.setProperty(nameValue[0], nameValue[1]);
                }
            }
        }
        return properties;
    }

    /**
     * Teardown the activation
     */
    protected void teardown() {
        log.debug("Tearing down " + spec);

        teardownSessionPool();
        teardownConnection();
        teardownDestination();

        log.debug("Tearing down complete " + this);
    }

    /**
     * Setup the Destination
     *
     * @param ctx the naming context
     * @throws Exception for any error
     */
    protected void setupDestination(Context ctx) throws Exception {
        String destinationName = spec.getDestination();

        String destinationTypeString = spec.getDestinationType();
        log.debug("Destination type defined as " + destinationTypeString);

        Class<?> destinationType;
        if (Topic.class.getName().equals(destinationTypeString)) {
            destinationType = Topic.class;
        } else if (Queue.class.getName().equals(destinationTypeString)) {
            destinationType = Queue.class;
        } else {
            destinationType = Destination.class;
        }

        log.debug("Retrieving destination " + destinationName + " of type " + destinationType.getName());
        destination = (Destination) lookup(ctx, destinationName, destinationType);
        if (destination instanceof Topic) {
            isTopic = true;
        }

        log.debug("Got destination " + destination + " from " + destinationName);
    }

    /**
     * Teardown the destination
     */
    protected void teardownDestination() {
        destination = null;
    }

    /**
     * Setup the Connection
     *
     * @param ctx the naming context
     * @throws Exception for any error
     */
    private void setupConnection(Context ctx) throws Exception {
        log.debug("setup connection " + this);

        String user = spec.getUser();
        String pass = spec.getPassword();
        String clientID = spec.getClientId();
        String connectionFactory = spec.getConnectionFactory();

        connection = setupConnection(ctx, user, pass, clientID, connectionFactory);

        log.debug("established connection " + this);
    }

    /**
     * Setup a Generic JMS Connection
     *
     * @param ctx               the naming context
     * @param user              the user
     * @param pass              the password
     * @param clientID          the client id
     * @param connectionFactory the connection factory from JNDI
     * @return the connection
     * @throws Exception for any error
     */
    private Connection setupConnection(Context ctx, String user, String pass, String clientID, String connectionFactory) throws Exception {
        log.debug("Attempting to lookup connection factory " + connectionFactory);
        Object preliminaryObject = lookup(ctx, connectionFactory, Object.class);
        log.debug("Got connection factory " + preliminaryObject + " from " + connectionFactory);
        log.debug("Attempting to create connection with user " + user);
        Connection result;
        if (isDeliveryTransacted) {
            XAConnectionFactory xagcf = (XAConnectionFactory) preliminaryObject;
            if (user != null) {
                result = xagcf.createXAConnection(user, pass);
            } else {
                result = xagcf.createXAConnection();
            }
        } else {
            if (preliminaryObject instanceof XAConnectionFactory) {
                XAConnectionFactory xagcf = (XAConnectionFactory) preliminaryObject;
                if (user != null) {
                    result = xagcf.createXAConnection(user, pass);
                } else {
                    result = xagcf.createXAConnection();
                }
            } else {
                ConnectionFactory gcf = (ConnectionFactory) preliminaryObject;
                if (user != null) {
                    result = gcf.createConnection(user, pass);
                } else {
                    result = gcf.createConnection();
                }
            }
        }
        try {
            if (clientID != null) {
                result.setClientID(clientID);
            }
            result.setExceptionListener(this);
            log.debug("Using generic connection " + result);
            return result;
        } catch (Throwable t) {
            try {
                result.close();
            } catch (Exception e) {
                log.trace("Ignored error closing connection", e);
            }
            if (t instanceof Exception) {
                throw (Exception) t;
            }
            throw new RuntimeException("Error configuring connection", t);
        }
    }

    /**
     * Teardown the connection
     */
    protected void teardownConnection() {
        try {
            if (connection != null) {
                log.debug("Closing the " + connection);
                connection.close();
            }
        } catch (Throwable t) {
            log.debug("Error closing the connection " + connection, t);
        }
        connection = null;
    }

    /**
     * Setup the server session pool
     *
     * @throws Exception for any error
     */
    protected void setupSessionPool() throws Exception {
        pool = new JmsServerSessionPool(this);
        log.debug("Created session pool " + pool);

        log.debug("Starting session pool " + pool);
        pool.start();
        log.debug("Started session pool " + pool);

        log.debug("Starting delivery " + connection);
        connection.start();
        log.debug("Started delivery " + connection);
    }

    /**
     * Teardown the server session pool
     */
    protected void teardownSessionPool() {
        try {
            if (connection != null) {
                log.debug("Stopping delivery " + connection);
                connection.stop();
            }
        } catch (Throwable t) {
            log.debug("Error stopping delivery " + connection, t);
        }

        try {
            if (pool != null) {
                log.debug("Stopping the session pool " + pool);
                pool.stop();
            }
        } catch (Throwable t) {
            log.debug("Error clearing the pool " + pool, t);
        }
        pool = null;
    }

    /**
     * Handles the setup
     */
    private class SetupActivation implements Work {
        public void run() {
            try {
                setupActivation();
            } catch (Throwable t) {
                handleFailure(t);
            }
        }

        public void release() {
        }
    }

    public static final String defaultToString(Object object) {
        if (object == null) {
            return "null";
        } else {
            return object.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(object));
        }
    }

    private static Object lookup(Context context, String name, Class clazz) throws Exception {
        Object result = context.lookup(name);
        Class objectClass = result.getClass();
        if (clazz.isAssignableFrom(objectClass) == false) {
            StringBuffer buffer = new StringBuffer(100);
            buffer.append("Object at '").append(name);
            buffer.append("' in context ").append(context.getEnvironment());
            buffer.append(" is not an instance of ");
            appendClassInfo(buffer, clazz);
            buffer.append(" object class is ");
            appendClassInfo(buffer, result.getClass());
            throw new ClassCastException(buffer.toString());
        }
        return result;
    }

    /**
     * Append Class Info
     *
     * @param buffer the buffer to append to
     * @param clazz  the class to describe
     */
    private static void appendClassInfo(StringBuffer buffer, Class clazz) {
        buffer.append("[class=").append(clazz.getName());
        buffer.append(" classloader=").append(clazz.getClassLoader());
        buffer.append(" interfaces={");
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append("interface=").append(interfaces[i].getName());
            buffer.append(" classloader=").append(interfaces[i].getClassLoader());
        }
        buffer.append("}]");
    }
}
