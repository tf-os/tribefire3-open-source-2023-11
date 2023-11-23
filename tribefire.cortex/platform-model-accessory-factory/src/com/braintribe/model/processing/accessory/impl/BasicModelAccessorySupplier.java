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

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessorySupplier;
import com.braintribe.model.processing.session.api.managed.ModelChangeListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Standard {@link ModelAccessorySupplier} implementation which is also a {@link ModelChangeListener}.
 * 
 * @author dirk.scheffler
 */
public class BasicModelAccessorySupplier implements ModelAccessorySupplier, ModelChangeListener {

	// constants
	private static final Logger log = Logger.getLogger(BasicModelAccessorySupplier.class);

	// configurable
	protected Supplier<PersistenceGmSession> cortexSessionProvider;

	// cached
	private final MaCache maCache = new MaCache(this::createAccessory, "modelName");

	private Consumer<CmdResolverBuilder> cmdInitializer;

	@Required
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionProvider = cortexSessionSupplier;
	}

	@Configurable
	public void setCmdInitializer(Consumer<CmdResolverBuilder> cmdInitializer) {
		this.cmdInitializer = cmdInitializer;
	}
	
	@Configurable
	public void setCacheModelAccessories(boolean cacheModelAccessories) {
		maCache.setCacheModelAccessories(cacheModelAccessories);
	}

	@Override
	public ModelAccessory getForModel(String modelName) {
		return maCache.getModelAccessory(modelName, null);
	}

	@Override
	public void onModelChange(String modelName) {
		maCache.onChange(modelName);
	}

	private BasicModelAccessory createAccessory(String modelName, String perspective) {
		checkPerspectiveNull(perspective);
		BasicModelAccessory modelAccessory = new BasicModelAccessory();
		modelAccessory.setCortexSessionProvider(cortexSessionProvider);
		modelAccessory.setCmdInitializer(cmdInitializer);
		modelAccessory.setModelName(modelName);

		log.debug(() -> "Created " + modelAccessory);

		return modelAccessory;

	}

	public static void checkPerspectiveNull(String perspective) {
		if (perspective != null)
			// should be unreachable.
			throw new IllegalArgumentException("This class does not support model perspectives. Perspective: " + perspective);
	}

}
