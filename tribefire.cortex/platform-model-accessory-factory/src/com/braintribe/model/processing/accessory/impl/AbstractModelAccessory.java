// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.accessory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.api.PurgableModelAccessory;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.provider.Holder;

public abstract class AbstractModelAccessory implements PurgableModelAccessory {

	// constants
	private static final Logger log = Logger.getLogger(AbstractModelAccessory.class);

	// configurable
	private Supplier<?> sessionProvider;
	protected Supplier<PersistenceGmSession> cortexSessionProvider;

	// post-initialized
	protected ManagedGmSession modelSession;
	protected ModelOracle modelOracle;
	protected CmdResolver cmdResolver;
	private final Object buildMonitor = new Object();
	private final Object updateMonitor = new Object();
	private volatile boolean isUpToDate = false; // when false, access to this accessory requires a build() call.
	private final List<ModelAccessoryListener> listeners = Collections.synchronizedList(new ArrayList<>());

	public AbstractModelAccessory() {
	}

	@Configurable
	public void setSessionProvider(Supplier<?> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Required
	@Configurable
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}

	public Supplier<?> getSessionProvider() {
		if (sessionProvider == null) {
			this.sessionProvider = new Holder<Object>(new Object());
		}
		return sessionProvider;
	}

	@Override
	public CmdResolver getCmdResolver() {
		ensureInitialized();
		return cmdResolver;
	}

	@Override
	public ManagedGmSession getModelSession() {
		ensureInitialized();
		return modelSession;
	}

	@Override
	public ModelOracle getOracle() {
		ensureInitialized();
		return modelOracle;
	}

	@Override
	public ModelMdResolver getMetaData() {
		return getCmdResolver().getMetaData();
	}

	@Override
	public GmMetaModel getModel() {
		return getOracle().getGmMetaModel();
	}

	@Override
	public boolean isUpToDate() {
		return isUpToDate; // when false, access to this accessory requires a build() call.
	}

	@Override
	public void addListener(ModelAccessoryListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ModelAccessoryListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void outdated() {
		synchronized (updateMonitor) {
			if (!isUpToDate)
				return;

			this.isUpToDate = false;
		}

		log.debug(() -> "Outdating " + this);

		// THIS WAY WE MIGHT NOTIFY A LISTENER AFTER IT WAS REMOVED!!! WE SHOULD AT LEAST DOCUMET THIS

		ModelAccessoryListener[] accessoryListeners = listeners.toArray(new ModelAccessoryListener[0]);

		for (ModelAccessoryListener listener : accessoryListeners)
			listener.onOutdated();
	}

	protected abstract void build();

	protected void ensureInitialized() {
		if (!isUpToDate) {
			synchronized (buildMonitor) {
				if (!isUpToDate) {
					log.trace(() -> "Ensuring initialization for " + this);
					this.build();
					isUpToDate = true;
					log.debug(() -> "Ensured initialization for " + this);
				}
			}
		}
	}

	/**
	 * @deprecated no reason for this method to be here. Probably not used anyway.
	 */
	@Deprecated
	protected TraversingCriterion modelTc() {
		throw new UnsupportedOperationException("Method 'AbstractModelAccessory.modelTc' is not supported!");
	}

}