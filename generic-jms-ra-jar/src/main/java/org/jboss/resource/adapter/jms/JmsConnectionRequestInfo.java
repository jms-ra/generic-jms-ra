/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import jakarta.resource.spi.ConnectionRequestInfo;

import jakarta.jms.Session;

import org.jboss.resource.adapter.jms.util.Strings;

/**
 * Request information used in pooling
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsConnectionRequestInfo implements ConnectionRequestInfo {
    private String userName;
    private char[] password;
    private String clientID;

    private boolean transacted = true;
    private int acknowledgeMode = Session.SESSION_TRANSACTED;
    private int type = JmsConnectionFactory.AGNOSTIC;

    /**
     * Creats with the MCF configured properties.
     * @param prop
     */
    public JmsConnectionRequestInfo(JmsMCFProperties prop) {
        this.userName = prop.getUserName();
        this.password = prop.getPassword();
        this.clientID = prop.getClientID();
        this.type = prop.getType();
    }

    /**
     * Create with specified properties.
     */
    public JmsConnectionRequestInfo(final boolean transacted, final int acknowledgeMode, final int type) {
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.type = type;
    }

    /**
     * Fill in default values if missing. Only applies to user and password.
     */
    public void setDefaults(JmsMCFProperties prop) {
        if (userName == null) {
            userName = prop.getUserName();//May be null there to
        }
        if (password == null) {
            password = prop.getPassword();//May be null there to
        }
        if (clientID == null) {
            clientID = prop.getClientID();//May be null there to
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        userName = name;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = Strings.toCharArray(password);
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof JmsConnectionRequestInfo) {
            JmsConnectionRequestInfo you = (JmsConnectionRequestInfo) obj;
            return (this.transacted == you.isTransacted() &&
                    this.acknowledgeMode == you.getAcknowledgeMode() &&
                    this.type == you.getType() &&
                    Strings.compare(userName, you.getUserName()) &&
                    Strings.compare(password, you.getPassword()) &&
                    Strings.compare(clientID, you.getClientID()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (transacted) {
            hashCode += 1;
        }
        if (type == JmsConnectionFactory.QUEUE) {
            hashCode += 3;
        } else if (type == JmsConnectionFactory.TOPIC) {
            hashCode += 5;
        }
        if (acknowledgeMode == Session.AUTO_ACKNOWLEDGE) {
            hashCode += 7;
        } else if (acknowledgeMode == Session.DUPS_OK_ACKNOWLEDGE) {
            hashCode += 11;
        }
        if (userName != null) {
            hashCode += userName.hashCode();
        }
        if (password != null) {
            hashCode += new String(password).hashCode();
        }
        if (clientID != null) {
            hashCode += clientID.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return "JmsConnectionRequestInfo{" + "userName=" + userName
                + ", password=****, clientID=" + clientID
                + ", transacted=" + transacted
                + ", acknowledgeMode=" + acknowledgeMode + ", type=" + type + '}';
    }
}
