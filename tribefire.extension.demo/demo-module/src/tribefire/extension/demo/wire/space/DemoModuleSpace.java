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
package tribefire.extension.demo.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.demo.model.deployment.AuditProcessor;
import tribefire.extension.demo.model.deployment.DemoAccess;
import tribefire.extension.demo.model.deployment.DemoApp;
import tribefire.extension.demo.model.deployment.DemoHealthCheckProcessor;
import tribefire.extension.demo.model.deployment.DemoTransientSmoodAccess;
import tribefire.extension.demo.model.deployment.DepartmentRiskProcessor;
import tribefire.extension.demo.model.deployment.EntityMarshallingProcessor;
import tribefire.extension.demo.model.deployment.FindByTextProcessor;
import tribefire.extension.demo.model.deployment.GetEmployeesByGenderProcessor;
import tribefire.extension.demo.model.deployment.GetOrphanedEmployeesProcessor;
import tribefire.extension.demo.model.deployment.GetPersonsByNameProcessor;
import tribefire.extension.demo.model.deployment.NewEmployeeProcessor;
import tribefire.extension.demo.model.deployment.OrphanedEmployeesWatchJob;
import tribefire.extension.demo.model.deployment.ResourceStreamingProcessor;
import tribefire.extension.demo.model.deployment.RevenueNotificationProcessor;
import tribefire.extension.demo.model.deployment.TestAccessProcessor;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;


/**
 * @author peter.gazdik
 */
@Managed
public class DemoModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private DemoDeployablesSpace deployables;
	
	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// @formatter:off
		bindings.bind(DemoAccess.T)
			.component(tfPlatform.binders().incrementalAccess())
			.expertFactory(deployables::demoAccess);
		
		bindings.bind(DemoTransientSmoodAccess.T)
			.component(tfPlatform.binders().incrementalAccess())
			.expertFactory(deployables::demoTransientSmoodAccess);
		
		bindings.bind(DepartmentRiskProcessor.T)
			.component(tfPlatform.binders().stateChangeProcessor())
			.expertSupplier(deployables::departmentRiskProcessor);
		
		bindings.bind(AuditProcessor.T)
			.component(tfPlatform.binders().stateChangeProcessor())
			.expertSupplier(deployables::auditProcessor);
		
		bindings.bind(GetEmployeesByGenderProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::getEmployeesByGenderProcessor);
		
		bindings.bind(GetOrphanedEmployeesProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::getOrphanedEmployeesProcessor);
		
		bindings.bind(GetPersonsByNameProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::getPersonsByNameProcessor);
	
		bindings.bind(DemoApp.T)
			.component(tfPlatform.binders().webTerminal())
			.expertFactory(deployables::listUserApp);
		
		bindings.bind(FindByTextProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::findByTextProcessor);
		
		bindings.bind(EntityMarshallingProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertSupplier(deployables::entityMarshallingProcessor);
		
		bindings.bind(TestAccessProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertFactory(deployables::testAccessProcessor);
		
		bindings.bind(OrphanedEmployeesWatchJob.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::orphanedEmployeesWatcher);
		
		bindings.bind(NewEmployeeProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::newEmployeeProcessor);
		
		bindings.bind(ResourceStreamingProcessor.T)
			.component(tfPlatform.binders().accessRequestProcessor())
			.expertSupplier(deployables::resourceStreamingProcessor);
		
		bindings.bind(RevenueNotificationProcessor.T)
			.component(tfPlatform.binders().stateChangeProcessor())
			.expertFactory(deployables::revenueNotificationProcessor);
		
		bindings.bind(DemoHealthCheckProcessor.T)
			.component(tfPlatform.binders().checkProcessor())
			.expertSupplier(deployables::demoHealthCheckProcessor);
		// @formatter:on
	}
}
