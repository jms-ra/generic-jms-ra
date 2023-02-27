# Generic JMS JCA Resource Adapter for WildFly

This project is for the Generic Jakarta Messaging Service JCA Resource Adapter for WildFly.  As the name suggests, this JCA RA provides the ability to integrate with any Jakarta Messaging Service broker which allows remote clients to look-up connection factories and destinations via JNDI (as outlined in section 4.2 of [the JMS 1.1 specification](http://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&ved=0CDEQFjAA&url=http%3A%2F%2Fdownload.oracle.com%2Fotn-pub%2Fjcp%2F7195-jms-1.1-fr-spec-oth-JSpec%2Fjms-1_1-fr-spec.pdf&ei=psavUKuZDaSy2wWZ54D4Cw&usg=AFQjCNGCh-3NatP_ezkZ6MSgeahTmUuyZg)).  It currently is only verified to work in WildFly and supports, for example, consuming messages with an MDB and sending messages with a JCA-base Jakarta Messaging Service connection factory to 3rd-party brokers.  It is based on the generic JMS JCA RA found in previous versions of JBoss AS (e.g. 4, 5, 6 and 7).  However, unlike those versions this is a stand-alone project now and no longer supports internal dead-letter processing since every modern JMS broker supports this already.

To be clear, the Generic Jakarta Messaging Service JCA Resource Adapter for WildFly should only be used if the Jakarta Messaging Service provider with which you are integrating does not have a JCA Resource Adapter of its own.  Most enterprise Jakarta Messaging Service providers have their own JCA RA, but for whatever reason there are still a few who are lacking this essential integration component.

## Get Help

Any questions, etc. can be posted on the ["Generic Jakarta Messaging Service JCA Resource Adapter for WildFly" Google Group](https://groups.google.com/forum/?fromgroups=#!forum/jboss-generic-jms-ra).

**Note:** this is a *community* project and has no official support from Red Hat in any capacity.  I will certainly work with the community to address any issues, but I cannot guarantee any particular [SLA](http://en.wikipedia.org/wiki/Service-level_agreement).

## Project structure

The project consists of three Maven modules:

- The parent module
 - The "generic-jms-ra-jar" module to create the library which goes inside the RAR.
 - The "generic-jms-ra-rar" module to create the actual resource adapter archive which is deployed within the Jakarta EE application server (e.g. WildFly).

FYI - Pre-built versions of the resource adapter archive used to be available in the [downloads section](https://github.com/jms-ra/generic-jms-ra/downloads), but [GitHub has deprecated this feature](https://github.com/blog/1302-goodbye-uploads).

## Build instructions

1. Download the source via any of the methods which GitHub provides (e.g. the [tags](https://github.com/jms-ra/generic-jms-ra/tags) page).
2. Execute 'mvn install' to build the code.
3. Execute 'mvn -Prelease install' to generate the deployable resource adapter.


## Transaction Support

JTA transactions are very commonly used with MDBs since it is easy to treat a JMS message as a unit of work which should be performed atomically.  For example, an MDB might consume a message, update a table in one or more databases, and then send another JMS message.  In this kind of use-case it's extremely common to require all this work be done atomically so that if any individual part fails then the whole unit of work fails which then usually re-delivers the original message or moves it to a DLQ of some kind.

To enable this behavior an MDB needs to be configured appropriately.  For example, it would need these annotations ( **Note:** these are added by default in WildFly and every other Jakarta EE 10 compliant application server):

    @TransactionManagement(TransactionManagementType.CONTAINER)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)

This tells the container to start a JTA transaction when it delivers a message to the MDB's onMessage method.  You can read more about the semantics of these annotations in [this tutorial from Oracle](http://docs.oracle.com/javaee/6/tutorial/doc/bncij.html).

Behind the scenes, the Java EE server and the JCA RA use the JTA API to enlist and handle all the various javax.transaction.xa.XAResource implementations from the resource managers involved in the transaction (e.g. JMS providers, JDBC datasources, etc.).  Section 8 of the JMS 1.1 specification entitled "JMS Application Server Facilities" describes all the interfaces which a JMS provider must implement in order to support this transactional use-case.  However, JMS providers are *not required* to implement these interfaces which means even though you may want this kind of behavior, you are at the mercy of the JMS provider with whom you are integrating.

When the Java EE server activates an MDB endpoint using the "Generic JMS JCA Resource Adapter for JBoss AS" the RA sets up the JMS sessions that will be used for consuming messages.  During the setup of these sessions the RA looks at the implementation of the connection factory object which the MDB is configured to use (via the `connectionFactory` activation configuration property) to see if it implements javax.jms.XAConnectionFactory.  If it does implement javax.jms.XAConnectionFactory then everything will be set up so that the aforementioned transactional use-case is possible.  If it doesn't implement javax.jms.XAConnectionFactory then the aforementioned transactional use-case will not be possible.  Instead you will have to annotate (or otherwise configure) your MDB with something like:

    @TransactionManagement(TransactionManagementType.CONTAINER)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)

Or perhaps:

    @TransactionManagement(TransactionManagementType.BEAN)

Otherwise an exception will be thrown and the MDB will not deploy.

## WildFly Deployment Notes

Since this is a <em>generic</em> Jakarta Messaging Service JCA RA, the user must supply it with the proper client classes to actually make a physical connection to a 3rd party Jakarta Messaging Service broker. Since WildFly uses a modular classload this requires the user to:

1. Create a module with the proper integration classes 
2. Modify the manifest.mf of the RAR to use the aforementioned module

If you don't want to use the @ResourceAdapter annotation on your EJB3 MDB(s) then you can change the default resource adapter which all MDBs in the system will use:

	<subsystem xmlns="urn:jboss:domain:ejb3:10.0">
	    ... 
	    <mdb>
	        <resource-adapter-ref resource-adapter-name="generic-jms-rar.rar"/>
	        <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
	    </mdb>
	    ...
	</subsystem>


### Example WildFLy deployment descriptor for an outbound connector

To create an outbound connection factory, use a deployment descriptor like this in your standalone*.xml.

	<subsystem xmlns="urn:jboss:domain:resource-adapters:6.0">
	    <resource-adapters>
	        <resource-adapter>
	            <archive>
	                generic-jms-ra-<VERSION>.rar
	            </archive>
	            <transaction-support>XATransaction</transaction-support>
	            <connection-definitions>
	                <connection-definition class-name="org.jboss.resource.adapter.jms.JmsManagedConnectionFactory" jndi-name="java:/GenericJmsXA" enabled="true" use-java-context="true" pool-name="GenericJmsXA" use-ccm="true">
	                    <config-property name="JndiParameters">
	                        java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=JBM_HOST:1099;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces
	                    </config-property>
	                    <config-property name="ConnectionFactory">
	                        XAConnectionFactory
	                    </config-property>
	                    <xa-pool>
	                        <min-pool-size>0</min-pool-size>
	                        <max-pool-size>10</max-pool-size>
	                        <prefill>false</prefill>
	                        <use-strict-min>false</use-strict-min>
	                        <flush-strategy>FailingConnectionOnly</flush-strategy>
	                        <pad-xid>false</pad-xid>
	                        <wrap-xa-resource>true</wrap-xa-resource>
	                    </xa-pool>
	                    <security>
	                        <application/>
	                    </security>
	                </connection-definition>
	            </connection-definitions>
	        </resource-adapter>
	    </resource-adapters>
	</subsystem>

This particular configuration binds a JMS connection factory to "java:/GenericJmsXA".  Under the covers it looks up the "XAConnectionFactory" via JNDI from JBM_HOST.  The "JndiParameters" are, of course, specific to JBoss AS 5 since that is the JNDI implementation to which we are connecting here.  To connect to a different kind of server you'll need to specify its specific JNDI properties as appropriate.

### Example EJB3 MDB

This MDB will connect to JBM_HOST using the "XAConnectionFactory" and consume messages from the "queue/source" destination.  It's important to note that the RA will use the "jndiParameters" activation configuration property to lookup the "connectionFactory" and the "destination."

Once a message is received the MDB will use the "java:/GenericJmsXA" connection factory (defined above) to send a message to the "target" destination hosted on JBM_HOST.  Notice here that the GenericJmsXA connection factory is looked up via JNDI, but the "target" destination is not looked up via JNDI but rather instantiated with javax.jms.Session.createQueue(String) where the Sting parameter is the actual name of the destination (i.e. not necessarily where it is bound in JNDI).  This is done because we want to avoid a full JNDI look-up of the destination on the remote server, and because there is currently no way to make a local JNDI look-up go to a remote server (a la the ExternalContext MBean from JBoss AS 4, 5, and 6).  The reason it is typically good to avoid a full JNDI lookup of the destination on the remote server is because it saves the developer from having to specify the same JNDI lookup parameters both in the code and the activation configuration.

The consumption and production will be done atomically because the underlying connection factories used to consume and produce the messages support XA and also because the way the MDB is coded to rollback the transaction when production fails for any reason.

	import jakarta.ejb.ActivationConfigProperty;
	import jakarta.ejb.MessageDriven;
	import jakarta.jms.Connection;
	import jakarta.jms.ConnectionFactory;
	import jakarta.jms.Destination;
	import jakarta.jms.Message;
	import jakarta.jms.MessageListener;
	import jakarta.jms.MessageProducer;
	import jakarta.jms.Session;
	import javax.naming.Context;
	import javax.naming.InitialContext;
	
	import org.jboss.ejb3.annotation.ResourceAdapter;
	
	@MessageDriven(activationConfig = {
	      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	      @ActivationConfigProperty(propertyName = "destination", propertyValue = "/queue/source"),
	      @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=JBM_HOST:1099;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces"),
	      @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "XAConnectionFactory")
	})
	@ResourceAdapter("generic-jms-ra-<VERSION>.rar")
	public class ExampleMDB implements MessageListener
	{

	   @Resource
	   private MessageDrivenContext context;

	   public void onMessage(final Message message)
	   {
	      Connection connection = null;

	      try
	      {
	         Context context = new InitialContext();
	         ConnectionFactory cf = (ConnectionFactory) context.lookup("java:/GenericJmsXA");
	         context.close();
	         connection = cf.createConnection();
	         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	         Destination destination = session.createQueue("target");
	         MessageProducer producer = session.createProducer(destination);
	         Message msg = session.createTextMessage("example text");
	         producer.send(msg);
	      }
	      catch (Exception e)
	      {
	         context.setRollbackOnly();
	      }
	      finally
	      {
	         if (connection != null)
	         {
	            connection.close();
	         }
	      }
	   }
	}

If you don't want to specify your MDB configuration in the code via annotations then you can use the traditional ejb-jar.xml deployment descriptor.  Furthermore, in JBoss AS7 you can use system property substitution in ejb-jar.xml so that you can change the configuration of the MDB without opening the archive in which the MDB is deployed (e.g. JAR or EAR).  This is helpful when for example you need to move an application from a development environment to a QA or production environment.  To enable system property substitution in ejb-jar.xml in JBoss AS7 set the `<spec-descriptor-property-replacement>` to `true`, e.g.:

    <subsystem xmlns="urn:jboss:domain:ee:1.1">
        <spec-descriptor-property-replacement>true</spec-descriptor-property-replacement>
        <jboss-descriptor-property-replacement>true</jboss-descriptor-property-replacement>
    </subsystem>

As I understand it, `<spec-descriptor-property-replacement>` is set to `false` by default because of the way the Java EE TCK works.

Here is an example ejb-jar.xml that would replace the @MessageDriven configuration above:

    <?xml version="1.0" encoding="UTF-8"?>
    <ejb-jar version="3.0" xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd">
       <enterprise-beans>
          <message-driven>
             <ejb-name>ExampleMDB</ejb-name>
             <ejb-class>ExampleMDB</ejb-class>
             <activation-config>
                <activation-config-property>
                    <activation-config-property-name>destinationType</activation-config-property-name>
                    <activation-config-property-value>javax.jms.Queue</activation-config-property-value>
                </activation-config-property>
                <activation-config-property>
                   <activation-config-property-name>destination</activation-config-property-name>
                   <activation-config-property-value>/queue/source</activation-config-property-value>
                </activation-config-property>
                <activation-config-property>
                   <activation-config-property-name>jndiParameters</activation-config-property-name>
                   <activation-config-property-value>java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=${myJmsProvider};java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces</activation-config-property-value>
                </activation-config-property>
                <activation-config-property>
                   <activation-config-property-name>connectionFactory</activation-config-property-name>
                   <activation-config-property-value>${myConnectionFactory}</activation-config-property-value>
                </activation-config-property>
             </activation-config>
          </message-driven>
       </enterprise-beans>
    </ejb-jar>

Notice that both the `jndiParameters` and `connectionFactory` activation configuration properties use a special `${}` syntax in their values.  That is where the substitution would take place.  You can specify the value of those substitutions on the command line when you start JBoss AS7, e.g.:

    ./standalone.sh -c standalone-full.xml -DmyJmsProvider=hostname:1099 -DmyConnectionFactory=XAConnectionFactory

Lastly, when deploying an MDB which depends on a non-default RA it is customary to modify the MDB's deployment so that it is not deployed until the RA it needs has been deployed.  To do this in JBoss AS7 simply add this line to the META-INF/manifest.mf of your deployment:

	Dependencies: deployment.generic-jms-rar.rar

You can set up this kind of dependency for any application that needs to use the RA (e.g. a servlet sending a JMS message).	

## Tibco Integration 

This information was provided by community members as I don't have access to a Tibco instance.  I have received module definitions from 2 different versions of Tibco EMS.  I imagine the same configuration could be used for other Tibco versions as well.

### Tibco EMS 5.1 Module

	<?xml version='1.0' encoding='UTF-8'?>
	<module xmlns="urn:jboss:module:1.1" name="com.tibco.tibjms">
	    <resources>
	        <resource-root path="slf4j-api-1.4.2.jar"></resource-root>
	        <resource-root path="slf4j-simple-1.4.2.jar"></resource-root>
	        <resource-root path="tibcrypt.jar"></resource-root>
	        <resource-root path="tibjms.jar"></resource-root>
	        <resource-root path="tibjmsadmin.jar"></resource-root>
	        <resource-root path="tibjmsufo.jar"></resource-root>
	    </resources>
	    <dependencies>
	        <module name="jakarta.api"/>
	        <module name="javjakartaax.jms.api"/>
	    </dependencies>
	</module>

### Tibco EMS 6.0 Module

    <?xml version='1.0' encoding='UTF-8'?>
    <module xmlns="urn:jboss:module:1.1" name="com.tibco.tibjms">
        <resources>
            <resource-root path="tibjms.jar"/>
            <resource-root path="tibcrypt.jar"/>
        </resources>
        <dependencies>
            <module name="jakarta.api"/>
            <module name="jakarta.jms.api"/>
        </dependencies>
    </module>

### Example AS7 deployment descriptor for an outbound connector

	<subsystem xmlns="urn:jboss:domain:resource-adapters:6.0">
	    <resource-adapters>
	        <resource-adapter>
	            <archive>
	                generic-jms-ra-<VERSION>.rar
	            </archive>
	            <transaction-support>NoTransaction</transaction-support>
	            <connection-definitions>
	                <connection-definition class-name="org.jboss.resource.adapter.jms.JmsManagedConnectionFactory" jndi-name="java:/GenericJmsXA" enabled="true" use-java-context="true" pool-name="GenericJmsXA" use-ccm="true">
	                    <config-property name="JndiParameters">
	                        java.naming.factory.initial=com.tibco.tibjms.naming.TibjmsInitialContextFactory;java.naming.provider.url=tcp://TIBCO_HOST:7222
	                    </config-property>
	                    <config-property name="ConnectionFactory">
	                        QueueConnectionFactory
	                    </config-property>
	                    <pool>
	                        <min-pool-size>0</min-pool-size>
	                        <max-pool-size>10</max-pool-size>
	                        <prefill>false</prefill>
	                        <use-strict-min>false</use-strict-min>
	                        <flush-strategy>FailingConnectionOnly</flush-strategy>
	                    </pool>
	                    <security>
	                        <application></application>
	                    </security>
	                </connection-definition>
	            </connection-definitions>
	        </resource-adapter>
	    </resource-adapters>
	</subsystem>

Notice the "JndiParameters" are Tibco specific.

## SonicMQ Integration

This was provided from the community.

### SonicMQ 8.5.1 Module

    <module xmlns="urn:jboss:module:1.1" name="com.sonic.sonic-esb">
        <resources>
            <resource-root path="mfcontext.jar"/>
            <resource-root path="sonic_ASPI.jar"/>
            <resource-root path="sonic_Client.jar"/>
            <resource-root path="sonic_Crypto.jar"/>
            <resource-root path="sonic_Selector.jar"/>
            <resource-root path="sonic_XA.jar"/>
            <resource-root path="sonic_XMessage.jar"/>
        </resources>
 
        <dependencies>
            <module name="javax.api"/>
            <module name="javax.jms.api"/>
        </dependencies>
    </module>

### Example AS7 deployment descriptor for an outbound connector

        <subsystem xmlns="urn:jboss:domain:resource-adapters:1.0">
            <resource-adapters>
                <resource-adapter>
                    <archive>
                        generic-jms-ra-<VERSION>.rar
                    </archive>
                    <transaction-support>XATransaction</transaction-support>
                    <connection-definitions>
                        <connection-definition class-name="org.jboss.resource.adapter.jms.JmsManagedConnectionFactory" jndi-name="java:/GenericJmsXA" enabled="true" use-java-context="true" pool-name="GenericJmsXA" use-ccm="true">
                            <config-property name="JndiParameters">
                                java.naming.factory.initial=com.sonicsw.jndi.mfcontext.MFContextFactory;com.sonicsw.jndi.mfcontext.domain=sonic_d1;java.naming.provider.url=tcp://sonicmq-host1:2506,tcp://sonicmq-host2:2506;java.naming.security.principal=jboss;java.naming.security.credentials=test
                            </config-property>
                            <config-property name="ConnectionFactory">
                                qcf_jbosstest
                            </config-property>
                            <xa-pool>
                                <min-pool-size>0</min-pool-size>
                                <max-pool-size>10</max-pool-size>
                                <prefill>false</prefill>
                                <use-strict-min>false</use-strict-min>
                                <flush-strategy>FailingConnectionOnly</flush-strategy>
                                <pad-xid>false</pad-xid>
                                <wrap-xa-resource>true</wrap-xa-resource>
                            </xa-pool>
                            <security>
                                <application/>
                            </security>
                        </connection-definition>
                    </connection-definitions>
                </resource-adapter>
            </resource-adapters>
        </subsystem>

### Example EJB3 MDB Configuration

    @MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jboss.in"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=com.sonicsw.jndi.mfcontext.MFContextFactory;com.sonicsw.jndi.mfcontext.domain=sonic_d1;java.naming.provider.url=tcp://sonicmq-host1:2506,tcp://sonicmq-host2:2506;java.naming.security.principal=jboss;java.naming.security.credentials=test"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "qcf_jbosstest"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "jboss"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "test") })
    @ResourceAdapter("generic-jms-ra-<VERSION>.rar")

## Activation Configuration Properties (for inbound)

### Most commonly used activation configuration properties
* <strong>destination</strong> - the JNDI name of JMS destination from which the MDB will consume messages; **this is required**
* <strong>destinationType</strong> - the type of JMS destination from which to consume messages; valid values are `javax.jms.Queue`, `javax.jms.Topic`, or `javax.jms.Destination`; default is `javax.jms.Destination`
* <strong>jndiParameters</strong> - the JNDI parameters used to perform the lookup of the destination and the connectionFactory; each parameter consists of a "name=value" pair; parameters are separated with a semi-colon (';'); if no parameters are specified then an empty InitialContext will be used (i.e. the lookup will be local)
* <strong>connectionFactory</strong> - the JNDI name of connection factory which the RA will use to consume the messages; this is normally a connection factory which supports XA; **this is required**

### Less commonly used activation configuration properties
* <strong>messageSelector</strong> - the JMS selector to use when consuming messages; default is null
* <strong>acknowledgeMode</strong> - the acknowledgement mode used when consuming messages; only applicable when using Bean-Managed transactions; valid values are `DUPS_OK_ACKNOWLEDGE` and `AUTO_ACKNOWLEDGE`; default is `AUTO_ACKNOWLEDGE`; when Container-Managed transactions are used the acknowledgement of the message is performed by the Java EE application server in accordance with the outcome of the MDB's transaction (assuming such a transaction exists)
* <strong>subscriptionDurability</strong> - the durability of the topic subscription; default is non-durable; the value "Durable" makes the subscription durable, anything else makes it non-durable
* <strong>clientId</strong> - the client ID to use for a topic subscription
* <strong>subscriptionName</strong> - the name of the topic subscription
* <strong>reconnectInterval</strong> - how long to wait between reconnectAttempts; value is measured in seconds; default is 10
* <strong>reconnectAttempts</strong> - how many times to try to reconnect if the connection to the JMS broker is lost; default is -1 (i.e. infinite attempts)
* <strong>user</strong> - the name of the user used when connecting to the JMS provider
* <strong>password</strong> - the password used when connecting to the JMS provider
* <strong>minSession</strong> - the minimum number of JMS sessions to create; default is 1
* <strong>maxSession</strong> - the maximum number of JMS sessions to create; default is 15

### Rarely used activation configuration properties
* <strong>maxMessages</strong> - the value passed to `javax.jms.ConnectionConsumer.createConnectionConsumer(..)`; see section 8.2.4 of the JMS 1.1 specification for further details; default is 1
* <strong>transactionTimeout</strong> - the value used for the JTA transaction timeout when using Container-Managed transactions; default is 0 (i.e. use the system default timeout)
* <strong>forceClearOnShutdown</strong> - whether or not to wait for MDB processing to complete before shutting down the internal JMS ServerSession pool; default is false (i.e. wait for MDB processing to complete)
* <strong>forceClearOnShutdownInterval</strong> - how long to wait between attempts to shutdown the internal JMS ServerSession pool; value is measured in milliseconds; default is 1000
* <strong>forceClearAttempts</strong> - how many times to attempt shutting down the internal JMS ServerSession pool; default is 0

## Connection Factory Configuration Properties (for outbound)

* <strong>JndiParameters</strong> - the JNDI parameters used to perform the lookup of the ConnectionFactory (see below); each parameter consists of a "name=value" pair; parameters are separated with a semi-colon (';'); if no parameters are specified then an empty InitialContext will be used (i.e. the lookup will be local)
* <strong>ConnectionFactory</strong> - the JNDI name of connection factory which the RA will use to send the messages; this is normally a connection factory which supports XA; **this is required**
* <strong>UserName</strong> - the name of the user used when connecting to the JMS provider
* <strong>Password</strong> - the password used when connecting to the JMS provider
* <strong>ClientID</strong> - the client ID to set on the connection (e.g. for a topic subscription)
* <strong>SessionDefaultType</strong> - set this to match the kind of session your application needs; valid values are "javax.jms.Topic" (set this if you are using `javax.jms.TopicConnection.createTopicSession()`) and "javax.jms.Queue" (set this if you are using `javax.jms.QueueConnection.createQueueSession()`); do not set if you are using `javax.jms.Session.createSession()`
