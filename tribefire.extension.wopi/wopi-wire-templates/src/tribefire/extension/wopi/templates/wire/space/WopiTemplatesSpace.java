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
package tribefire.extension.wopi.templates.wire.space;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.deployment.database.connector.DatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.deployment.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.wopi.connector.WopiWacConnector;
import com.braintribe.model.wopi.service.CleanupWopiSessionWorker;
import com.braintribe.model.wopi.service.ExpireWopiSessionWorker;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.model.wopi.service.WopiIntegrationExample;
import com.braintribe.model.wopi.service.WopiServiceProcessor;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.wopi.WopiConstants;
import tribefire.extension.wopi.templates.api.WopiTemplateContext;
import tribefire.extension.wopi.templates.api.WopiTemplateDatabaseContext;
import tribefire.extension.wopi.templates.util.WopiTemplateUtil;
import tribefire.extension.wopi.templates.wire.contract.BasicInstancesContract;
import tribefire.extension.wopi.templates.wire.contract.WopiTemplatesContract;

/**
 *
 */
@Managed
public class WopiTemplatesSpace implements WireSpace, WopiTemplatesContract {

	private static final Logger logger = Logger.getLogger(WopiTemplatesSpace.class);

	@Import
	private BasicInstancesContract basicInstances;

	@Import
	private WopiMetaDataSpace wopiMetaData;

	@Override
	public IncrementalAccess access(WopiTemplateContext context) {
		// check if access comes from Template Context or use a new created
		IncrementalAccess access = context.getAccess();
		if (access == null) {
			return wopiAccess(context);
		} else {
			// add dependency if the access does not have it as already as a dependency
			if (access.getMetaModel().getDependencies().stream().filter(m -> m.getName().equals(WopiTemplateUtil.resolveDataModelName(context)))
					.count() == 0) {
				GmMetaModel dataModel = wopiMetaData.dataModel(context);
				GmMetaModel serviceModel = wopiMetaData.serviceModel(context);
				access.getMetaModel().getDependencies().add(dataModel);
				access.getServiceModel().getDependencies().add(serviceModel);
			}

			return access;
		}
	}

	@Override
	public void setupWopi(WopiTemplateContext context) {
		if (context == null) {
			throw new IllegalArgumentException("The WopiTemplateContext must not be null.");
		}
		logger.debug(() -> "Configuring WOPI Access based on:\n" + StringTools.asciiBoxMessage(context.toString(), -1));

		access(context);

		// processing
		cleanupWopiSessionWorker(context);
		expireWopiSessionWorker(context);
		wopiServiceProcessor(context);
		wopiIntegrationExample(context);

		// metadata
		wopiMetaData.metaData(context);
	}

	@Managed
	private IncrementalAccess wopiAccess(WopiTemplateContext context) {

		IncrementalAccess bean = storageAccess(context);

		bean.setWorkbenchAccess(wopiWorkbenchAccess(context));

		return bean;
	}

	@Override
	@Managed
	public WopiServiceProcessor wopiServiceProcessor(WopiTemplateContext context) {
		WopiServiceProcessor bean = context.create(WopiServiceProcessor.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getWopiModule());
		bean.setAutoDeploy(true);

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Service Processor", context));
		bean.setWopiApp(wopiApp(context));
		return bean;
	}

