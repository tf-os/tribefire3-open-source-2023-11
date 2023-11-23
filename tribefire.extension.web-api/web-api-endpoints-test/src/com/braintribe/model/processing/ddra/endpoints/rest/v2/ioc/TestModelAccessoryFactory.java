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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;

public class TestModelAccessoryFactory implements ModelAccessoryFactory, ModelAccessory {

	public static TestModelAccessoryFactory testModelAccessoryFactory(IncrementalAccess access) {
		return new TestModelAccessoryFactory(access.getMetaModel());
	}

	private final GmMetaModel model;

	private final ModelOracle modelOracle;

	private final CmdResolver cmdResolver;

	public TestModelAccessoryFactory(GmMetaModel model) {
		this.model = model;
		this.modelOracle = new BasicModelOracle(model);
		this.cmdResolver = new CmdResolverImpl(this.modelOracle);
	}

	@Override
	public ModelAccessory getForAccess(String accessId) {
		return this;
	}
	
	@Override
	public ModelAccessory getForServiceDomain(String serviceDomainId) {
		return this;
	}

	@Override
	public CmdResolver getCmdResolver() {
		return cmdResolver;
	}

	@Override
	public ManagedGmSession getModelSession() {
		throw new NotImplementedException();
	}

	@Override
	public GmMetaModel getModel() {
		return model;
	}

	@Override
	public ModelOracle getOracle() {
		return modelOracle;
	}
}
