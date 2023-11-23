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
package tribefire.extension.hibernate.wire.space;

import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;
import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Maps.map;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;

import com.braintribe.cartridge.common.processing.DialectAutoSense;
import com.braintribe.cartridge.common.processing.DialectMapping;
import com.braintribe.cartridge.common.processing.ObjectRelationalMappings;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.hibernate.schema.SimpleConnectionUrlProvider;
import com.braintribe.model.access.hibernate.schema.SimpleSchemaProvider;
import com.braintribe.model.access.hibernate.schema.auto.DbSchemaUpdateImpl;
import com.braintribe.model.access.hibernate.schema.auto.SimpleDbSchemaUpdateContextProvider;
import com.braintribe.model.access.hibernate.schema.meta.DbUpdateStatementExecutor;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateComponent;
import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.model.accessdeployment.hibernate.HibernateEnhancedConnectionPool;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.PlainComponentBinder;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.deployment.hibernate.HibernateMappingsDirectorySupplier;
import com.braintribe.model.processing.idgenerator.basic.DateIdGenerator;
import com.braintribe.model.processing.idgenerator.basic.UuidGenerator;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.persistence.hibernate.GmAwareHibernateSessionFactoryBean;
import com.braintribe.persistence.hibernate.HibernateSessionFactoryBean;
import com.braintribe.persistence.hibernate.adaptor.CompoundHibernateConfigurationAdaptor;
import com.braintribe.persistence.hibernate.adaptor.HibernateConfigurationAdaptor;
import com.braintribe.persistence.hibernate.adaptor.TemporaryFolderCacheAdaptor;
import com.braintribe.persistence.hibernate.adaptor.XPathAdaptor;
import com.braintribe.persistence.hibernate.dialects.HibernateDialectMapping;
import com.braintribe.persistence.hibernate.dialects.HibernateDialectMappings;
import com.braintribe.persistence.hibernate.sql.HibernateEnhancedDataSource;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.api.util.Maps;