	@Override
	@Managed
	public WopiApp wopiApp(WopiTemplateContext context) {
		WopiApp bean = context.create(WopiApp.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getWopiModule());
		bean.setAutoDeploy(true);

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI App", context));
		bean.setPathIdentifier("wopi");

		bean.setWopiWacConnector(wopiWacConnector(context));
		bean.setAccess(access(context));

		if (!CommonTools.isEmpty(context.getContext())) {
			bean.setContext(context.getContext());
		}

		if (!CommonTools.isNull(context.getLockExpirationInMs())) {
			bean.setLockExpirationInMs(context.getLockExpirationInMs());
		}
		if (!CommonTools.isNull(context.getWopiSessionExpirationInMs())) {
			bean.setWopiSessionExpirationInMs(context.getWopiSessionExpirationInMs());
		}
		if (!CommonTools.isNull(context.getWopiAppLogWarningThresholdInMs())) {
			bean.setLogWarningThresholdInMs(context.getWopiAppLogWarningThresholdInMs());
		}
		if (!CommonTools.isNull(context.getWopiAppLogErrorThresholdInMs())) {
			bean.setLogErrorThresholdInMs(context.getWopiAppLogErrorThresholdInMs());
		}

		// WOPI SESSION STANDARD VALUES
		if (!CommonTools.isNull(context.getMaxVersions())) {
			bean.setMaxVersions(context.getMaxVersions());
		}
		if (!CommonTools.isEmpty(context.getTenant())) {
			bean.setTenant(context.getTenant());
		}
		if (!CommonTools.isNull(context.getWopiLockExpirationInMs())) {
			bean.setWopiLockExpirationInMs(context.getWopiLockExpirationInMs());
		}

		// UI CUSTOMIZATION
		if (!CommonTools.isNull(context.getShowUserFriendlyName())) {
			bean.setShowUserFriendlyName(context.getShowUserFriendlyName());
		}
		if (!CommonTools.isNull(context.getShowBreadcrumbBrandName())) {
			bean.setShowBreadcrumbBrandName(context.getShowBreadcrumbBrandName());
		}
		if (!CommonTools.isNull(context.getShowBreadCrumbDocName())) {
			bean.setShowBreadCrumbDocName(context.getShowBreadCrumbDocName());
		}
		if (!CommonTools.isNull(context.getShowBreadcrumbFolderName())) {
			bean.setShowBreadcrumbFolderName(context.getShowBreadcrumbFolderName());
		}
		if (!CommonTools.isNull(context.getDisablePrint())) {
			bean.setDisablePrint(context.getDisablePrint());
		}
		if (!CommonTools.isNull(context.getDisableTranslation())) {
			bean.setDisableTranslation(context.getDisableTranslation());
		}

		return bean;
	}

	@Override
	@Managed
	public WopiIntegrationExample wopiIntegrationExample(WopiTemplateContext context) {
		WopiIntegrationExample bean = context.create(WopiIntegrationExample.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getWopiModule());
		bean.setAutoDeploy(true);

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Integration Example", context));
		bean.setPathIdentifier("wopiIntegrationExample");

		bean.setAccess(access(context));
		bean.setWopiApp(wopiApp(context));

		return bean;
	}

	@Override
	@Managed
	public WopiWacConnector wopiWacConnector(WopiTemplateContext context) {
		WopiWacConnector bean = context.create(WopiWacConnector.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getWopiModule());
		bean.setAutoDeploy(true);

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI WAC Connector", context));
		bean.setWacDiscoveryEndpoint(context.getWacDiscoveryEndpoint());
		bean.setCustomPublicServicesUrl(context.getCustomPublicServicesUrl());

		return bean;
	}

	@Override
	@Managed
	public CleanupWopiSessionWorker cleanupWopiSessionWorker(WopiTemplateContext context) {
		CleanupWopiSessionWorker bean = context.create(CleanupWopiSessionWorker.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getWopiModule());
		bean.setAutoDeploy(true);

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Cleanup WopiSession Worker", context));

		bean.setAccess(access(context));
		bean.setIntervalInMs(3600000l);
		bean.setContext(wopiApp(context).getContext());

		return bean;
	}
	@Override
	@Managed
	public ExpireWopiSessionWorker expireWopiSessionWorker(WopiTemplateContext context) {
		ExpireWopiSessionWorker bean = context.create(ExpireWopiSessionWorker.T, InstanceConfiguration.currentInstance());
		bean.setModule(context.getWopiModule());
		bean.setAutoDeploy(true);

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Expire WopiSessions Worker", context));

		bean.setAccess(access(context));
		bean.setIntervalInMs(3600000l);
		bean.setContext(wopiApp(context).getContext());

		return bean;
	}

