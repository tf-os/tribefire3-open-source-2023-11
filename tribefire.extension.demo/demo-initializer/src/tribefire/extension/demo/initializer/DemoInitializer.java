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
package tribefire.extension.demo.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.demo.initializer.wire.DemoInitializerWireModule;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerContract;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerMainContract;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerModelsContract;

/**
 * <p>
 * This {@link AbstractInitializer initializer} initializes targeted accesses
 * with our custom instances available from initializer's contracts.
 * </p>
 */
public class DemoInitializer extends AbstractInitializer<DemoInitializerMainContract> {

	@Override
	protected WireTerminalModule<DemoInitializerMainContract> getInitializerWireModule() {
		return DemoInitializerWireModule.INSTANCE;
	}

	@Override
	protected void initialize(PersistenceInitializationContext context,
			WiredInitializerContext<DemoInitializerMainContract> initializerContext,
			DemoInitializerMainContract initializerMainContract) {

		DemoInitializerModelsContract models = initializerMainContract.initializerModelsContract();
		CoreInstancesContract coreInstances = initializerMainContract.coreInstancesContract();

		GmMetaModel cortexModel = coreInstances.cortexModel();
		GmMetaModel cortexServiceModel = coreInstances.cortexServiceModel();

		cortexModel.getDependencies().add(models.configuredDemoDeploymentModel());
		cortexServiceModel.getDependencies().add(models.configuredDemoServiceModel());
		cortexServiceModel.getDependencies().add(models.configuredDemoCortexServiceModel());

		models.demoWorkbenchModel();

		DemoInitializerContract initializer = initializerMainContract.initializerContract();

		initializer.demoAccess();
		initializer.demoTransientSmoodAccess();
		initializer.revenueNotificationProcessor();
		initializer.departmentRiskProcessor();
		initializer.auditProcessor();
		initializer.newEmployeeProcessor();
		initializer.getOrphanedEmployeesProcessor();
		initializer.getEmployeesByGenderProcessor();
		initializer.getPersonsByNameProcessor();
		initializer.findByTextProcessor();
		initializer.resourceStreamingProcessor();
		initializer.testAccessProcessor();
		initializer.healthChecks();
		initializer.demoApp();
		initializer.orphanedEmployeesJobScheduler();

		initializerMainContract.metaData();
	}
}
