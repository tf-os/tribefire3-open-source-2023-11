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
package tribefire.extension.demo.initializer.wire.contract;

import com.braintribe.model.extensiondeployment.check.HealthCheck;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.demo.model.deployment.AuditProcessor;
import tribefire.extension.demo.model.deployment.DemoAccess;
import tribefire.extension.demo.model.deployment.DemoApp;
import tribefire.extension.demo.model.deployment.DemoTransientSmoodAccess;
import tribefire.extension.demo.model.deployment.DepartmentRiskProcessor;
import tribefire.extension.demo.model.deployment.EntityMarshallingProcessor;
import tribefire.extension.demo.model.deployment.FindByTextProcessor;
import tribefire.extension.demo.model.deployment.GetEmployeesByGenderProcessor;
import tribefire.extension.demo.model.deployment.GetOrphanedEmployeesProcessor;
import tribefire.extension.demo.model.deployment.GetPersonsByNameProcessor;
import tribefire.extension.demo.model.deployment.NewEmployeeProcessor;
import tribefire.extension.demo.model.deployment.ResourceStreamingProcessor;
import tribefire.extension.demo.model.deployment.RevenueNotificationProcessor;
import tribefire.extension.demo.model.deployment.TestAccessProcessor;
import tribefire.extension.job_scheduling.deployment.model.JobScheduling;

/**
 * <p>
 * This {@link WireSpace Wire contract} exposes our custom instances (e.g.
 * instances of our deployables).
 * </p>
 */
public interface DemoInitializerContract extends WireSpace {

	DemoAccess demoAccess();

	DemoTransientSmoodAccess demoTransientSmoodAccess();

	RevenueNotificationProcessor revenueNotificationProcessor();

	DepartmentRiskProcessor departmentRiskProcessor();

	AuditProcessor auditProcessor();

	NewEmployeeProcessor newEmployeeProcessor();
	
	GetOrphanedEmployeesProcessor getOrphanedEmployeesProcessor();

	GetEmployeesByGenderProcessor getEmployeesByGenderProcessor();

	GetPersonsByNameProcessor getPersonsByNameProcessor();

	FindByTextProcessor findByTextProcessor();
	
	EntityMarshallingProcessor entityMarshallingProcessor();

	ResourceStreamingProcessor resourceStreamingProcessor();

	TestAccessProcessor testAccessProcessor();

	HealthCheck healthChecks();

	DemoApp demoApp();

	JobScheduling orphanedEmployeesJobScheduler();
	
	MetaData confidential();

	MetaData processWithGetOrphanedEmployees();
	
	MetaData processWithGetEmployeeByGender();

	MetaData processWithGetPersonsByName();

	MetaData processWithFindByText();
	
	MetaData processWithEntityMarshalling();

	MetaData processWithNewEmployee();

	MetaData processWithTestAccess();

	MetaData onChangeRevenue();

	MetaData onChangeProfitable();

	MetaData onChangeAudit();

	MetaData onCreateAudit();

	MetaData onDeleteAudit();

	MetaData dateClippingDay();

	MetaData processWithResourceStreaming();
	
	MetaData hidden();

}
