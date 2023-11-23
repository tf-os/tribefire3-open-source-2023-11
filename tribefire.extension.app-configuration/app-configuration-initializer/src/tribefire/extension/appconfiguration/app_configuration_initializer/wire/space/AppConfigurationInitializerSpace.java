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
package tribefire.extension.appconfiguration.app_configuration_initializer.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.StringTools.isBlank;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.deployment.database.connector.DatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.DisjunctionSelector;
import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.deployables.initializer.support.wire.contract.DefaultDeployablesContract;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.appconfiguration.app_configuration_initializer.wire.contract.AppConfigurationInitializerContract;
import tribefire.extension.appconfiguration.app_configuration_initializer.wire.contract.AppConfigurationInitializerModelsContract;
import tribefire.extension.appconfiguration.app_configuration_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.appconfiguration.app_configuration_initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.appconfiguration.model.deployment.AppConfigurationJsUxComponent;
import tribefire.extension.appconfiguration.model.deployment.AppConfigurationProcessor;
import tribefire.extension.appconfiguration.model.deployment.ExportLocalizationsToSpreadsheetProcessor;
import tribefire.extension.appconfiguration.model.deployment.ImportLocalizationsFromSpreadsheetProcessor;
import tribefire.extension.js.model.deployment.JsUxComponent;
import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;

@Managed
public class AppConfigurationInitializerSpace extends AbstractInitializerSpace implements AppConfigurationInitializerContract {

	@Import
	private AppConfigurationInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private DefaultDeployablesContract defaultDeployables;

	@Managed
	@Override
	public IncrementalAccess appConfigurationAccess() {
		IncrementalAccess bean = isHibernateAccess() ? appConfigurationHibernateAccess() : appConfigurationSmoodAccess();
		bean.setExternalId(properties.APP_CONFIGURATION_ACCESS_ID());
		bean.setName("App Configuration Access");
		bean.setServiceModel(existingInstances.appConfigurationApiModel());
		bean.setMetaModel(existingInstances.appConfigurationModel());
		bean.setWorkbenchAccess(appConfigurationWorkbenchAccess());
		bean.setAspectConfiguration(appConfigurationAccessAspectConfiguration());

		return bean;
	}

	@Managed
	private CollaborativeSmoodAccess appConfigurationSmoodAccess() {
		return create(CollaborativeSmoodAccess.T);
	}

	@Managed
	private HibernateAccess appConfigurationHibernateAccess() {
		HibernateAccess bean = create(HibernateAccess.T);
		bean.setConnector(appConfigurationAccessConnection());

		return bean;
	}

	@Managed
	private AspectConfiguration appConfigurationAccessAspectConfiguration() {
		AspectConfiguration bean = create(AspectConfiguration.T);
		bean.getAspects().add(defaultDeployables.stateProcessingAspect());

		if (Boolean.TRUE.equals(properties.APP_CONFIGURATION_ENABLE_AUDITING())) {
			bean.getAspects().add(defaultDeployables.auditAspect());
		}

		return bean;
	}

	@Managed
	private DatabaseConnectionPool appConfigurationAccessConnection() {
		HikariCpConnectionPool bean = create(HikariCpConnectionPool.T);
		bean.setName("App Configuration Access Connection");
		bean.setExternalId("connection.appConfiguration");
		bean.setConnectionDescriptor(appConfigurationAccessConnectionDescriptor());
		bean.setMaxPoolSize(properties.APP_CONFIGURATION_DB_POOL_MAX_CONNECTIONS());
		bean.setMinPoolSize(1);

		return bean;
	}

	@Managed
	private DatabaseConnectionDescriptor appConfigurationAccessConnectionDescriptor() {
		GenericDatabaseConnectionDescriptor bean = create(GenericDatabaseConnectionDescriptor.T);
		bean.setDriver(properties.APP_CONFIGURATION_DB_CONNECTION_DRIVER());
		bean.setUrl(properties.APP_CONFIGURATION_DB_CONNECTION_URL());
		bean.setUser(properties.APP_CONFIGURATION_DB_CONNECTION_USER());
		bean.setPassword(properties.APP_CONFIGURATION_DB_CONNECTION_PASSWORD());

		return bean;
	}

	@Managed
	private CollaborativeSmoodAccess appConfigurationWorkbenchAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		bean.setExternalId(properties.APP_CONFIGURATION_ACCESS_ID() + ".wb");
		bean.setName("App Configuration Workbench Access");
		bean.setMetaModel(models.appConfigurationWorkbenchModel());
		bean.setWorkbenchAccess(existingInstances.workbenchAccess());

