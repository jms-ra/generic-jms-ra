/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.util.Set;
import java.util.Iterator;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.SecurityException;
import jakarta.resource.spi.ConnectionRequestInfo;

import jakarta.resource.spi.security.PasswordCredential;

/**
 * Credential information
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsCred {
    public String name;

    public char[] pwd;

    public JmsCred() {
        // empty
    }

    /**
     * Get our own simple cred
     * @param mcf
     * @param subject
     * @param info
     * @return
     * @throws SecurityException 
     */
    public static JmsCred getJmsCred(ManagedConnectionFactory mcf, Subject subject, ConnectionRequestInfo info) throws SecurityException {
        JmsCred jc = new JmsCred();
        if (subject == null && info != null) {
            // Credentials specifyed on connection request
            jc.name = ((JmsConnectionRequestInfo) info).getUserName();
            jc.pwd = ((JmsConnectionRequestInfo) info).getPassword();
        } else if (subject != null) {
            // Credentials from appserver
            PasswordCredential pwdc = GetCredentialAction.getCredential(subject, mcf);
            if (pwdc == null) {
                // No hit - we do need creds
                throw new SecurityException("No Password credentials found");
            }
            jc.name = pwdc.getUserName();
            jc.pwd = pwdc.getPassword();
        } else {
            throw new SecurityException("No Subject or ConnectionRequestInfo set, could not get credentials");
        }
        return jc;
    }

    @Override
    public String toString() {
        return super.toString() + "{ username=" + name + ", password=**** }";
    }

    private static class GetCredentialAction implements PrivilegedAction<PasswordCredential> {
        Subject subject;
        ManagedConnectionFactory mcf;

        GetCredentialAction(Subject subject, ManagedConnectionFactory mcf) {
            this.subject = subject;
            this.mcf = mcf;
        }

        @Override
        public PasswordCredential run() {
            Set<PasswordCredential> creds = subject.getPrivateCredentials(PasswordCredential.class);
            PasswordCredential pwdc = null;
            Iterator<PasswordCredential> credentials = creds.iterator();
            while (credentials.hasNext()) {
                PasswordCredential curCred = credentials.next();
                if (curCred.getManagedConnectionFactory().equals(mcf)) {
                    pwdc = curCred;
                    break;
                }
            }
            return pwdc;
        }

        static PasswordCredential getCredential(Subject subject, ManagedConnectionFactory mcf) {
            GetCredentialAction action = new GetCredentialAction(subject, mcf);
            return AccessController.doPrivileged(action);
        }
    }
}
