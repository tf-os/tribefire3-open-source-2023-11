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
package tribefire.extension.wopi.initializer.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.wopi.service.WacHealthCheckProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.wopi.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.wopi.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.wopi.initializer.wire.contract.WopiInitializerContract;
import tribefire.extension.wopi.processing.EntityStorageType;
import tribefire.extension.wopi.processing.StorageType;
import tribefire.extension.wopi.templates.api.WopiTemplateContext;
import tribefire.extension.wopi.templates.api.WopiTemplateDatabaseContext;
import tribefire.extension.wopi.templates.wire.contract.WopiTemplatesContract;

@Managed
public class WopiInitializerSpace extends AbstractInitializerSpace implements WopiInitializerContract {

	private static final Logger logger = Logger.getLogger(WopiInitializerSpace.class);

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private WopiTemplatesContract wopiTemplates;

	@Managed
	public WopiTemplateDatabaseContext defaultDatabaseContext() {

		//@formatter:off
		WopiTemplateDatabaseContext context = WopiTemplateDatabaseContext.builder()
				.setHibernateDialect(properties.WOPI_DB_HIBERNATEDIALECT())
				.setTablePrefix(properties.WOPI_TABLE_PREFIX())
				.setDatabaseDriver(properties.WOPI_DB_DRIVER())
				.setDatabaseUrl(properties.WOPI_DB_URL())
				.setDatabaseUsername(properties.WOPI_DB_USER())
				.setDatabasePassword(properties.WOPI_DB_PASSWORD())
				.setMinPoolSize(properties.WOPI_DB_MIN_POOL_SIZE())
				.setMaxPoolSize(properties.WOPI_DB_MAX_POOL_SIZE()
			).build();
		//@formatter:on
		return context;

	}

	@Managed
	public WopiTemplateContext defaultWopiTemplateContext() {

		StorageType storageType = properties.WOPI_STORAGE_TYPE();
		if (storageType == StorageType.external) {
			// Well, some other intializer has to take care of it
			logger.debug(() -> "External storage type configured. Another initializer will have to take care of that. Using fs for the time being.");
			storageType = StorageType.fs;
		}

		WopiTemplateDatabaseContext wopiTemplateDatabaseContext = null;
		EntityStorageType entityStorageType = properties.WOPI_ENTITY_STORAGE_TYPE();
		if (entityStorageType == EntityStorageType.db || storageType == StorageType.db) {
			wopiTemplateDatabaseContext = defaultDatabaseContext();
		}

		//@formatter:off
		WopiTemplateContext bean = WopiTemplateContext.builder(null)
				.setContext(properties.WOPI_CONTEXT())
				.setEntityFactory(super::create)
				.setWopiModule(existingInstances.module())
				.setLookupFunction(super::lookup)
				.setLookupExternalIdFunction(super::lookupExternalId)
				.setStorageType(storageType)
				.setDatabaseContext(wopiTemplateDatabaseContext)
				.setStorageFolder(properties.WOPI_STORAGE_FOLDER())
				.setWacDiscoveryEndpoint("http://wopi.agile-documents.com/hosting/discovery")
				.setCustomPublicServicesUrl(null)
				.build();
		//@formatter:on
		return bean;
	}

	@Override
	public void setupDefaultConfiguration() {
		wopiTemplates.setupWopi(defaultWopiTemplateContext());
	}

	@Override
	@Managed
	public WacHealthCheckProcessor healthCheckProcessor() {
		WacHealthCheckProcessor bean = create(WacHealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("WOPI Check Processor");
		bean.setExternalId("wopi.healthzProcessor");
		return bean;
	}

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("WOPI Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.functional);
		bean.setIsPlatformRelevant(false);

		return bean;
	}
}
