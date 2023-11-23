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
package tribefire.extension.demo.initializer.wire.space;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.check.HealthCheck;
import com.braintribe.model.extensiondeployment.meta.OnChange;
import com.braintribe.model.extensiondeployment.meta.OnCreate;
import com.braintribe.model.extensiondeployment.meta.OnDelete;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.DateClipping;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.deployables.initializer.support.wire.contract.DefaultDeployablesContract;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerContract;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerModelsContract;
import tribefire.extension.demo.initializer.wire.contract.ExistingInstancesContract;
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
import tribefire.extension.job_scheduling.deployment.model.JobCronScheduling;
import tribefire.extension.job_scheduling.deployment.model.JobScheduling;

/**
 * @see DemoInitializerContract
 */
@Managed
public class DemoInitializerSpace extends AbstractInitializerSpace implements DemoInitializerContract {

	@Import
	private DefaultDeployablesContract defaultDeployables;

	@Import
	private DemoInitializerModelsContract models;

	@Import
	private CoreInstancesContract coreInstances;
	
	@Import
	private ExistingInstancesContract existingInstances;

	// == Deployables == //

	@Managed
	@Override
	public DemoAccess demoAccess() {
		DemoAccess bean = create(DemoAccess.T);
		
		bean.setName("Demo Access");
		bean.setExternalId("access.demo");
		bean.setMetaModel(models.configuredDemoModel());
		bean.setServiceModel(models.configuredDemoServiceModel());
		bean.setWorkbenchAccess(demoWorkbenchAccess());
		bean.setAspectConfiguration(aspectConfiguration());

		return bean;
	}

	@Managed
	@Override
	public DemoTransientSmoodAccess demoTransientSmoodAccess() {
		DemoTransientSmoodAccess bean = create(DemoTransientSmoodAccess.T);
		
		bean.setExternalId("access.demoTransientSmoodAccess");
		bean.setName("Demo Transient Smood Access");
		bean.setMetaModel(models.configuredDemoModel());

		return bean;
	}

	@Managed
	private AspectConfiguration aspectConfiguration() {
		AspectConfiguration bean = create(AspectConfiguration.T);
		bean.setAspects(demoAspects());

		return bean;
	}

	private List<AccessAspect> demoAspects() {
		List<AccessAspect> aspects = new ArrayList<>();
		aspects.addAll(defaultDeployables.defaultAspects());

		return aspects;
	}

	@Managed
	private CollaborativeSmoodAccess demoWorkbenchAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		
		bean.setExternalId("access.demo.wb");
		bean.setName("Demo Workbench Access");
		bean.setMetaModel(models.demoWorkbenchModel());
		bean.setWorkbenchAccess(coreInstances.workbenchAccess());

