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
package com.braintribe.testing.tools.gm.session;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.impl.managed.AbstractModelAccessory;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

/**
 * Simple {@link ModelAccessory} which can be used in tests.
 */
public class TestModelAccessory extends AbstractModelAccessory {

	private static final Supplier<?> sessionProvider = Object::new;

	private BasicManagedGmSession modelSession;
	private GmMetaModel metaModel;
	private Supplier<Set<String>> userRolesProvider;

	public static TestModelAccessory newModelAccessory(GmMetaModel metaModel) {
		return new TestModelAccessory(metaModel).build();
	}

	public TestModelAccessory(GmMetaModel metaModel) {
		this.metaModel = metaModel;
	}

	// do not delete!
	public TestModelAccessory(CmdResolver cmdResolver) {
		this.cmdResolver = cmdResolver;
	}

	@Override
	public ManagedGmSession getModelSession() {
		return modelSession;
	}

	@Override
	public CmdResolver getCmdResolver() {
		return cmdResolver;
	}

	public synchronized TestModelAccessory build() {
		if (modelSession != null)
			return this;

		try {
			return buildHelper();

		} catch (Exception e) {
			throw new GmSessionRuntimeException("Error while building model accessory. Error: " + e.getMessage(), e);
		}
	}

	private TestModelAccessory buildHelper() {
		modelSession = new BasicManagedGmSession();
		metaModel = modelSession.merge().adoptUnexposed(true).doFor(metaModel);
		modelOracle = new BasicModelOracle(metaModel);

		ResolutionContextBuilder contextBuilder = new ResolutionContextBuilder(modelOracle) //
				.addDynamicAspectProvider(RoleAspect.class, userRolesProvider) //
				.addStaticAspect(AccessTypeAspect.class, SmoodAccess.class.getName()) //
				.setSessionProvider(sessionProvider);

		cmdResolver = new CmdResolverImpl(contextBuilder.build());

		return this;
	}

	@Override
	public GmMetaModel getModel() {
		return this.metaModel;
	}
}
