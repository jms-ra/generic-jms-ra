package org.jboss.resource.adapter.jms.inflow;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;

/**
 * <p>This class can be used to define defaults for an MDB activation specification in the server profile.</p>
 * 
 * <p>This configuration is placed into a resource adapter admin object that can be referred to from a MDB via the activation spec.</p>
 * 
 * <p>This allows environment specific runtime configuration for a annotation based MDB to be defined by the application server configuration.</p>
 * 
 * <p>Example admin object configruation in the resource adapter</p>
 * 
 * <code>
 * <admin-object class-name="org.jboss.resource.adapter.jms.inflow.JmsActivationSpecDefaultsAdminObject" jndi-name="java:jboss/ra/example/jmsActivationSpecDefaultsAdminObject" enabled="true" use-java-context="false" pool-name="jmsActivationSpecDefaultsAdminObject">
 *     <config-property name="jndiParameters">
 *         java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://TIBCO_HOST:7222
 *     </config-property>
 *     <config-property name="user">user</config-property>
 *     <config-property name="password">password</config-property>
 * </admin-object>
 * </code>
 * 
 * <p>Example MBD configuration</p>
 * 
 * <code> 
 * @MessageDriven( name = "ConsumeMDB", activationConfig = {
 *     @ActivationConfigProperty(propertyName = "jndiJmsActivationSpecDefaultsAdminObject", propertyValue = "java:jboss/ra/example/jmsActivationSpecDefaultsAdminObject"),
 *     @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "QueueConnectionFactory"),
 *     @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
 *     @ActivationConfigProperty(propertyName = "destination", propertyValue = "example.queue"),
 * })
 * </code>
 */
public class JmsActivationSpecDefaultsAdminObject implements Referenceable, Serializable {
	
	private static final long serialVersionUID = 1L;

    private String jndiParameters;
    
    private String user;
    
    public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private String password;

	public String getJndiParameters() {
		return jndiParameters;
	}

	public void setJndiParameters(String jndiParameters) {
		this.jndiParameters = jndiParameters;
	}

	private Reference reference;

	@Override
	public Reference getReference() throws NamingException {
		return reference;
	}

	@Override
	public void setReference(Reference reference) {
		this.reference = reference;
	}
	
	
}
