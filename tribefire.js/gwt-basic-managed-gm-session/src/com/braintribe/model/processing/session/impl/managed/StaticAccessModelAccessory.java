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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;

public class StaticAccessModelAccessory implements ModelAccessory {

	private static final Logger log = Logger.getLogger(StaticAccessModelAccessory.class);

	protected final ManagedGmSession modelSession;
	protected final ModelOracle modelOracle;
	protected final CmdResolver cmdResolver;

	private String toStringValue;

	public StaticAccessModelAccessory(GmMetaModel metaModel, String accessId) {
		try {

			modelSession = new BasicManagedGmSession();
			metaModel = modelSession.merge().adoptUnexposed(false).doFor(metaModel);

			modelOracle = new BasicModelOracle(metaModel);

			ResolutionContextBuilder rcb = new ResolutionContextBuilder(modelOracle);
			rcb.setSessionProvider(() -> this);
			rcb.addStaticAspect(AccessAspect.class, accessId);
			cmdResolver = new CmdResolverImpl(rcb.build());

			toStringValue = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [modelName='" + metaModel.getName() + "']";

			log.debug("Built " + this);

		} catch (Exception e) {
			throw new GmSessionRuntimeException("Error while building model accessory. Error: " + e.getMessage(), e);
		}
	}

	@Override
	public CmdResolver getCmdResolver() {
		return cmdResolver;
	}

	@Override
	public ManagedGmSession getModelSession() {
		return modelSession;
	}

	@Override
	public GmMetaModel getModel() {
		return modelOracle.getGmMetaModel();
	}

	@Override
	public ModelOracle getOracle() {
		return modelOracle;
	}

	@Override
	public String toString() {
		return toStringValue;
	}

}
