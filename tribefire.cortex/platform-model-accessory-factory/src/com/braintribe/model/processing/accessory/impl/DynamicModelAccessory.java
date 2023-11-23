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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryListener;

/**
 * @author peter.gazdik
 */
public class DynamicModelAccessory implements ModelAccessory, ModelAccessoryListener {

	private final Supplier<ModelAccessory> modelAccessorySupplier;

	private final Object lock = new String("DynamicModelAccessory");
	private final List<ModelAccessoryListener> listeners = Collections.synchronizedList(newList());

	private volatile ModelAccessory delegate;

	public DynamicModelAccessory(Supplier<ModelAccessory> modelAccessorySupplier) {
		this.modelAccessorySupplier = modelAccessorySupplier;
	}

	@Override
	public void onOutdated() {
		synchronized (lock) {
			ModelAccessory newModelAccessory = modelAccessorySupplier.get();

			if (delegate != newModelAccessory) {
				if (delegate != null)
					delegate.removeListener(this);

				delegate = newModelAccessory;
				delegate.addListener(this);
			}
		}

		ModelAccessoryListener[] listenersCopy = listeners.toArray(new ModelAccessoryListener[0]);

		for (ModelAccessoryListener listener : listenersCopy)
			listener.onOutdated();
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
	public CmdResolver getCmdResolver() {
		return getDelegate().getCmdResolver();
	}

	@Override
	public ManagedGmSession getModelSession() {
		return getDelegate().getModelSession();
	}

	@Override
	public GmMetaModel getModel() {
		return getDelegate().getModel();
	}

	@Override
	public ModelOracle getOracle() {
		return getDelegate().getOracle();
	}

	private ModelAccessory getDelegate() {
		if (delegate == null)
			synchronized (lock) {
				if (delegate == null) {
					delegate = modelAccessorySupplier.get();
					delegate.addListener(this);
				}

			}

		return delegate;
	}

}
