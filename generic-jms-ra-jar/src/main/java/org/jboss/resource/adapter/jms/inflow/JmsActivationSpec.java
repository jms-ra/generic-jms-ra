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

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;
//import org.jboss.util.Strings;

/**
 * A generic jms ActivationSpec.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="jbertram@redhat.com">Justin Bertram</a>
 */
public class JmsActivationSpec implements ActivationSpec {
    /**
     * The log
     */
    private static final Logger log = Logger.getLogger(JmsActivationSpec.class);

    /**
     * The resource adapter
     */
    private ResourceAdapter ra;

    /**
     * The destination
     */
    private String destination;

    /**
     * The destination type
     */
    private String destinationType = Destination.class.getName();

    /**
     * The message selector
     */
    private String messageSelector;

    /**
     * The acknowledgement mode
     */
    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    /**
     * The subscription durability
     */
    private boolean subscriptionDurability;

    /**
     * The client id
     */
    private String clientId;

    /**
     * The subscription name
     */
    private String subscriptionName;

    /**
     * The reconnect interval in seconds
     */
    private long reconnectInterval = 10;

    /**
     * The user
     */
    private String user;

    /**
     * The password
     */
    private String pass;

    /**
     * The maximum number of messages
     */
    private int maxMessages = 1;

    /**
     * The minimum number of sessions
     */
    private int minSession = 1;

    /**
     * The maximum number of sessions
     */
    private int maxSession = 15;

    //Default to -1 attempts (i.e. infinite)
    private int reconnectAttempts = -1;

    private int transactionTimeout;

    private boolean forceClearOnShutdown = false;

    private long forceClearOnShutdownInterval = 1000;

    private int forceClearAttempts = 0;

    private String jndiParameters;

    private String connectionFactory;

    /*
     * Jndi Location for a resource adapter admin object contains default values for an activation specfication.
     */
    private String jndiJmsActivationSpecDefaultsAdminObject;
    
    /*
     * An admin object contains default values for an activation specfication. This object is looked up using jndiJmsActivationSpecDefaultsAdminObject.
     */
    private JmsActivationSpecDefaultsAdminObject jmsActivationSpecDefaultsAdminObject;
    
    public void setForceClearOnShutdown(boolean forceClear) {
        this.forceClearOnShutdown = forceClear;
    }

    public boolean isForceClearOnShutdown() {
        return this.forceClearOnShutdown;
    }

    public long getForceClearOnShutdownInterval() {
        return this.forceClearOnShutdownInterval;
    }

    public void setForceClearOnShutdownInterval(long forceClearOnShutdownInterval) {
        this.forceClearOnShutdownInterval = forceClearOnShutdownInterval;
    }

    public int getForceClearAttempts() {
        return forceClearAttempts;
    }

    public void setForceClearAttempts(int forceClearAttempts) {
        this.forceClearAttempts = forceClearAttempts;
    }

    /**
     * @return the acknowledgeMode.
     */
    public String getAcknowledgeMode() {
        if (Session.DUPS_OK_ACKNOWLEDGE == acknowledgeMode) {
            return "Dups-ok-acknowledge";
        } else if (Session.AUTO_ACKNOWLEDGE == acknowledgeMode) {
            return "Auto-acknowledge";
        } else {
            return "unknown";
        }
    }

    /**
     * @param acknowledgeMode The acknowledgeMode to set.
     */
    public void setAcknowledgeMode(String acknowledgeMode) {
        if ("DUPS_OK_ACKNOWLEDGE".equals(acknowledgeMode) || "Dups-ok-acknowledge".equals(acknowledgeMode)) {
            this.acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
        } else if ("AUTO_ACKNOWLEDGE".equals(acknowledgeMode) || "Auto-acknowledge".equals(acknowledgeMode)) {
            this.acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
        } else {
            throw new IllegalArgumentException("Unsupported acknowledgement mode: " + acknowledgeMode);
        }
    }

    /**
     * @return the acknowledgement mode
     */
    public int getAcknowledgeModeInt() {
        return acknowledgeMode;
    }

    /**
     * @return the clientId.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId The clientId to set.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the destination.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination The destination to set.
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the destinationType.
     */
    public String getDestinationType() {
        return destinationType;
    }

    /**
     * @param destinationType The destinationType to set.
     */
    public void setDestinationType(String destinationType) {
        if (Topic.class.getName().equals(destinationType) ||
                Queue.class.getName().equals(destinationType) ||
                Destination.class.getName().equals(destinationType)) {
            this.destinationType = destinationType;
        } else {
            throw new IllegalArgumentException("Unsupported destinationType: " + destinationType);
        }
    }

    /**
     * @return the messageSelector.
     */
    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * @param messageSelector The messageSelector to set.
     */
    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    /**
     * @return the subscriptionDurability.
     */
    public String getSubscriptionDurability() {
        if (subscriptionDurability) {
            return "Durable";
        } else {
            return "NonDurable";
        }
    }

    /**
     * @param subscriptionDurability The subscriptionDurability to set.
     */
    public void setSubscriptionDurability(String subscriptionDurability) {
        this.subscriptionDurability = "Durable".equals(subscriptionDurability);
    }

    /**
     * @return whether the subscription is durable.
     */
    public boolean isDurable() {
        return subscriptionDurability;
    }

    /**
     * @return the subscriptionName.
     */
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * @param subscriptionName The subscriptionName to set.
     */
    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    /**
     * @return the reconnectInterval.
     */
    public long getReconnectInterval() {
        return reconnectInterval;
    }

