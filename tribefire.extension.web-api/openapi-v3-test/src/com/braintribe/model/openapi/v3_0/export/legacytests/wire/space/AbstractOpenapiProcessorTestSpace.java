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
package com.braintribe.model.openapi.v3_0.export.legacytests.wire.space;

import java.util.function.Supplier;

import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.openapi.v3_0.export.legacytests.ioc.TestAccessSpace;
import com.braintribe.model.openapi.v3_0.export.legacytests.ioc.TestSessionFactory;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.tools.gm.session.TestModelAccessoryFactory;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public abstract class AbstractOpenapiProcessorTestSpace implements WireSpace {
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		TestAccessSpace.cortexAccess();
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServicesBase);
	}

	protected void configureServicesBase(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth");
		configureServices(bean);
	}

	protected abstract void configureServices(ConfigurableDispatchingServiceProcessor bean);

	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}

	protected ModelAccessoryFactory modelAccessoryFactory() {
		TestModelAccessoryFactory bean = new TestModelAccessoryFactory();

		bean.registerAccessModelAccessory("cortex", TestAccessSpace.cortexModel);
		bean.registerAccessModelAccessory("test.access", TestAccessSpace.testServiceModel);
		bean.registerServiceModelAccessory("test.access", TestAccessSpace.testServiceModel);
		bean.registerServiceModelAccessory("test.domain1", TestAccessSpace.testModel);
		bean.registerServiceModelAccessory("test.domain2", TestAccessSpace.testModel);

		return bean;
	}

	protected Supplier<PersistenceGmSession> cortexSessionSupplier() {
		return () -> sessionFactory().newSession("cortex");
	}

	protected PersistenceGmSessionFactory sessionFactory() {
		TestSessionFactory bean = new TestSessionFactory();
		bean.addAccess(TestAccessSpace.cortexAccess);
		return bean;
	}
}
