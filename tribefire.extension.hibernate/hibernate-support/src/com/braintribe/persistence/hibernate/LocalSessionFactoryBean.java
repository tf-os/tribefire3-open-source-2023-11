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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import com.braintribe.common.lcd.function.CheckedSupplier;

/**
 * A temporary clone of org.springframework.orm.hibernate4.LocalSessionFactoryBean 
 * tobe used while the dependency to spring is being removed.
 */
public class LocalSessionFactoryBean {

	private DataSource dataSource;
	private URL[] configLocations;
	private URL[] mappingLocations;
	private List<Supplier<InputStream>> mappingInputSuppliers;
	private File[] cacheableMappingLocations;
	private File[] mappingJarLocations;
	private File[] mappingDirectoryLocations;
	private Interceptor entityInterceptor;
	private Properties hibernateProperties;
	private ClassLoader classLoader;
	private Configuration configuration;
	private SessionFactory sessionFactory;

	/**
	 * Set the DataSource to be used by the SessionFactory.
	 * If set, this will override corresponding settings in Hibernate properties.
	 * <p>If this is set, the Hibernate settings should not define
	 * a connection provider to avoid meaningless double configuration.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set the location of a single Hibernate XML config file, for example as
	 * classpath resource "classpath:hibernate.cfg.xml".
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean.
	 * @see org.hibernate.cfg.Configuration#configure(java.net.URL)
	 */
	public void setConfigLocation(URL configLocation) {
		this.configLocations = new URL[] {configLocation};
	}

	/**
	 * Set the locations of multiple Hibernate XML config files, for example as
	 * classpath resources "classpath:hibernate.cfg.xml,classpath:extension.cfg.xml".
	 * <p>Note: Can be omitted when all necessary properties and mapping
	 * resources are specified locally via this bean.
	 * @see org.hibernate.cfg.Configuration#configure(java.net.URL)
	 */
	public void setConfigLocations(URL... configLocations) {
		this.configLocations = configLocations;
	}

	/**
	 * Set locations of Hibernate mapping files, for example as classpath
	 * resource "classpath:example.hbm.xml". Supports any resource location
	 * via Spring's resource abstraction, for example relative paths like
	 * "WEB-INF/mappings/example.hbm.xml" when running in an application context.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addInputStream
	 */
	public void setMappingLocations(URL... mappingLocations) {
		this.mappingLocations = mappingLocations;
	}

	public void setMappingInputSuppliers(List<Supplier<InputStream>> mappingInputSuppliers) {
		this.mappingInputSuppliers = mappingInputSuppliers;
	}

	/**
	 * Set locations of cacheable Hibernate mapping files, for example as web app
	 * resource "/WEB-INF/mapping/example.hbm.xml". Supports any resource location
	 * via Spring's resource abstraction, as long as the resource can be resolved
	 * in the file system.
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addCacheableFile(java.io.File)
	 */
	public void setCacheableMappingLocations(File... cacheableMappingLocations) {
		this.cacheableMappingLocations = cacheableMappingLocations;
	}

	/**
	 * Set locations of jar files that contain Hibernate mapping resources,
	 * like "WEB-INF/lib/example.hbm.jar".
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addJar(java.io.File)
	 */
	public void setMappingJarLocations(File... mappingJarLocations) {
		this.mappingJarLocations = mappingJarLocations;
	}

	/**
	 * Set locations of directories that contain Hibernate mapping resources,
	 * like "WEB-INF/mappings".
	 * <p>Can be used to add to mappings from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 * @see org.hibernate.cfg.Configuration#addDirectory(java.io.File)
	 */
	public void setMappingDirectoryLocations(File... mappingDirectoryLocations) {
		this.mappingDirectoryLocations = mappingDirectoryLocations;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this factory.
	 * @see org.hibernate.cfg.Configuration#setInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Set Hibernate properties, such as "hibernate.dialect".
	 * <p>Note: Do not specify a transaction provider here when using
	 * Spring-driven transactions. It is also advisable to omit connection
	 * provider settings and use a Spring-set DataSource instead.
	 * @see #setDataSource
	 */
	public void setHibernateProperties(Properties hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	/** ClassLoader */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	/**
	 * Return the Hibernate properties, if any. Mainly available for
	 * configuration through property paths that specify individual keys.
	 */
	public Properties getHibernateProperties() {
		if (hibernateProperties == null)
			hibernateProperties = new Properties();
		return hibernateProperties;
	}

	public void afterPropertiesSet() {
		Configuration cfg = new Configuration(bootstrapServiceRegistry());
		
		if (dataSource != null)
			cfg.getProperties().put(Environment.DATASOURCE, dataSource);

		if (configLocations != null)
			// Load Hibernate configuration from given location.
			for (URL url: configLocations)
				cfg.configure(url);

		if (mappingLocations != null)
			// Register given Hibernate mapping definitions, contained in resource files.
			for (URL url : mappingLocations)
				cfg.addInputStream(CheckedSupplier.uncheckedGet(url::openStream));

		if (mappingInputSuppliers != null)
			// Register given Hibernate mapping definitions, directly from InputStreams
			for (Supplier<InputStream> supplier : mappingInputSuppliers)
				cfg.addInputStream(supplier.get());

		if (cacheableMappingLocations != null)
			// Register given cacheable Hibernate mapping definitions, read from the file system.
			for (File file: cacheableMappingLocations)
				cfg.addCacheableFile(file);

		if (mappingJarLocations != null)
			// Register given Hibernate mapping definitions, contained in jar files.
			for (File file: mappingJarLocations)
				cfg.addJar(file);

		if (mappingDirectoryLocations != null)
			// Register all Hibernate mapping definitions in the given directories.
			for (File file : mappingDirectoryLocations)
				cfg.addDirectory(requireDirectory(file));

		if (entityInterceptor != null)
			cfg.setInterceptor(entityInterceptor);

		if (hibernateProperties != null)
			cfg.addProperties(hibernateProperties);

		// Build SessionFactory instance.
		configuration = cfg;
		sessionFactory = cfg.buildSessionFactory();
	}

	private BootstrapServiceRegistry bootstrapServiceRegistry() {
		BootstrapServiceRegistryBuilder result = new BootstrapServiceRegistryBuilder();
		if (classLoader != null)
			result.applyClassLoader(classLoader);

		return result.build();
	}

	private File requireDirectory(File file) {
		if (!file.isDirectory())
			throw new IllegalArgumentException("Mapping directory location [" + file + "] does not denote a directory");
		return file;
	}

	/**
	 * Return the Hibernate Configuration object used to build the SessionFactory.
	 * Allows for access to configuration metadata stored there (rarely needed).
	 * @throws IllegalStateException if the Configuration object has not been initialized yet
	 */
	public final Configuration getConfiguration() {
		return requireNonNull(configuration, "Configuration not initialized yet");
	}

	public SessionFactory getObject() {
		if (sessionFactory == null)
			afterPropertiesSet();
		
		return sessionFactory;
	}

	public void closeFactory() {
		if (sessionFactory != null)
			sessionFactory.close();
	}

}