		return bean;
	}

	@Managed
	@Override
	public AppConfigurationProcessor appConfigurationProcessor() {
		AppConfigurationProcessor bean = create(AppConfigurationProcessor.T);
		bean.setName("App Configuration Processor");
		bean.setExternalId("processor.appConfiguration");
		bean.setAccessId(properties.APP_CONFIGURATION_ACCESS_ID());

		return bean;
	}

	@Managed
	@Override
	public ProcessWith processWithAppConfigurationProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(appConfigurationProcessor());

		return bean;
	}

	@Managed
	@Override
	public ImportLocalizationsFromSpreadsheetProcessor importLocalizationsFromSpreadsheetProcessor() {
		ImportLocalizationsFromSpreadsheetProcessor bean = create(ImportLocalizationsFromSpreadsheetProcessor.T);
		bean.setName("Import Localizations From Spreadsheet Processor");
		bean.setExternalId("processor.importLocalizationsFromSpreadsheet");
		bean.setAccessId(properties.APP_CONFIGURATION_ACCESS_ID());

		return bean;
	}

	@Managed
	@Override
	public ProcessWith processWithImportLocalizationsFromSpreadsheetProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(importLocalizationsFromSpreadsheetProcessor());

		return bean;
	}

	@Managed
	@Override
	public ExportLocalizationsToSpreadsheetProcessor exportLocalizationsToSpreadsheetProcessor() {
		ExportLocalizationsToSpreadsheetProcessor bean = create(ExportLocalizationsToSpreadsheetProcessor.T);
		bean.setName("Export Localizations To Spreadsheet Processor");
		bean.setExternalId("processor.exportLocalizationsToSpreadsheet");
		bean.setAccessId(properties.APP_CONFIGURATION_ACCESS_ID());

		return bean;
	}

	@Managed
	@Override
	public ProcessWith processWithExportLocalizationsToSpreadsheetProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(exportLocalizationsToSpreadsheetProcessor());

		return bean;
	}

	@Managed
	@Override
	public Hidden hiddenForNonAdminAndApi() {
		Hidden bean = create(Hidden.T);
		bean.setSelector(nonAdminAndApiSelector());

		return bean;
	}

	@Managed
	@Override
	public Hidden hiddenForNonAdminAndGme() {
		Hidden bean = create(Hidden.T);
		bean.setSelector(nonAdminAndGmeSelector());

		return bean;
	}

	@Managed
	private RoleSelector adminSelector() {
		RoleSelector bean = create(RoleSelector.T);
		bean.setRoles(asSet("tf-admin", properties.APP_CONFIGURATION_ADMIN_ROLE()));

		return bean;
	}

	@Managed
	private NegationSelector nonAdminSelector() {
		NegationSelector bean = create(NegationSelector.T);
		bean.setOperand(adminSelector());

		return bean;
	}

	@Managed
	private DisjunctionSelector apiSelector() {
		DisjunctionSelector bean = create(DisjunctionSelector.T);
		bean.getOperands().add(existingInstances.swaggerSelector());
		bean.getOperands().add(existingInstances.openApiSelector());

		return bean;
	}

	@Managed
	private ConjunctionSelector nonAdminAndApiSelector() {
		ConjunctionSelector bean = create(ConjunctionSelector.T);
		bean.getOperands().add(nonAdminSelector());
		bean.getOperands().add(apiSelector());

		return bean;
	}

	@Managed
	private ConjunctionSelector nonAdminAndGmeSelector() {
		ConjunctionSelector bean = create(ConjunctionSelector.T);
		bean.getOperands().add(nonAdminSelector());
		bean.getOperands().add(existingInstances.gmeSelector());

		return bean;
	}

	@Managed
	@Override
	public ViewWithJsUxComponent viewWithJsUxComponent() {
		ViewWithJsUxComponent bean = create(ViewWithJsUxComponent.T);
		bean.setComponent(uxComponent());
		bean.setDisplayName(uxComponentDisplayName());

		return bean;
	}

	@Managed
	private JsUxComponent uxComponent() {
		JsUxComponent bean = create(AppConfigurationJsUxComponent.T);
		bean.setModule(existingInstances.uxModule());

		return bean;
	}

	@Managed
	private LocalizedString uxComponentDisplayName() {
		LocalizedString bean = create(LocalizedString.T);
		bean.getLocalizedValues().put("default", "App Configuration UX Module View");

		return bean;
	}

	private boolean isHibernateAccess() {
		return !isBlank(properties.APP_CONFIGURATION_DB_CONNECTION_URL());
	}
}
