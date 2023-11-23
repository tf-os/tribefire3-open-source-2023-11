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
package tribefire.cortex.qa_other.wire.space;

import static tribefire.cortex.qa.CortexQaCommons.createServiceProcessor;
import static tribefire.cortex.qa.CortexQaCommons.otherModuleName;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.model.MainAndOtherSerp;
import tribefire.cortex.qa.CortexQaCommons;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.ServiceBindersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class QaOtherModuleSpace implements TribefireModuleContract {

	@Import
	private TribefirePlatformContract tfPlatform;


	//
	// Initializers
	//

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(this::createServiceProcessors);
	}

	private void createServiceProcessors(PersistenceInitializationContext context) {
		ManagedGmSession session = context.getSession();

		createServiceProcessor(session, MainAndOtherSerp.T, otherModuleName);
	}

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		ServiceBindersContract binders = tfPlatform.binders();

		bindings.bind(MainAndOtherSerp.T).component(binders.serviceProcessor())
				.expertSupplier(CortexQaCommons.qaServiceExpertSupplier(MainAndOtherSerp.T));
	}

}