		return bean;
	}

	@Managed
	@Override
	public RevenueNotificationProcessor revenueNotificationProcessor() {
		RevenueNotificationProcessor bean = create(RevenueNotificationProcessor.T);
		
		bean.setExternalId("statechangeProcessor.revenueNotification");
		bean.setName("Revenue Notification Processor");

		return bean;
	}

	@Managed
	@Override
	public DepartmentRiskProcessor departmentRiskProcessor() {
		DepartmentRiskProcessor bean = create(DepartmentRiskProcessor.T);
		
		bean.setExternalId("statechangeProcessor.departmentRisk");
		bean.setName("Department Risk Processor");

		return bean;
	}

	@Managed
	@Override
	public AuditProcessor auditProcessor() {
		AuditProcessor bean = create(AuditProcessor.T);
		
		bean.setExternalId("statechangeProcessor.audit");
		bean.setName("Audit Processor");

		return bean;
	}

	@Managed
	@Override
	public NewEmployeeProcessor newEmployeeProcessor() {
		NewEmployeeProcessor bean = create(NewEmployeeProcessor.T);
		
		bean.setExternalId("serviceProcessor.newEmployee");
		bean.setName("New Employee Action Processor");

		return bean;
	}
	
	@Managed
	@Override
	public GetOrphanedEmployeesProcessor getOrphanedEmployeesProcessor() {
		GetOrphanedEmployeesProcessor bean = create(GetOrphanedEmployeesProcessor.T);
		
		bean.setExternalId("serviceProcessor.getOrphanedEmployees");
		bean.setName("Get Orphaned Employees Processor");

		return bean;
	}

	@Managed
	@Override
	public GetEmployeesByGenderProcessor getEmployeesByGenderProcessor() {
		GetEmployeesByGenderProcessor bean = create(GetEmployeesByGenderProcessor.T);
		
		bean.setExternalId("serviceProcessor.getEmployeeByGender");
		bean.setName("Get Employee By Gender Processor");

		return bean;
	}

	@Managed
	@Override
	public GetPersonsByNameProcessor getPersonsByNameProcessor() {
		GetPersonsByNameProcessor bean = create(GetPersonsByNameProcessor.T);

		bean.setExternalId("serviceProcessor.getPersonsByName");
		bean.setName("Get Persons By Name Processor");

		return bean;
	}

	@Managed
	@Override
	public FindByTextProcessor findByTextProcessor() {
		FindByTextProcessor bean = create(FindByTextProcessor.T);
		
		bean.setExternalId("serviceProcessor.findByText");
		bean.setName("Find By Text Processor");

		return bean;
	}
	
	@Managed
	@Override
	public EntityMarshallingProcessor entityMarshallingProcessor() {
		EntityMarshallingProcessor bean = create(EntityMarshallingProcessor.T);
		
		bean.setExternalId("serviceProcessor.entityMarshalling");
		bean.setName("Entity Marshalling Processor");

		return bean;
	}

	@Managed
	@Override
	public ResourceStreamingProcessor resourceStreamingProcessor() {
		ResourceStreamingProcessor bean = create(ResourceStreamingProcessor.T);
		
		bean.setExternalId("serviceProcessor.resource.streaming");
		bean.setName("Resource Streaming Processor");

		return bean;
	}

	@Managed
	@Override
	public TestAccessProcessor testAccessProcessor() {
		TestAccessProcessor bean = create(TestAccessProcessor.T);
		
		bean.setExternalId("serviceProcessor.testAccess");
		bean.setName("Test Access Processor");

		return bean;
	}

	@Managed
	@Override
	public DemoApp demoApp() {
		DemoApp bean = create(DemoApp.T);
		
		bean.setAccess(demoAccess());
		bean.setExternalId("app.demo");
		bean.setName("Demo App");
		bean.setPathIdentifier("app.demo");
		bean.setUser("cortex");
		bean.setPassword("cortex");
		
		return bean;
	}

	@Managed
	@Override
	public JobScheduling orphanedEmployeesJobScheduler() {
		JobCronScheduling bean = create(JobCronScheduling.T);
		
		bean.setJobRequestProcessor(orphanedEmployeesWatchJob());
		bean.setCronExpression("0/10 0/1 * 1/1 * ? *");
		bean.setCoalescing(true);
		bean.setName("Orphaned Employees Job Scheduler");
		bean.setExternalId("scheduling.orphaned.employees");

		return bean;
	}

	@Managed
	private OrphanedEmployeesWatchJob orphanedEmployeesWatchJob() {
		OrphanedEmployeesWatchJob bean = create(OrphanedEmployeesWatchJob.T);
		
		bean.setExternalId("job.watch.orphaned.employees");
		bean.setName("Orphaned Employees Watch Job");
		bean.setAccess(demoAccess());

		return bean;
	}

	@Managed
	@Override
	public HealthCheck healthChecks() {
		HealthCheck bean = create(HealthCheck.T);
		
		// TODO module-compatible?
		bean.getChecks().add(demoHealthCheckProcessor());

		return bean;
	}

	@Managed
	private DemoHealthCheckProcessor demoHealthCheckProcessor() {
		DemoHealthCheckProcessor bean = create(DemoHealthCheckProcessor.T);
		
		bean.setExternalId("serviceProcessor.demoHealthCheckProcessor");
		bean.setName("Demo Health Check Processor");

		return bean;
	}

	// == Model metadata == //

	@Managed
	@Override
	public MetaData confidential() {
		return create(Confidential.T);
	}
	
	@Managed
	@Override
	public MetaData processWithGetOrphanedEmployees() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(getOrphanedEmployeesProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData processWithGetEmployeeByGender() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(getEmployeesByGenderProcessor());

		return bean;
	}

	@Managed
	@Override
	public MetaData processWithGetPersonsByName() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(getPersonsByNameProcessor());

		return bean;
	}

	@Managed
	@Override
	public MetaData processWithFindByText() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(findByTextProcessor());
		
		return bean;
	}
	
	@Managed
	@Override
	public MetaData processWithEntityMarshalling() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(entityMarshallingProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData processWithNewEmployee() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(newEmployeeProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData processWithTestAccess() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(testAccessProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData onChangeRevenue() {
		OnChange bean = create(OnChange.T);
		
		bean.setProcessor(revenueNotificationProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData onChangeProfitable() {
		OnChange bean = create(OnChange.T);
		
		bean.setProcessor(departmentRiskProcessor());
		
		return bean;
	}
	
	@Managed
	@Override
	public MetaData onChangeAudit() {
		OnChange bean = create(OnChange.T);
		
		bean.setProcessor(auditProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData onCreateAudit() {
		OnCreate bean = create(OnCreate.T);
		
		bean.setProcessor(auditProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData onDeleteAudit() {
		OnDelete bean = create(OnDelete.T);
		
		bean.setProcessor(auditProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData dateClippingDay() {
		DateClipping bean = create(DateClipping.T);
		
		bean.setLower(DateOffsetUnit.day);
		bean.setUpper(null);
		
		return bean;
	}

	@Managed
	@Override
	public MetaData processWithResourceStreaming() {
		ProcessWith bean = create(ProcessWith.T);
		
		bean.setProcessor(resourceStreamingProcessor());
		
		return bean;
	}

	@Managed
	@Override
	public MetaData hidden() {
		Hidden bean = create(Hidden.T);
		
		bean.setInherited(false);
		
		return bean;
	}
}
