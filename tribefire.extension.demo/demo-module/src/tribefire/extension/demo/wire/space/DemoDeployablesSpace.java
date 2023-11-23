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

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.EmptyNonIncrementalAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.demo.model.deployment.DemoTransientSmoodAccess;
import tribefire.extension.demo.processing.AuditProcessor;
import tribefire.extension.demo.processing.DemoAccess;
import tribefire.extension.demo.processing.DemoApp;
import tribefire.extension.demo.processing.DemoHealthCheckProcessor;
import tribefire.extension.demo.processing.DepartmentRiskProcessor;
import tribefire.extension.demo.processing.EntityMarshallingProcessor;
import tribefire.extension.demo.processing.FindByTextProcessor;
import tribefire.extension.demo.processing.GetEmployeesByGenderProcessor;
import tribefire.extension.demo.processing.GetOrphanedEmployeesProcessor;
import tribefire.extension.demo.processing.GetPersonsByNameProcessor;
import tribefire.extension.demo.processing.NewEmployeeProcessor;
import tribefire.extension.demo.processing.OrphanedEmployeesWatchJob;
import tribefire.extension.demo.processing.ResourceStreamingProcessor;
import tribefire.extension.demo.processing.RevenueNotificationProcessor;
import tribefire.extension.demo.processing.TestAccessProcessor;
import tribefire.extension.demo.processing.tools.DemoPopulationBuilder;
import tribefire.extension.job_scheduling.api.api.JobRequest;
import tribefire.extension.job_scheduling.api.api.JobResponse;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 * This space class hosts configuration of deployables based on their denotation types.
 */
@Managed
public class DemoDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	// == State Change Processors == //

	@Managed
	public RevenueNotificationProcessor revenueNotificationProcessor(
			ExpertContext<tribefire.extension.demo.model.deployment.RevenueNotificationProcessor> context) {
		tribefire.extension.demo.model.deployment.RevenueNotificationProcessor deployable = context.getDeployable();

		RevenueNotificationProcessor bean = new RevenueNotificationProcessor();
		bean.setMaxRevenue(deployable.getMaxRevenue());
		bean.setMinRevenue(deployable.getMinRevenue());

		return bean;
	}

	@Managed
	public DepartmentRiskProcessor departmentRiskProcessor() {
		return new DepartmentRiskProcessor();
	}

	@Managed
	public AuditProcessor auditProcessor() {
		return new AuditProcessor();
	}

	// == Accesses == //

	@Managed
	public DemoAccess demoAccess(ExpertContext<tribefire.extension.demo.model.deployment.DemoAccess> context) {
		tribefire.extension.demo.model.deployment.DemoAccess deployable = context.getDeployable();

		DemoAccess bean = new DemoAccess();
		bean.setMetaModelProvider(deployable::getMetaModel);
		bean.setAccessId(deployable.getExternalId());

		return bean;
	}

	/**
	 * Deploy a {@link SmoodAccess} with a "transient" data delegate (so no initial population and no changes are stored), and then we initialize the
	 * underlying Smood with data from the {@link DemoPopulationBuilder}.
	 */
	@Managed
	public SmoodAccess demoTransientSmoodAccess(ExpertContext<DemoTransientSmoodAccess> context) {
		DemoTransientSmoodAccess deployable = context.getDeployable();

		SmoodAccess bean = new SmoodAccess();
		bean.setReadWriteLock(new ReentrantReadWriteLock());
		bean.setAccessId(deployable.getExternalId());
		EmptyNonIncrementalAccess dataDelegate = new EmptyNonIncrementalAccess(deployable.getMetaModel());
		bean.setDataDelegate(dataDelegate);

		Collection<GenericEntity> population = DemoPopulationBuilder.newInstance().noIdGenerator().build();

		Smood smood = bean.getDatabase();
		smood.initialize(population);

		return bean;
	}

	// == Apps == //

	@Managed
	public DemoApp listUserApp(ExpertContext<tribefire.extension.demo.model.deployment.DemoApp> context) {
		tribefire.extension.demo.model.deployment.DemoApp deployable = context.getDeployable();

		DemoApp bean = new DemoApp();
		bean.setAccessId(deployable.getAccess().getExternalId());
		bean.setUser(deployable.getUser());
		bean.setPassword(deployable.getPassword());

		return bean;
	}

	// == Service Processors == //

	@Managed
	public NewEmployeeProcessor newEmployeeProcessor() {
		return new NewEmployeeProcessor();
	}

	@Managed
	public GetEmployeesByGenderProcessor getEmployeesByGenderProcessor() {
		return new GetEmployeesByGenderProcessor();
	}

	@Managed
	public GetOrphanedEmployeesProcessor getOrphanedEmployeesProcessor() {
		return new GetOrphanedEmployeesProcessor();
	}

	@Managed
	public GetPersonsByNameProcessor getPersonsByNameProcessor() {
		return new GetPersonsByNameProcessor();
	}

	@Managed
	public FindByTextProcessor findByTextProcessor() {
		return new FindByTextProcessor();
	}

	@Managed
	public EntityMarshallingProcessor entityMarshallingProcessor() {
		EntityMarshallingProcessor bean = new EntityMarshallingProcessor();
		bean.setJsonMarshaller(tfPlatform.marshalling().jsonMarshaller());
		bean.setXmlMarshaller(tfPlatform.marshalling().xmlMarshaller());
		bean.setYamlMarshaller(tfPlatform.marshalling().yamlMarshaller());

		return bean;
	}

	@Managed
	public TestAccessProcessor testAccessProcessor(ExpertContext<tribefire.extension.demo.model.deployment.TestAccessProcessor> context) {
		tribefire.extension.demo.model.deployment.TestAccessProcessor deployable = context.getDeployable();

		TestAccessProcessor bean = new TestAccessProcessor();
		String defaultQueryString = deployable.getDefaultQuery();
		if (defaultQueryString == null) {
			throw new DeploymentException("No default query configured.");
		}
		ParsedQuery parsedQuery = QueryParser.parse(defaultQueryString);
		if (!parsedQuery.getIsValidQuery()) {
			throw new DeploymentException("Could not parse default query: " + defaultQueryString);
		}
		bean.setDefaultQuery(parsedQuery.getQuery());
		bean.setSessionFactory(tfPlatform.requestUserRelated().sessionFactory());

		return bean;
	}

	@Managed
	public DemoHealthCheckProcessor demoHealthCheckProcessor() {
		return new DemoHealthCheckProcessor();
	}

	@Managed
	public ResourceStreamingProcessor resourceStreamingProcessor() {
		ResourceStreamingProcessor bean = new ResourceStreamingProcessor();
		bean.setExistingResourcesPath(publicResourcesPath(""));
		bean.setUploadPath(publicResourcesPath("/uploads"));

		return bean;
	}

	// == Jobs == //

	@Managed
	public ServiceProcessor<JobRequest, JobResponse> orphanedEmployeesWatcher(
			ExpertContext<tribefire.extension.demo.model.deployment.OrphanedEmployeesWatchJob> context) {
		tribefire.extension.demo.model.deployment.OrphanedEmployeesWatchJob deployable = context.getDeployable();

		OrphanedEmployeesWatchJob bean = new OrphanedEmployeesWatchJob();
		bean.setAccessId(deployable.getAccess().getExternalId());
		bean.setSessionFactory(tfPlatform.requestUserRelated().sessionFactory());

		return bean;
	}

	// == Helpers == //

	private String publicResourcesPath(String resourcesPath) {
		Path path = null;
		try {
			path = resources.publicResources(resourcesPath).asPath();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve " + resourcesPath);
		}
		return path.toString();
	}
}
