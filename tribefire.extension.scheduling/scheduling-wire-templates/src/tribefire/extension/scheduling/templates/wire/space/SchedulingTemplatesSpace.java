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
package tribefire.extension.scheduling.templates.wire.space;

import java.util.Set;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.accessdeployment.smood.SmoodAccess;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.extensiondeployment.scheduling.JobScheduling;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.cron.CronTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.scheduling.SchedulingConstants;
import tribefire.extension.scheduling.model.deployment.SchedulerJob;
import tribefire.extension.scheduling.model.deployment.SchedulingProcessor;
import tribefire.extension.scheduling.templates.api.SchedulingTemplateContext;
import tribefire.extension.scheduling.templates.util.DbMapper;
import tribefire.extension.scheduling.templates.util.DdraMappingsBuilder;
import tribefire.extension.scheduling.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingDbMappingsContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingMetaDataContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingModelsContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingTemplatesContract;

@Managed
public class SchedulingTemplatesSpace implements SchedulingTemplatesContract {

	private static final Logger logger = Logger.getLogger(SchedulingTemplatesSpace.class);

	@Import
	private SchedulingModelsContract models;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private SchedulingMetaDataContract metadata;

	@Import
	private ExistingInstancesContract existing;

	@Import
	private SchedulingDbMappingsContract dbMappings;

	@Managed
	@Override
	public SchedulingProcessor configure(SchedulingTemplateContext context) {
		SchedulingProcessor bean = serviceProcessor(context);

		IncrementalAccess access = access(context);
		bean.setAccessId(access.getExternalId());

		metadata.configureMetaData(context);

		Set<DdraMapping> ddraMappings = ddraMappings(context);

		existing.ddraConfiguration().getMappings().addAll(ddraMappings);

		scheduler(context);
		SchedulerJob schedulerJob = schedulerJob(context);
		schedulerJob.setAccessId(access.getExternalId());

		addMetadataToModels(context);

		return bean;
	}

	@Override
	@Managed
	public JobScheduling scheduler(SchedulingTemplateContext context) {
		JobScheduling bean = context.create(JobScheduling.T, InstanceConfiguration.currentInstance());

		Long pollingIntervalMs = context.getPollingIntervalMs();
		if (pollingIntervalMs == null || pollingIntervalMs <= 0L) {
			pollingIntervalMs = (long) Numbers.MILLISECONDS_PER_MINUTE * 10;
		}
		String cronExpression = CronTools.createCronExpressionFromTimeSpan(pollingIntervalMs);
		logger.debug("Transformed time interval " + pollingIntervalMs + " into cron expression: " + cronExpression);

		bean.setCronExpression(cronExpression);
		bean.setCoalescing(true);
		bean.setJob(schedulerJob(context));
		bean.setExternalId("job-scheduling.cleanup");
		bean.setName("Scheduling Job");

		return bean;
	}

	@Managed
	private SchedulerJob schedulerJob(SchedulingTemplateContext context) {
		SchedulerJob bean = context.create(SchedulerJob.T, InstanceConfiguration.currentInstance());
		bean.setExternalId("job.scheduling");
		bean.setModule(context.getModule());
		bean.setAccessId(context.getAccessId());

		return bean;
	}

	@Override
	@Managed
	public SchedulingProcessor serviceProcessor(SchedulingTemplateContext context) {
		SchedulingProcessor bean = context.create(SchedulingProcessor.T, InstanceConfiguration.currentInstance());
		bean.setAccessId(context.getAccessId());
		return bean;
	}

	@Managed
	@Override
	public MetaData processWithSchedulingServiceProcessor(SchedulingTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(serviceProcessor(context));
		return bean;
	}

	// Access

	@Managed
	@Override
	public IncrementalAccess access(SchedulingTemplateContext context) {
		String dataAccessId = context.getAccessId();
		if (!StringTools.isBlank(dataAccessId)) {
			IncrementalAccess bean = context.lookupExternalId(dataAccessId);
			if (bean != null) {
				return bean;
			}
		}
		IncrementalAccess bean = schedulingAccess(context);
		return bean;
	}

