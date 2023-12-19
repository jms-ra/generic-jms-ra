/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.resource.adapter.jms.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;

public class TransactionUtils {

    private static final String TRANSACTION_SYNCHRONIZATION_REGISTRY_LOOKUP = "java:comp/TransactionSynchronizationRegistry";

    // Cached reference to the transaction sync registry to determine if a
    // transaction is active
    private static volatile transient TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    /**
     * check whether there is an active transaction.
     */
    public static boolean isInTransaction() {
        TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();
        boolean inTx = tsr.getTransactionStatus() == Status.STATUS_ACTIVE;
        return inTx;
    }

    /**
     * lookup the transactionSynchronizationRegistry and cache it.
     */
    private static TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        TransactionSynchronizationRegistry cachedTSR = transactionSynchronizationRegistry;
        if (cachedTSR == null) {
            synchronized (TransactionUtils.class) {
                cachedTSR = transactionSynchronizationRegistry;
                if (cachedTSR == null) {
                    cachedTSR = (TransactionSynchronizationRegistry) lookup(
                            TRANSACTION_SYNCHRONIZATION_REGISTRY_LOOKUP);
                    transactionSynchronizationRegistry = cachedTSR;
                }
            }
        }
        return cachedTSR;
    }

    private static Object lookup(String name) {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            return ctx.lookup(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
            }
        }
    }

}
