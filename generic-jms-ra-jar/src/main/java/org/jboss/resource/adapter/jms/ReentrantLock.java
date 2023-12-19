/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.resource.adapter.jms;

import java.util.Collection;

/**
 * ReentrantLock override
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ReentrantLock extends java.util.concurrent.locks.ReentrantLock {
    /**
     * Serial version uid
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param fair Fair locking
     */
    public ReentrantLock(boolean fair) {
        super(fair);
    }

    /**
     * {@inheritDoc}
     */
    public Thread getOwner() {
        return super.getOwner();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Thread> getQueuedThreads() {
        return super.getQueuedThreads();
    }
}
