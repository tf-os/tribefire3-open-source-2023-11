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
package com.braintribe.model.processing.test.impl.session;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.AbstractModelAccessory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

/**
 * 
 */
public class TestModelAccessory extends AbstractModelAccessory {

	private BasicManagedGmSession modelSession;
	private Supplier<Set<String>> userRolesProvider;
	private final Supplier<?> sessionProvider = () -> UUID.randomUUID().toString();

	public TestModelAccessory(ModelOracle modelOracle) {
		this.modelOracle = modelOracle;
	}

	/** @deprecated use {@link #TestModelAccessory(CmdResolver)} instead */
	@Deprecated
	public TestModelAccessory(ModelOracle modelOracle, CmdResolver cmdResolver) {
		this.modelOracle = modelOracle;
		this.cmdResolver = cmdResolver;
	}

	/** Please do not delete! */
	public TestModelAccessory(CmdResolver cmdResolver) {
		this.modelOracle = cmdResolver.getModelOracle();
		this.cmdResolver = cmdResolver;
	}

	@Override
	public ManagedGmSession getModelSession() {
		return modelSession;
	}

	public synchronized void build() {
		try {

			if (modelSession == null) {

				modelSession = new BasicManagedGmSession();
				// this just attaches the whole meta model to the session
				modelSession.merge().adoptUnexposed(true).doFor(modelOracle.getGmMetaModel());

				if (cmdResolver == null) {
					ResolutionContextBuilder contextBuilder = new ResolutionContextBuilder(modelOracle);
					contextBuilder.addDynamicAspectProvider(RoleAspect.class, userRolesProvider);
					contextBuilder.setSessionProvider(sessionProvider);

					cmdResolver = new CmdResolverImpl(contextBuilder.build());
				}

			}

		} catch (Exception e) {
			throw new GmSessionRuntimeException("Error while building model accessory. Error: " + e.getMessage(), e);
		}
	}

}
