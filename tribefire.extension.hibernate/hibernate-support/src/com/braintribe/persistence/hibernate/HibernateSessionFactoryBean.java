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
package com.braintribe.persistence.hibernate;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.persistence.hibernate.adaptor.HibernateConfigurationAdaptor;
import com.braintribe.persistence.hibernate.adaptor.TemporaryFolderCacheAdaptor;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.StopWatch;

import net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory;

/**
 * Factory for hibernate {@link SessionFactory} instances. It uses a {@link DataSource} to provide the actual
 * {@link Connection}.
 *
 * @see #setDataSource(DataSource)
 *
 *      All important hibernate properties can be configured typesafe while meaningful defaults are already set for non
 *      required properties.
 *
 * @see #setCurrentSessionContextClass(Class)
 * @see #setDialect(Class)
 * @see #setHbm2DdlAuto(String)
 * @see #setMappingLocations(URL...)
 * @see #setShowSql(boolean)
 * @see #setUseQueryCache(boolean)
 * @see #setUseSecondLevelCache(boolean)
 *
 * @author Dirk
 */
public class HibernateSessionFactoryBean extends LocalSessionFactoryBean
		implements Supplier<SessionFactory>, LifecycleAware /* , BeanNameAware, ConfigurationObserver */ {

	protected static Logger logger = Logger.getLogger(HibernateSessionFactoryBean.class);

	private static Map<String, HibernateSessionFactoryBean> factories = new ConcurrentHashMap<String, HibernateSessionFactoryBean>();
	private final String id = UUID.randomUUID().toString();

	private static void register(HibernateSessionFactoryBean bean) {
		factories.put(bean.getId(), bean);
	}

	private static void unregister(HibernateSessionFactoryBean bean) {
		factories.remove(bean.getId());
	}

	private static HibernateSessionFactoryBean getSessionFactoryBean(String id) {
		return factories.get(id);
	}

	private final Properties properties = new Properties();

	private DataSource dataSource;

	private String beanName;

	protected File ehCacheConfiguration = null;
	protected File temporaryEhCacheConfiguration = null;
	protected HibernateConfigurationAdaptor configAdaptor = null;

	protected boolean updateSchema = true;
	protected final List<Runnable> beforeSchemaCreationTasks = newList();
	protected final List<Runnable> afterSchemaCreationTasks = newList();

	/**
	 * default constructor
	 */
	public HibernateSessionFactoryBean() {
		register(this);
		properties.setProperty(Environment.CONNECTION_PROVIDER, ConnectionProviderImpl.class.getName());
		properties.setProperty("factoryBeanId", getId());

		// caching
		setRegionFactory("ehcache-singleton");
		setUseQueryCache(true);
		setUseSecondLevelCache(true);

		// update schema if desired
		setHbm2DdlAuto("update");
	}

	@Override
	public void preDestroy() {

		try {
			super.closeFactory();
		} catch (Exception e) {
			logger.error("Could not destroy the session factory", e);
		}

		unregister(this);

		if (this.temporaryEhCacheConfiguration != null) {
			try {
				if (this.temporaryEhCacheConfiguration.exists()) {
					this.temporaryEhCacheConfiguration.deleteOnExit();
					this.temporaryEhCacheConfiguration = null;
				}
			} catch (Exception e) {
				logger.error("Could not delete " + this.temporaryEhCacheConfiguration, e);
			}
		}
		if (this.configAdaptor != null) {
			this.configAdaptor.cleanup();
		}
	}

	@Override
	public void postConstruct() {
		run(beforeSchemaCreationTasks);
		
		setHbm2DdlAuto(updateSchema ? "update" : "none");

		afterPropertiesSet();

		run(afterSchemaCreationTasks);
	}

	private void run(List<Runnable> tasks) {
		for (Runnable r : tasks)
			r.run();
	}

	@Override
	public void afterPropertiesSet() {

		StopWatch stopWatch = new StopWatch();

		if (this.configAdaptor == null) {
			try {
				this.configAdaptor = new TemporaryFolderCacheAdaptor();

			} catch (Exception e) {
				logger.error("Could not initialize the default TemporaryFolderCacheAdaptor", e);
				this.configAdaptor = null;
			}
		}

		if (this.ehCacheConfiguration == null) {
			FileOutputStream fos = null;
			try {
				URL ehCfgResource = getClass().getResource("ehcache.xml");
				ReadableByteChannel rbc = Channels.newChannel(ehCfgResource.openStream());
				this.ehCacheConfiguration = File.createTempFile("ehcache", ".xml");
				this.temporaryEhCacheConfiguration = this.ehCacheConfiguration;
				fos = new FileOutputStream(this.ehCacheConfiguration);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			} catch (Exception e) {
				logger.debug("Cannot access resource ehcache.xml.", e);
				this.ehCacheConfiguration = null;

			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {
						logger.error("Could not close output stream to " + this.ehCacheConfiguration, e);
					}
				}
			}
		}

		stopWatch.intermediate("ehcache.xml creation");

		if (this.ehCacheConfiguration != null) {

			if (this.configAdaptor != null) {
				try {
					this.configAdaptor.adaptEhCacheConfigurationResource(this.ehCacheConfiguration);
				} catch (Exception e) {
					logger.error("Could not adapt Hibernate configuration paths.", e);
				}
			}

			try {
				String location = this.ehCacheConfiguration.toURI().toURL().toString();
				properties.setProperty(SingletonEhCacheRegionFactory.NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME, location);
			} catch (Exception e) {
				logger.error("Could not set EHCache configuration file.", e);
			}
		}

		stopWatch.intermediate("Config Adapter");

		super.setHibernateProperties(properties);
		try {
			super.afterPropertiesSet();

		} catch (Exception e) {
			if (exceptionToText(e).contains("Table 'test.all_sequences' doesn't exist")) {
				logger.debug("Database does not contain an all_sequences table. Ignoring this fact.");
			} else {
				throw new RuntimeException("Error while trying to invoke super.afterPropertiesSet.", e);
			}
		}

		stopWatch.intermediate("Build Session Factory");

		logger.debug(() -> "Initialized hibernate session factory for bean " + this.beanName + ": " + stopWatch);
	}

	private String exceptionToText(Exception e) {
		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		return sw.toString();
	}

	private String getId() {
		return id;
	}

	public Properties getProperties() {
		return properties;
	}

	@Configurable
	public void setUpdateSchema(boolean updateSchema) {
		this.updateSchema = updateSchema;
	}

	/** Applied <b>before</b> the schema modification of Hibernate is done. */
	@Configurable
	public void addBeforeSchemaCreationTask(Runnable r) {
		beforeSchemaCreationTasks.add(r);
	}

	/** Applied <b>after</b> the schema modification of Hibernate is done. */
	@Configurable
	public void addAfterSchemaCreationTask(Runnable r) {
		afterSchemaCreationTasks.add(r);
	}

	@Required
	public void setDialect(Class<? extends Dialect> dialectClass) {
		properties.setProperty(Environment.DIALECT, dialectClass.getName());
	}

	public void setAutoCommit(boolean autoCommit) {
		properties.setProperty(Environment.AUTOCOMMIT, "" + autoCommit);
	}

	/**
	 * optional configuration when using ehcache as a cache or region provider
	 */
	public void setEhCacheConfiguration(File ehCacheConfigurationLocation) {
		this.ehCacheConfiguration = ehCacheConfigurationLocation;
	}

	@Override
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setShowSql(boolean showSql) {
		properties.setProperty(Environment.SHOW_SQL, Boolean.toString(showSql));
	}

	public void setFormatSql(boolean formatSql) {
		properties.setProperty(Environment.FORMAT_SQL, Boolean.toString(formatSql));
	}

	public boolean getShowSql() {
		String data = properties.getProperty(Environment.SHOW_SQL);
		if (data == null)
			return false;

		return Boolean.parseBoolean(data);
	}

	public void setHbm2DdlAuto(String mode) {
		properties.setProperty(Environment.HBM2DDL_AUTO, mode);
	}

	public String getHbm2DdlAuto() {
		return properties.getProperty(Environment.HBM2DDL_AUTO);
	}

	public void setRegionFactory(Class<? extends org.hibernate.cache.spi.RegionFactory> regionFactoryClass) {
		properties.setProperty(Environment.CACHE_REGION_FACTORY, regionFactoryClass.getName());
	}

	public void setRegionFactory(String regionFactoryClass) {
		properties.setProperty(Environment.CACHE_REGION_FACTORY, regionFactoryClass);
	}

	public void setCurrentSessionContextClass(Class<? extends CurrentSessionContext> currentSessionContextClass) {
		properties.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, currentSessionContextClass.getName());
	}

	public String getCurrentSessionContextClass() {
		return properties.getProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS);
	}

	public void setUseQueryCache(boolean useQueryCache) {
		properties.setProperty(Environment.USE_QUERY_CACHE, Boolean.toString(useQueryCache));
	}

	public boolean getUseQueryCache() {
		String data = properties.getProperty(Environment.USE_QUERY_CACHE);
		if (data == null)
			return false;

		return Boolean.parseBoolean(data);
	}

	public void setUseSecondLevelCache(boolean useSecondLevenCache) {
		properties.setProperty(Environment.USE_SECOND_LEVEL_CACHE, Boolean.toString(useSecondLevenCache));
	}

	public boolean getUseSecondLevelCache() {
		String data = properties.getProperty(Environment.USE_SECOND_LEVEL_CACHE);
		if (data == null)
			return false;

		return Boolean.parseBoolean(data);
	}

	public void setDefaultSchema(String defaultSchema) {
		updateProperty(Environment.DEFAULT_SCHEMA, defaultSchema);
	}

	public String getDefaultSchema() {
		return properties.getProperty(Environment.DEFAULT_SCHEMA);
	}

	public void setDefaultCatalog(String defaultCatalog) {
		updateProperty(Environment.DEFAULT_CATALOG, defaultCatalog);
	}

	public void setUseJdbcMetadataDefaults(boolean useJdbcMetadataDefaults) {
		properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", Boolean.toString(useJdbcMetadataDefaults));
	}

	public boolean getUseJdbcMetadataDefaults() {
		String data = properties.getProperty("hibernate.temp.use_jdbc_metadata_defaults");
		if (data == null)
			return false;

		return Boolean.parseBoolean(data);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDefaultBatchFetchSize(int defaultBatchFetchSize) {
		properties.setProperty(Environment.DEFAULT_BATCH_FETCH_SIZE, String.valueOf(defaultBatchFetchSize));
	}

	/**
	 * <p>
	 * Sets additional generic hibernate properties based on the given map.
	 * 
	 * <p>
	 * {@code null} parameters are ignored, just like map entries with {@code null} keys or values.
	 */
	public void setAdditionalProperties(Map<String, String> additionalProperties) {
		if (isEmpty(additionalProperties))
			return;

		Map<String, Object> runtimeProperties = new HashMap<>();
		runtimeProperties.putAll(CommonTools.getSystemProperties());
		runtimeProperties.putAll(CommonTools.getEnvironmentVariables());
		Set<String> propertyNames = TribefireRuntime.getPropertyNames();
		for (String propName : propertyNames) {
			runtimeProperties.put(propName, TribefireRuntime.getProperty(propName));
		}

		for (Map.Entry<String, String> additionalProperty : additionalProperties.entrySet()) {
			String key = additionalProperty.getKey();
			String value = additionalProperty.getValue();
			if (key == null || value == null) {
				logger.warn("Ignoring invalid property. key=" + key + " value=" + value);
				continue;
			}
			try {
				value = StringTools.patternFormat(value, runtimeProperties);
			} catch (Exception e) {
				logger.info("Could not use " + value + " as a pattern. If this is an error, please check the configuration of this connection.", e);
				// ignore exception; take value as-is
			}

			properties.setProperty(key, value);
		}

	}

	public static class ConnectionProviderImpl implements ConnectionProvider, org.hibernate.service.spi.Configurable {

		private static final long serialVersionUID = 4158910239835530985L;

		private DataSource dataSource;

		@Override
		public void closeConnection(Connection connection) throws SQLException {
			connection.close();
		}

		@Override
		public void configure(Map properties) throws HibernateException {
			String id = (String) properties.get("factoryBeanId");
			HibernateSessionFactoryBean sessionFactoryBean = getSessionFactoryBean(id);
			dataSource = sessionFactoryBean.getDataSource();
		}

		@Override
		public Connection getConnection() throws SQLException {
			return dataSource.getConnection();
		}

		@Override
		public boolean supportsAggressiveRelease() {
			return false;
		}

		@Override
		public boolean isUnwrappableAs(Class unwrapType) {
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> unwrapType) {
			throw new UnsupportedOperationException("Method 'HibernateSessionFactoryBean.ConnectionProviderImpl.unwrap' is not supported!");
		}

	}

	public HibernateConfigurationAdaptor getConfigAdaptor() {
		return configAdaptor;
	}

	@Configurable
	public void setConfigAdaptor(HibernateConfigurationAdaptor configAdaptor) {
		this.configAdaptor = configAdaptor;
	}

	@Override
	public SessionFactory get() {
		return getObject();
	}

	protected Object updateProperty(String name, Object value) {
		Objects.requireNonNull(name, "property name");

		if (value == null)
			return properties.remove(name);
		else
			return properties.put(name, value);
	}

}