	@Managed
	public IncrementalAccess schedulingAccess(SchedulingTemplateContext context) {
		IncrementalAccess bean;

		DatabaseConnectionPool claimsDefaultDbConnectionPool = null;
		String connectionGlobalId = context.getDatabaseConnectionGlobalId();
		if (!StringTools.isBlank(connectionGlobalId)) {
			claimsDefaultDbConnectionPool = context.lookup(connectionGlobalId);
		}

		if (!StringTools.isBlank(context.getDatabaseUrl()) || claimsDefaultDbConnectionPool != null) {
			bean = hibernateAccess(context);
		} else {
			bean = smoodAccess(context);
		}

		bean.setExternalId(context.getAccessId());
		bean.setName("Scheduling Access");
		bean.setMetaModel(models.configuredSchedulingAccessModel());
		bean.setServiceModel(models.configuredSchedulingApiModel());
		bean.setWorkbenchAccess(wbAccess(context));

		return bean;
	}

	@Managed
	private SmoodAccess smoodAccess(SchedulingTemplateContext context) {
		SmoodAccess bean = context.create(SmoodAccess.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	private HibernateAccess hibernateAccess(SchedulingTemplateContext context) {
		HibernateAccess bean = context.create(HibernateAccess.T, InstanceConfiguration.currentInstance());

		bean.setConnector(dbConnectionPool(context));

		final String dbPrefix = "SCHED_";

		bean.setForeignKeyNamePrefix(dbPrefix);
		bean.setIndexNamePrefix(dbPrefix);
		bean.setObjectNamePrefix(dbPrefix);
		bean.setTableNamePrefix(dbPrefix);
		bean.setUniqueKeyNamePrefix(dbPrefix);

		return bean;
	}

	@Managed
	private DatabaseConnectionPool dbConnectionPool(SchedulingTemplateContext context) {
		String dbUrl = context.getDatabaseUrl();
		if (StringTools.isBlank(dbUrl)) {
			DatabaseConnectionPool claimsDefaultDbConnectionPool = context.lookup(context.getDatabaseConnectionGlobalId());
			return claimsDefaultDbConnectionPool;
		}
		HikariCpConnectionPool bean = context.create(HikariCpConnectionPool.T, InstanceConfiguration.currentInstance());
		bean.setConnectionDescriptor(dbConnectionDescriptor(context));
		bean.setMaxPoolSize(10);
		bean.setEnableJmx(true);
		bean.setEnableMetrics(true);
		bean.setExternalId("db.data.scheduling");
		bean.setName("Scheduling DB");
		return bean;
	}

	@Managed
	private GenericDatabaseConnectionDescriptor dbConnectionDescriptor(SchedulingTemplateContext context) {
		GenericDatabaseConnectionDescriptor bean = context.create(GenericDatabaseConnectionDescriptor.T, InstanceConfiguration.currentInstance());
		bean.setDriver("org.postgresql.Driver");
		bean.setUrl(context.getDatabaseUrl());
		bean.setUser(context.getDatabaseUser());
		bean.setPassword(context.getDatabasePassword());
		return bean;
	}

	@Managed
	private CollaborativeSmoodAccess wbAccess(SchedulingTemplateContext context) {
		CollaborativeSmoodAccess bean = context.create(CollaborativeSmoodAccess.T, InstanceConfiguration.currentInstance());

		bean.setExternalId(SchedulingConstants.WB_ACCESS_ID);
		bean.setName("Scheduling Workbench Access");
		bean.setMetaModel(models.configuredSchedulingWbModel());
		bean.setWorkbenchAccess(coreInstances.workbenchAccess());

		return bean;
	}

	@Override
	@Managed
	public Set<DdraMapping> ddraMappings(SchedulingTemplateContext context) {
		final InstanceConfiguration currentInstance = InstanceConfiguration.currentInstance();
		//@formatter:off
		Set<DdraMapping> bean =
					new DdraMappingsBuilder(
							context.getAccessId(),
						context::lookup,
						t -> context.create(t, currentInstance))
					.build();
		//@formatter:on
		return bean;
	}

	private void addMetadataToModels(SchedulingTemplateContext context) {
		applyDataModelMetaData(context, models.configuredSchedulingAccessModel());
	}
	private void applyDataModelMetaData(SchedulingTemplateContext context, GmMetaModel model) {
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(model);

		editor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.id, stringTypeSpecification(context), idName(context));

		///////////////////////////////
		// apply DB related mappings
		///////////////////////////////
		new DbMapper(dbMappings, editor).applyDbMappings();

	}

	@Managed
	private TypeSpecification stringTypeSpecification(SchedulingTemplateContext context) {
		TypeSpecification bean = context.create(TypeSpecification.T, InstanceConfiguration.currentInstance());
		bean.setType(existing.stringType());
		return bean;
	}
	@Managed
	private Name idName(SchedulingTemplateContext context) {
		Name bean = context.create(Name.T, InstanceConfiguration.currentInstance());
		bean.setName(LocalizedString.create("Id"));
		return bean;
	}
}
