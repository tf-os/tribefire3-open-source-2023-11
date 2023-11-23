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

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessorySupplier;

/**
 * An {@link AbstractModelAccessory} which obtain its {@link #getModelSession()} and {@link #getOracle()} from a parent {@link ModelAccessory} as
 * supplied by a {@link ModelAccessorySupplier}.
 * 
 * @author dirk.scheffler
 */
public abstract class AbstractDerivingModelAccessory extends AbstractModelAccessory {

	// constants
	private static final Logger log = Logger.getLogger(AbstractDerivingModelAccessory.class);

	// configurable
	private ModelAccessorySupplier modelAccessorySupplier;
	private Supplier<Set<String>> userRolesProvider;

	// cached
	private ModelAccessory parentAccessory;
	private final String parentLoadMonitor = new String("model-accessory-parent-lock");

	@Required
	@Configurable
	public void setModelAccessorySupplier(ModelAccessorySupplier modelAccessorySupplier) {
		this.modelAccessorySupplier = modelAccessorySupplier;
	}

	@Override
	@Required
	@Configurable
	public void setSessionProvider(Supplier<?> sessionProvider) {
		super.setSessionProvider(sessionProvider);
	}

	@Required
	@Configurable
	public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) {
		this.userRolesProvider = userRolesProvider;
	}

	@Override
	protected void build() {
		ModelAccessory parentAccessory = getParentAccessory();

		modelSession = parentAccessory.getModelSession();
		modelOracle = parentAccessory.getOracle();

		ResolutionContextBuilder rcb = new ResolutionContextBuilder(modelOracle);
		rcb.addDynamicAspectProvider(RoleAspect.class, userRolesProvider);
		rcb.setSessionProvider(getSessionProvider());

		initializeContextBuilder(rcb);

		cmdResolver = new CmdResolverImpl(rcb.build());

		log.debug(() -> "Built " + this);
	}

	protected void initializeContextBuilder(@SuppressWarnings("unused") ResolutionContextBuilder rcb) {
		// To be optionally overridden by implementations
	}

	public ModelAccessory getParentAccessory() {
		if (parentAccessory == null)
			synchronized (parentLoadMonitor) {
				if (parentAccessory == null) {
					parentAccessory = modelAccessorySupplier.getForModel(getModelName());
					parentAccessory.addListener(this::onOutdated);
				}
			}

		return parentAccessory;
	}

	private void onOutdated() {
		log.debug(() -> "Notifying onOutdated from parent: " + parentAccessory + " to " + AbstractDerivingModelAccessory.this);
		outdated();
	}

	protected abstract String getModelName();

}