    /**
     * @param reconnectInterval The reconnectInterval to set.
     */
    public void setReconnectInterval(long reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    /**
     * @return the reconnect interval
     */
    public long getReconnectIntervalLong() {
        return reconnectInterval * 1000;
    }

    /**
     * @return the user.
     */
    public String getUser() {
    	if (user == null 
    			&& jmsActivationSpecDefaultsAdminObject != null 
    			&& jmsActivationSpecDefaultsAdminObject.getUser() != null) 
    	{
    		return jmsActivationSpecDefaultsAdminObject.getUser();
    	}
        return user;
    }

    /**
     * @param user The user to set.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password.
     */
    public String getPassword() {
    	if (pass == null 
    			&& jmsActivationSpecDefaultsAdminObject != null 
    			&& jmsActivationSpecDefaultsAdminObject.getPassword() != null) 
    	{
    		return jmsActivationSpecDefaultsAdminObject.getPassword();
    	}
        return pass;
    }

    /**
     * @param pass The password to set.
     */
    public void setPassword(String pass) {
        this.pass = pass;
    }

    /**
     * @return the maxMessages.
     */
    public int getMaxMessages() {
        return maxMessages;
    }

    /**
     * @param maxMessages The maxMessages to set.
     */
    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    /**
     * @return the maximum number of messages
     */
    public int getMaxMessagesInt() {
        return maxMessages;
    }

    /**
     * @return the minSession.
     */
    public int getMinSession() {
        return minSession;
    }

    /**
     * @param minSession The minSession to set.
     */
    public void setMinSession(int minSession) {
        this.minSession = minSession;
    }

    /**
     * @return the minimum number of sessions
     */
    public int getMinSessionInt() {
        return minSession;
    }

    /**
     * @return the maxSession.
     */
    public int getMaxSession() {
        return maxSession;
    }

    /**
     * @param maxSession The maxSession to set.
     */
    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    /**
     * @return the maximum number of sessions
     */
    public int getMaxSessionInt() {
        return maxSession;
    }

    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        this.ra = ra;
    }

    public void validate() throws InvalidPropertyException {
        if (log.isTraceEnabled()) {
            log.trace("validate " + this);
        }

        if (destination == null || destination.trim().equals("")) {
            throw new InvalidPropertyException("destination is mandatory");
        }

        if (connectionFactory == null || connectionFactory.trim().equals("")) {
            throw new InvalidPropertyException("connectionFactory is mandatory");
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(JmsActivation.defaultToString(this)).append('(');
        buffer.append("ra=").append(ra);
        buffer.append(" destination=").append(destination);
        buffer.append(" destinationType=").append(destinationType);
        if (messageSelector != null) {
            buffer.append(" messageSelector=").append(messageSelector);
        }
        buffer.append(" acknowledgeMode=").append(getAcknowledgeMode());
        buffer.append(" subscriptionDurability=").append(subscriptionDurability);
        if (clientId != null) {
            buffer.append(" clientID=").append(clientId);
        }
        if (subscriptionName != null) {
            buffer.append(" subscriptionName=").append(subscriptionName);
        }
        buffer.append(" reconnectInterval=").append(reconnectInterval);
        buffer.append(" reconnectAttempts=").append(reconnectAttempts);
        buffer.append(" user=").append(user);
        if (pass != null) {
            buffer.append(" password=").append("<not shown>");
        }
        buffer.append(" maxMessages=").append(maxMessages);
        buffer.append(" minSession=").append(minSession);
        buffer.append(" maxSession=").append(maxSession);
        buffer.append(" connectionFactory=").append(connectionFactory);
        buffer.append(" jndiParameters=").append(jndiParameters);
        buffer.append(" jndiJmsActivationSpecDefaultsAdminObject=").append(jndiJmsActivationSpecDefaultsAdminObject);
        if (jmsActivationSpecDefaultsAdminObject != null) {
            buffer.append(" jmsActivationSpecDefaultsAdminObject.jndiParameters=").append(jmsActivationSpecDefaultsAdminObject.getJndiParameters());
            buffer.append(" jmsActivationSpecDefaultsAdminObject.user=").append(jmsActivationSpecDefaultsAdminObject.getUser());
            if (jmsActivationSpecDefaultsAdminObject.getPassword() != null) {
            	buffer.append(" jmsActivationSpecDefaultsAdminObject.password=").append("<not shown>");
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    public int getReconnectAttempts() {
        return reconnectAttempts;
    }

    public void setReconnectAttempts(int reconnectAttempts) {
        this.reconnectAttempts = reconnectAttempts;
    }

    public int getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    public void setJndiParameters(String jndiParameters) {
        this.jndiParameters = jndiParameters;
    }

    public String getJndiParameters() {
    	if (jndiParameters == null 
    			&& jmsActivationSpecDefaultsAdminObject != null 
    			&& jmsActivationSpecDefaultsAdminObject.getJndiParameters() != null) 
    	{
    		return jmsActivationSpecDefaultsAdminObject.getJndiParameters();
    	}
        return jndiParameters;
    }

    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }
    
    public String getJndiJmsActivationSpecDefaultsAdminObject() {
		return jndiJmsActivationSpecDefaultsAdminObject;
	}

	public void setJndiJmsActivationSpecDefaultsAdminObject(
			String jndiJmsActivationSpecDefaultsAdminObject) {
		this.jndiJmsActivationSpecDefaultsAdminObject = jndiJmsActivationSpecDefaultsAdminObject;
		
		try {
			jmsActivationSpecDefaultsAdminObject = (JmsActivationSpecDefaultsAdminObject) new InitialContext().lookup(jndiJmsActivationSpecDefaultsAdminObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