import net.sf.ehcache.CacheManager;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class HibernateDeployablesSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(HibernateDeployablesSpace.class);

	@Import
	private TribefireWebPlatformContract tfPlatform;

	private static final String ENV_TF_HIBERNATE_SCHEMA_UPDATE = "TF_HIBERNATE_SCHEMA_UPDATE";
	private static final String ENV_VALUE_TF_HIBERNATE_SCHEMA_UPDATE_FORCE = "FORCE";
	private static final String ENV_VALUE_TF_HIBERNATE_SCHEMA_UPDATE_TRUE = "TRUE";
	private static final String ENV_VALUE_TF_HIBERNATE_SCHEMA_UPDATE_FALSE = "FALSE";

	public void bindDeployables(DenotationBindingBuilder bindings) {
		ComponentBinder<DatabaseConnectionPool, DataSource> binder = new PlainComponentBinder<>(DatabaseConnectionPool.T, DataSource.class);

		PlainComponentBinder<HibernateComponent, com.braintribe.model.access.hibernate.HibernateComponent> hibernateComponentBinder = new PlainComponentBinder<>(
				HibernateComponent.T, com.braintribe.model.access.hibernate.HibernateComponent.class);

		bindings.bind(HibernateAccess.T) //
				.component(tfPlatform.binders().incrementalAccess()).expertFactory(this::access) //
				.component(hibernateComponentBinder).expertFactory(this::access);

		bindings.bind(HibernateEnhancedConnectionPool.T) //
				.component(binder) //
				.expertFactory(this::hibernateEnhancedConnectionPool);
	}

	@Managed
	private HibernateEnhancedDataSource hibernateEnhancedConnectionPool(ExpertContext<HibernateEnhancedConnectionPool> context) {
		HibernateEnhancedConnectionPool deployable = context.getDeployable();

		HibernateComponent componentDenotation = deployable.getHibernateComponent();
		DatabaseConnectionPool connectorDenotation = componentDenotation.getConnector();

		DataSource dataSoure = context.resolve(connectorDenotation, DatabaseConnectionPool.T);
		com.braintribe.model.access.hibernate.HibernateComponent component = context.resolve(componentDenotation, HibernateComponent.T);

		HibernateEnhancedDataSource bean = new HibernateEnhancedDataSource();
		bean.setDelegate(dataSoure);
		bean.setSessionFactory(component.getSessionFactory());

		return bean;
	}

	@Managed
	private com.braintribe.model.access.hibernate.HibernateAccess access(ExpertContext<HibernateAccess> context) {

		HibernateAccess deployable = context.getDeployable();

		com.braintribe.model.access.hibernate.HibernateAccess bean = new com.braintribe.model.access.hibernate.HibernateAccess();
		currentInstance().onDestroy(() -> shutdownCacheManagerIfNeeded(context)); // CAN'T TOUCH THIS
		bean.setHibernateSessionFactory(sessionFactory(context));
		bean.setModelSupplier(deployable::getMetaModel);
		bean.setAccessId(deployable.getExternalId());
		bean.setExpertRegistry(expertRegistry(context));
		bean.setLogging(deployable.getLogging());
		bean.setDeadlockRetryLimit(deployable.getDeadlockRetryLimit());
		bean.setDurationWarningThreshold(deployable.getDurationWarningThreshold());
		return bean;

	}

	/**
	 * The {@link CacheManager} must be fetched and closed like this because Hibernate's {@link SessionFactory} constructor might fail (e.g. when
	 * there is a problem with mappings) after the CM was already created. Yes, the SessionFactory does not do a proper cleanup, so there is an
	 * existing CM with some threads and we have to take care of it.
	 */
	private void shutdownCacheManagerIfNeeded(ExpertContext<HibernateAccess> context) {
		CacheManager cacheManager = CacheManager.getCacheManager(cacheName(context));
		if (cacheManager != null)
			cacheManager.shutdown();
	}

	@Managed
	private SessionFactory sessionFactory(ExpertContext<HibernateAccess> context) {
		HibernateSessionFactoryBean factory = sesionFactorySupplier(context);

		SessionFactory bean = factory.getObject();
		currentInstance().onDestroy(bean::close);

		return bean;
	}

	@Managed
	private HibernateSessionFactoryBean sesionFactorySupplier(ExpertContext<HibernateAccess> context) {
		StopWatch stopWatch = new StopWatch();

		HibernateAccess deployable = context.getDeployable();

		DataSoureInfo dataSoureInfo = resolveDataSourceInfo(context);
		stopWatch.intermediate("resolve dialect info");

		HibernateSessionFactoryBean bean = new GmAwareHibernateSessionFactoryBean();
		stopWatch.intermediate("Bean Creation");

		Boolean schemaUpdateOnModelChange = deployable.getSchemaUpdateOnlyOnModelChange();
		if (!forceDbSchemaUpdate() && (schemaUpdateOnModelChange == null || schemaUpdateOnModelChange.booleanValue())) {
			handleDbSchemaAutoUpdate(context, bean, stopWatch);
		}

		stopWatch.intermediate("SchemaUpdate Creation");

		// @formatter:off
		ObjectRelationalMappings
			.applyMappings(
					bean::setMappingInputSuppliers, 
					deployable,
					cmdResolver(context),
					() -> configureGeneratedMappings(context, bean, stopWatch)
				);
		// @formatter:on

		stopWatch.intermediate("Mappings");

		bean.setDataSource(dataSoureInfo.dataSource);
		bean.setDialect(dataSoureInfo.dialectClass);
		bean.setDefaultSchema(deployable.getDefaultSchema());
		bean.setDefaultCatalog(deployable.getDefaultCatalog());
		bean.setDefaultBatchFetchSize(30);

		bean.setUseQueryCache(!TribefireRuntime.isClustered());
		bean.setUseSecondLevelCache(!TribefireRuntime.isClustered());
		bean.setRegionFactory("ehcache");
		bean.setConfigAdaptor(configAdaptor(context));

		// Setting the additional properties last as these must be prioritized over inferred default values.
		bean.setAdditionalProperties(deployable.getProperties());

		logger.debug(() -> "Creating HibernateSessionFactoryBean " + deployable.getConnector() + ": " + stopWatch);

		return bean;
	}

	private void handleDbSchemaAutoUpdate(ExpertContext<HibernateAccess> context, HibernateSessionFactoryBean bean, StopWatch stopWatch) {
		DbSchemaUpdateImpl updateHandler = dbSchemaUpdateHandler(context);
		stopWatch.intermediate("Schema Update Handler Created");

		boolean needsSchemaUpdate = updateHandler.needsSchemaUpdate();
		stopWatch.intermediate("Schema Update Required (" + needsSchemaUpdate + ")");

		bean.setUpdateSchema(needsSchemaUpdate);

		// this method call needs to be the very last because the last code line signals that the schema updateis successful
		if (needsSchemaUpdate)
			bean.addAfterSchemaCreationTask(updateHandler::storeDbSchemaConfiguration);
	}

	// ENV_TF_HIBERNATE_SCHEMA_UPDATE:
	// 'not set' = use automatic schema update mechanism
	// 'FORCE' = disable automatic schema update mechanism
	// 'TRUE' = force that automatic schema update mechanism returns true
	// 'FALSE' = force that automatic schema update mechanism returns false
	private boolean forceDbSchemaUpdate() {
		boolean force = false;
		String hibernateSchemaUpdate = TribefireRuntime.getProperty(ENV_TF_HIBERNATE_SCHEMA_UPDATE);
		if (hibernateSchemaUpdate != null) {
			if (hibernateSchemaUpdate.equalsIgnoreCase(ENV_VALUE_TF_HIBERNATE_SCHEMA_UPDATE_FORCE)) {
				force = true;
			}
		}
		return force;
	}

	private Boolean overrideDbSchemaUpdate() {
		Boolean overrideDbSchemaUpdate = null;
		String hibernateSchemaUpdate = TribefireRuntime.getProperty(ENV_TF_HIBERNATE_SCHEMA_UPDATE);
		if (hibernateSchemaUpdate != null) {
			if (hibernateSchemaUpdate.equalsIgnoreCase(ENV_VALUE_TF_HIBERNATE_SCHEMA_UPDATE_TRUE)) {
				overrideDbSchemaUpdate = true;
			} else if (hibernateSchemaUpdate.equalsIgnoreCase(ENV_VALUE_TF_HIBERNATE_SCHEMA_UPDATE_FALSE)) {
				overrideDbSchemaUpdate = false;
			}
		}
		return overrideDbSchemaUpdate;
	}

	@Managed
	private DbSchemaUpdateImpl dbSchemaUpdateHandler(ExpertContext<HibernateAccess> context) {
		HibernateAccess deployable = context.getDeployable();
		DataSource dataSource = resolveDataSource(context);

		File mappingDirectory = mappingDirectory(context);

		String tableNamePrefix = deployable.getTableNamePrefix();

		Supplier<String> connectionUrl = simpleConnectionUrlProvider(dataSource);
		Supplier<String> schema = simpleSchemaProvider(dataSource);

		DbSchemaUpdateImpl bean = new DbSchemaUpdateImpl();
		bean.setLocking(tfPlatform.cluster().locking());
		bean.setDataSource(dataSource);
		bean.setMappingDirectory(mappingDirectory);
		bean.setAccessId(deployable.getExternalId());
		bean.setInstanceId(tfPlatform.platformReflection().instanceId().stringify());
		bean.setDbSchemaUpdateContextProvider(simpleDbSchemaUpdateContextProvider(connectionUrl, schema, tableNamePrefix));
		Boolean overrideDbSchemaUpdate = overrideDbSchemaUpdate();
		bean.setOverrideDbSchemaUpdate(overrideDbSchemaUpdate);

		return bean;
	}

	private Supplier<String> simpleDbSchemaUpdateContextProvider(Supplier<String> connectionUrlSupplier, Supplier<String> schemaSupplier,
			String tableNamePrefix) {
		SimpleDbSchemaUpdateContextProvider bean = new SimpleDbSchemaUpdateContextProvider();
		bean.setConnectionUrlSupplier(connectionUrlSupplier);
		bean.setSchemaSupplier(schemaSupplier);
		bean.setTableNamePrefix(tableNamePrefix);
		return bean;
	}

	private Supplier<String> simpleConnectionUrlProvider(DataSource dataSource) {
		SimpleConnectionUrlProvider bean = new SimpleConnectionUrlProvider();
		bean.setDataSource(dataSource);
		return bean;
	}

	private Supplier<String> simpleSchemaProvider(DataSource dataSource) {
		SimpleSchemaProvider bean = new SimpleSchemaProvider();
		bean.setDataSource(dataSource);
		return bean;
	}

	@Managed
	private GmExpertRegistry expertRegistry(@SuppressWarnings("unused") ExpertContext<HibernateAccess> context) {
		ConfigurableGmExpertRegistry bean = new ConfigurableGmExpertRegistry();
		bean.add(IdGenerator.class, String.class, new UuidGenerator());
		bean.add(IdGenerator.class, Date.class, new DateIdGenerator());
		return bean;
	}

	@Managed
	private HibernateConfigurationAdaptor configAdaptor(ExpertContext<HibernateAccess> context) {
		CompoundHibernateConfigurationAdaptor bean = new CompoundHibernateConfigurationAdaptor();
		// @formatter:off
		bean.setAdaptors(
				list(
					cacheNameAdaptor(context), 
					cacheFolderAdaptor(context)
				)
			);
		// @formatter:on
		return bean;
	}

	@Managed
	private HibernateConfigurationAdaptor cacheNameAdaptor(ExpertContext<HibernateAccess> context) {
		XPathAdaptor bean = new XPathAdaptor();
		bean.setValueMap(map(Maps.entry("/ehcache/@name", cacheName(context))));
		return bean;
	}

	private String cacheName(ExpertContext<HibernateAccess> context) {
		return "cacheManager-deployed-" + context.getDeployableExternalId();
	}

	@Managed
	private HibernateConfigurationAdaptor cacheFolderAdaptor(@SuppressWarnings("unused") ExpertContext<HibernateAccess> context) {
		try {
			TemporaryFolderCacheAdaptor bean = new TemporaryFolderCacheAdaptor();
			return bean;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error creating temporary folder cache adaptor");
		}
	}

	private void configureGeneratedMappings(ExpertContext<HibernateAccess> context, HibernateSessionFactoryBean bean, StopWatch stopWatch) {
		File mappingDirectory = mappingDirectory(context);
		bean.setMappingDirectoryLocations(mappingDirectory);

		DbUpdateStatementExecutor usExecutor = updateStatementExecutor(context, mappingDirectory);
		bean.addBeforeSchemaCreationTask(() -> usExecutor.runBeforeSchemaCreationTasks());
		bean.addAfterSchemaCreationTask(() -> usExecutor.runAfterSchemaCreationTasks());

		stopWatch.intermediate("Configured DbUpdateStatementExecutor");
	}

	private DbUpdateStatementExecutor updateStatementExecutor(ExpertContext<HibernateAccess> context, File mappingDirectory) {
		DbUpdateStatementExecutor bean = new DbUpdateStatementExecutor();
		bean.setContextDescription("HibernateAccess " + context.getDeployable().getExternalId());
		bean.setDataSource(resolveDataSource(context));
		bean.setMappingDirectory(mappingDirectory);

		return bean;
	}

	@Managed
	private File mappingDirectory(ExpertContext<HibernateAccess> context) {
		HibernateMappingsDirectorySupplier factory = hibernateMappingsDirectorySupplier(context);
		currentInstance().onDestroy(factory::preDestroy);

		return factory.get();
	}

	private HibernateMappingsDirectorySupplier hibernateMappingsDirectorySupplier(ExpertContext<HibernateAccess> context) {
		HibernateAccess deployable = context.getDeployable();

		HibernateMappingsDirectorySupplier factory = new HibernateMappingsDirectorySupplier();
		factory.setMetaModel(resolveModel(deployable));
		factory.setDefaultSchema(deployable.getDefaultSchema());
		factory.setDefaultCatalog(deployable.getDefaultCatalog());
		factory.setObjectNamePrefix(deployable.getObjectNamePrefix());
		factory.setTableNamePrefix(deployable.getTableNamePrefix());
		factory.setForeignKeyNamePrefix(deployable.getForeignKeyNamePrefix());
		factory.setUniqueKeyNamePrefix(deployable.getUniqueKeyNamePrefix());
		factory.setIndexNamePrefix(deployable.getIndexNamePrefix());
		factory.setCmdResolver(cmdResolver(context));
		factory.setCmdResolverFactory(this::newCmdResolver);
		factory.setDialect(resolveDataSourceInfo(context).dialect);

		return factory;
	}

	private CmdResolver cmdResolver(ExpertContext<HibernateAccess> context) {
		return modelAccessory(context.getDeployable()).getCmdResolver();
	}

	private GmMetaModel resolveModel(HibernateAccess deployable) {
		return modelAccessory(deployable).getModel();
	}

	private ModelAccessory modelAccessory(HibernateAccess deployable) {
		String accessId = deployable.getExternalId();

		return tfPlatform.systemUserRelated() //
				.modelAccessoryFactory() //
				.getForAccess(accessId);
	}

	private CmdResolverBuilder newCmdResolver(GmMetaModel model) {
		return tfPlatform.modelApi().newCmdResolver(model);
	}

	// ###############################################
	// ## . . . . . . . Dialect Info . . . . . . . .##
	// ###############################################

	private static class DataSoureInfo {
		public DataSource dataSource;
		public Class<? extends Dialect> dialectClass;
		public HibernateDialect dialect;
	}

	@Managed
	private DataSoureInfo resolveDataSourceInfo(ExpertContext<HibernateAccess> context) {
		HibernateAccess deployable = context.getDeployable();

		DataSoureInfo result = new DataSoureInfo();
		result.dataSource = resolveDataSource(context);
		result.dialectClass = resolveDialectClass(deployable, result.dataSource);
		result.dialect = resolveDialect(deployable, result.dialectClass);
		return result;
	}

	private DataSource resolveDataSource(ExpertContext<HibernateAccess> context) {
		return context.resolve(context.getDeployable().getConnector(), DatabaseConnectionPool.T);
	}

	private Class<? extends Dialect> resolveDialectClass(HibernateAccess deployable, DataSource dataSource) {
		HibernateDialect dialect = deployable.getDialect();
		if (dialect != null)
			return dialect(dialect);

		Class<? extends Dialect> result = dialect(dataSource);
		logger.debug("Auto-detected dialect " + result.getClass().getSimpleName() + " for hibernate access: " + deployable.getExternalId());
		return result;
	}

	private HibernateDialect resolveDialect(HibernateAccess deployable, Class<? extends Dialect> dialectClass) {
		HibernateDialect result = deployable.getDialect();
		if (result != null)
			return result;

		try {
			return HibernateDialect.valueOf(dialectClass.getSimpleName());
		} catch (RuntimeException e) {
			return null;
		}
	}

	private Class<? extends Dialect> dialect(DataSource dataSource) {
		try {
			return dialectAutoSense().senseDialect(dataSource);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to detect dialect based on " + dataSource);
		}
	}

	private Class<? extends Dialect> dialect(HibernateDialect hibernateDialect) {
		return HibernateDialectMappings.loadDialect(hibernateDialect.name());
	}

	@Managed
	private DialectAutoSense<? extends Dialect> dialectAutoSense() {
		DialectAutoSense<? extends Dialect> bean = new DialectAutoSense<>();
		bean.setDialectMappings(mappings());
		return bean;
	}

	private List<DialectMapping> mappings() {
		return HibernateDialectMappings.mapppings().stream() //
				.map(this::toDialectMapping)//
				.collect(Collectors.toList());
	}

	private DialectMapping toDialectMapping(HibernateDialectMapping hibernateMapping) {
		DialectMapping bean = new DialectMapping();
		bean.setProductMatcher(Pattern.compile(hibernateMapping.productRegex));
		bean.setVariant(hibernateMapping.variant);
		bean.setDialect(hibernateMapping.dialectType.getName());
		return bean;
	}

}