	@Managed
	@Override
	public IncrementalAccess storageAccess(WopiTemplateContext context) {
		IncrementalAccess bean;
		if (context.getDatabaseContext() == null) {
			bean = smoodAccess(context);
		} else {
			bean = dbAccess(context);
		}
		bean.setAutoDeploy(true);
		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("Wopi Access ", context));
		bean.setServiceModel(wopiMetaData.serviceModel(context));
		bean.setMetaModel(wopiMetaData.dataModel(context));
		return bean;

	}

	@Managed
	private CollaborativeSmoodAccess smoodAccess(WopiTemplateContext context) {
		CollaborativeSmoodAccess bean = context.create(CollaborativeSmoodAccess.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	private HibernateAccess dbAccess(WopiTemplateContext context) {
		WopiTemplateDatabaseContext dbContext = context.getDatabaseContext();
		HibernateDialect hibernateDialect = dbContext.getHibernateDialect();
		String tablePrefix = dbContext.getTablePrefix();

		HibernateAccess bean = context.create(HibernateAccess.T, InstanceConfiguration.currentInstance());

		bean.setConnector(connectionPool(context));

		bean.setDialect(hibernateDialect);
		if (!StringTools.isBlank(tablePrefix)) {
			bean.setTableNamePrefix(tablePrefix);
		}
		return bean;
	}

	@Override
	public DatabaseConnectionPool connectionPool(WopiTemplateContext context) {
		return dbConnector(context);
	}

	@Managed
	private DatabaseConnectionPool dbConnector(WopiTemplateContext context) {
		HikariCpConnectionPool bean = context.create(HikariCpConnectionPool.T, InstanceConfiguration.currentInstance());
		bean.setAutoDeploy(true);

		WopiTemplateDatabaseContext dbContext = context.getDatabaseContext();

		Integer maxPoolSize = dbContext.getMaxPoolSize();
		Integer minPoolSize = dbContext.getMinPoolSize();

		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Connection Pool", context));
		bean.setAutoDeploy(true);
		bean.setConnectionDescriptor(dbConnectionDescriptor(context));
		bean.setMinPoolSize(minPoolSize);
		bean.setMaxPoolSize(maxPoolSize);
		return bean;
	}

	@Managed
	private DatabaseConnectionDescriptor dbConnectionDescriptor(WopiTemplateContext context) {
		WopiTemplateDatabaseContext dbContext = context.getDatabaseContext();
		String databaseDriver = dbContext.getDatabaseDriver();
		String databaseUrl = dbContext.getDatabaseUrl();
		String databaseUsername = dbContext.getDatabaseUsername();
		String databasePassword = dbContext.getDatabasePassword();

		GenericDatabaseConnectionDescriptor bean = context.create(GenericDatabaseConnectionDescriptor.T, InstanceConfiguration.currentInstance());
		bean.setUrl(databaseUrl);
		bean.setDriver(databaseDriver);
		bean.setUser(databaseUsername);
		bean.setPassword(databasePassword);
		return bean;
	}

	@Managed
	@Override
	public SqlBinaryProcessor sqlBinaryProcessor(WopiTemplateContext context) {
		SqlBinaryProcessor bean = context.create(SqlBinaryProcessor.T, InstanceConfiguration.currentInstance());
		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Access SQL Binary Processor ", context));

		bean.setConnectionPool(connectionPool(context));
		bean.setAutoDeploy(true);
		return bean;
	}

	@Managed
	@Override
	public FileSystemBinaryProcessor filesystemBinaryProcessor(WopiTemplateContext context) {
		FileSystemBinaryProcessor bean = context.create(FileSystemBinaryProcessor.T, InstanceConfiguration.currentInstance());
		bean.setName(WopiTemplateUtil.resolveContextBasedDeployableName("WOPI Access File System Binary Processor ", context));

		bean.setBasePath(context.getStorageFolder());
		bean.setAutoDeploy(true);
		return bean;
	}

	@Override
	@Managed
	public CollaborativeSmoodAccess wopiWorkbenchAccess(WopiTemplateContext context) {

		CollaborativeSmoodAccess bean = context.create(CollaborativeSmoodAccess.T, InstanceConfiguration.currentInstance());
		bean.setName("WOPI Workbench Access");
		bean.setMetaModel(wopiWorkbenchModel(context));
		bean.setWorkbenchAccess(basicInstances.workbenchAccess(context));

		bean.setExternalId(WopiConstants.WOPI_WORKBENCH_ACCESS_EXTERNALID);
		bean.setGlobalId(WopiConstants.WOPI_WORKBENCH_ACCESS_GLOBALID);
		return bean;
	}

	@Managed
	public GmMetaModel wopiWorkbenchModel(WopiTemplateContext context) {
		GmMetaModel bean = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		bean.setName(WopiConstants.WOPI_WORKBENCH_ACCESS_MODEL);
		bean.setVersion(WopiConstants.MAJOR_VERSION + ".0");

		List<GmMetaModel> dependencies = bean.getDependencies();
		dependencies.add(basicInstances.workbenchModel(context));
		dependencies.add(wopiMetaData.dataModel(context));
		dependencies.add(wopiMetaData.serviceModel(context));
		dependencies.add(basicInstances.essentialMetaDataModel(context));

		return bean;
	}

}
