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

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.inflow.JmsActivation;
import org.jboss.resource.adapter.jms.inflow.JmsActivationSpec;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A generic resource adapter for any JMS server.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 */
public class JmsResourceAdapter implements ResourceAdapter {
    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(JmsResourceAdapter.class);

    /**
     * The bootstrap context
     */
    private BootstrapContext ctx;

    /**
     * The activations by activation spec
     */
    private ConcurrentHashMap<ActivationSpec, JmsActivation> activations = new ConcurrentHashMap<>();

    /**
     * Get the work manager
     *
     * @return the work manager
     */
    public WorkManager getWorkManager() {
        return ctx.getWorkManager();
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        JmsActivation activation = new JmsActivation(this, endpointFactory, (JmsActivationSpec) spec);
        activations.put(spec, activation);
        activation.start();
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        JmsActivation activation = activations.remove(spec);
        if (activation != null) {
            activation.stop();
        }
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        this.ctx = ctx;
    }

    @Override
    public void stop() {
        for (Iterator<Map.Entry<ActivationSpec, JmsActivation>> i = activations.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<ActivationSpec, JmsActivation> entry = i.next();
            try {
                JmsActivation activation = entry.getValue();
                if (activation != null) {
                    activation.stop();
                }
            } catch (Exception ignored) {
                log.debug("Ignored", ignored);
            }
            i.remove();
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        if (ctx != null) {
            hashCode += ctx.hashCode();
        }

        if (activations != null) {
            hashCode += activations.hashCode();
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        // TODO
        return false;
    }
}
