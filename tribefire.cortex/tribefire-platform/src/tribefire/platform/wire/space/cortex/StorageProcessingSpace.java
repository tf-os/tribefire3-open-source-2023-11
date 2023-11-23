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
package tribefire.platform.wire.space.cortex;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;
//import com.braintribe.wire.api.util.Maps;

@Managed
public class StorageProcessingSpace implements WireSpace {

//	@Import
//	private BindingsSpace bindings;
//
//	@Import
//	private BindingResourcesSpace bindingResources;
//
//	@Import
//	private HibernateDialectSpace hibernateDialect;
//
//	@Import
//	private MasterResourcesContract resources;
//
//	@Import
//	private EnvironmentContract environment;
//	
//	@Import
//	private SystemAccessesSpace systemAccesses;
//
//	@Managed
//	public StorageFromUrlImporter importer() {
//		StorageFromUrlImporter bean = new StorageFromUrlImporter();
//		bean.setStorageTarget(resources.storage(".").asFile());
//		bean.setUrlSource(environment.property(TribefireRuntime.ENVIRONMENT_STORAGE_URL));
//		bean.setStorageChecksumFilePath("../.checksum");
//		bean.setRunOnStartup(true);
//		return bean;
//	}
//
//	@Managed
//	public StorageToResourceExporter exporter() {
//		StorageToResourceExporter bean = new StorageToResourceExporter();
//		// @formatter:off
//		bean.setExportAccessSources(
//				list(
//					storageAccessSource(systemAccesses.cortex()),
//					storageAccessSource(systemAccesses.cortexWorkbench()),
//					storageAccessSource(systemAccesses.workbench()),
//					storageAccessSource(systemAccesses.authWorkbench())
//				)
//			);
//		// @formatter:on
//		bean.setAccessDataMarshaller(StaxMarshaller.defaultInstance);
//		bean.setExportStaticResources(true);
//		bean.setDataFilePathPattern("databases/{accessId}/data/current.xml");
//		bean.setStaticResourcesDirectoryPattern("databases/{accessId}/resources");
//		return bean;
//	}
//
//	@Managed
//	public StoragePublisher publisher() {
//		StoragePublisher bean;
//		if (hasDataSource()) {
//			bean = databasePublisher();
//		} else {
//			bean = defaultPublisher();
//		}
//		return bean;
//	}
//
//	@Managed
//	public StoragePublisherListener publisherListener() {
//		EmptyStoragePublisherListener bean = new EmptyStoragePublisherListener();
//		return bean;
//	}
//
//	@Managed
//	public Supplier<? extends DataSource> dataSourceSupplier() {
//		// @formatter:off
//		Supplier<? extends DataSource> bean = 
//				bindingResources.dataSourceSupplier(
//						bindings.environmentStorageDb(),
//						bindings.jndiStorageDb()
//				);
//		// @formatter:on
//		return bean;
//	}
//
//	@Managed
//	public SessionFactory hibernateSessionFactory() {
//
//		HibernateSessionFactoryBean factory = new HibernateSessionFactoryBean();
//
//		DataSource dataSource = dataSourceSupplier().get();
//
//		factory.setDataSource(dataSource);
//		factory.setDialect(hibernateDialect.dialect(dataSource));
//
//		// @formatter:off
//		factory.setMappingLocations(resources.classpath("com/braintribe/model/storage/database/ExportedStorage.hbm.xml").asUrl());
//		// @formatter:on
//		factory.setDefaultBatchFetchSize(30);
//		factory.setAdditionalProperties(Collections.emptyMap());
//		factory.setInterceptor(new GmAdaptionInterceptor());
//
//		factory.setUseQueryCache(!TribefireRuntime.isClustered());
//		factory.setUseSecondLevelCache(!TribefireRuntime.isClustered());
//		factory.setRegionFactory(EhCacheRegionFactory.class);
//		factory.setConfigAdaptor(hibernateConfigAdaptor());
//
//		currentBean().onDestroy(factory::preDestroy);
//
//		try {
//			factory.afterPropertiesSet();
//		} catch (Exception e) {
//			throw new WireException("Failed to initialize session factory", e);
//		}
//
//		SessionFactory bean = factory.getObject();
//		return bean;
//
//	}
//
//	@Managed
//	public HibernateConfigurationAdaptor hibernateConfigAdaptor() {
//		CompoundHibernateConfigurationAdaptor bean = new CompoundHibernateConfigurationAdaptor();
//		// @formatter:off
//		bean.setAdaptors(
//				list(
//					cacheNameAdaptor(), 
//					cacheFolderAdaptor()
//				)
//			);
//		// @formatter:on
//		return bean;
//	}
//
//	@Managed
//	public HibernateConfigurationAdaptor cacheNameAdaptor() {
//		XPathAdaptor bean = new XPathAdaptor();
//		bean.setValueMap(map(Maps.entry("/ehcache/@name", "cacheManager-storage")));
//		return bean;
//	}
//
//	@Managed
//	public HibernateConfigurationAdaptor cacheFolderAdaptor() {
//		try {
//			TemporaryFolderCacheAdaptor bean = new TemporaryFolderCacheAdaptor();
//			return bean;
//		} catch (Exception e) {
//			throw new WireException(e);
//		}
//	}
//
//	public boolean hasDataSource() {
//		return dataSourceSupplier().get() != null;
//	}
//
//	protected NoTargetStoragePublisher defaultPublisher() {
//		NoTargetStoragePublisher bean = new NoTargetStoragePublisher();
//		bean.setResponseMessage("No eligible publish target exists");
//		return bean;
//	}
//
//	protected StorageToDatabasePublisher databasePublisher() {
//		StorageToDatabasePublisher bean = new StorageToDatabasePublisher();
//		bean.setSessionFactoryProvider(this::hibernateSessionFactory);
//		bean.setStoragePublisherListener(publisherListener());
//		return bean;
//	}
//
//	protected StorageAccessSource storageAccessSource(SystemAccessSpaceBase accessSpace) {
//		StorageAccessSource bean = new StorageAccessSource();
//		bean.setLowLevelSessionProvider(accessSpace::lowLevelSession);
//		bean.setSessionProvider(accessSpace.sessionProvider());
//		return bean;
//	}

}
