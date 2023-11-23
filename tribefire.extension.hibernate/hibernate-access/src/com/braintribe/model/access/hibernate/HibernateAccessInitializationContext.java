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
package com.braintribe.model.access.hibernate;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.Interceptor;
import org.hibernate.dialect.Dialect;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.utils.MapTools;

public class HibernateAccessInitializationContext {

	private Map<String, String> hibernateConfigurationProperties = newMap();
	private List<File> hibernateMappingsFolders;
	private List<URL> hibernateMappingsResources;
	private String modelName;
	private Map<Class<? extends GenericEntity>, IdGenerator> idGenerators;
	private Interceptor interceptor;

	public void addHibernateConfigurationProperties(final String... keysAndValues) {
		getHibernateConfigurationProperties().putAll(MapTools.decodeEntries(Arrays.asList(keysAndValues), "="));
	}

	public void addHibernateConfigurationProperty(final String key, final String value) {
		getHibernateConfigurationProperties().put(key, value);
	}

	public void setConnectionDriver(final Class<?> connectionDriver) {
		addHibernateConfigurationProperty("hibernate.connection.driver_class", connectionDriver.getName());
	}

	public void setConnectionUrl(final String connectionUrl) {
		addHibernateConfigurationProperty("hibernate.connection.url", connectionUrl);
	}

	public void setConnectionUsername(final String connectionUsername) {
		addHibernateConfigurationProperty("hibernate.connection.username", connectionUsername);
	}

	public void setConnectionPassword(final String connectionPassword) {
		addHibernateConfigurationProperty("hibernate.connection.password", connectionPassword);
	}

	public void setDialect(final Class<? extends Dialect> dialect) {
		addHibernateConfigurationProperty("hibernate.dialect", dialect.getName());
	}

	public Map<String, String> getHibernateConfigurationProperties() {
		return this.hibernateConfigurationProperties;
	}

	public void setHibernateConfigurationProperties(final Map<String, String> hibernateConfigurationProperties) {
		this.hibernateConfigurationProperties = hibernateConfigurationProperties;
	}

	public List<File> getHibernateMappingsFolders() {
		return this.hibernateMappingsFolders;
	}

	public void setHibernateMappingsFolders(final List<File> hibernateMappingsFolders) {
		this.hibernateMappingsFolders = hibernateMappingsFolders;
	}

	public void addHibernateMappingsFolder(final File hibernateMappingsFolder) {
		if (getHibernateMappingsFolders() == null)
			setHibernateMappingsFolders(newList());
		
		getHibernateMappingsFolders().add(hibernateMappingsFolder);
	}

	public List<URL> getHibernateMappingsResources() {
		return this.hibernateMappingsResources;
	}

	public void setHibernateMappingsResources(final List<URL> hibernateMappingsResources) {
		this.hibernateMappingsResources = hibernateMappingsResources;
	}

	public void addHibernateMappingsResource(final URL hibernateMappingsResource) {
		if (getHibernateMappingsResources() == null)
			setHibernateMappingsResources(newList());

		getHibernateMappingsResources().add(hibernateMappingsResource);
	}

	public String getModelName() {
		return this.modelName;
	}

	public void setModelName(final String modelName) {
		this.modelName = modelName;
	}

	public Map<Class<? extends GenericEntity>, IdGenerator> getIdGenerators() {
		return this.idGenerators;
	}

	public void setIdGenerators(final Map<Class<? extends GenericEntity>, IdGenerator> idGenerators) {
		this.idGenerators = idGenerators;
	}

	public Interceptor getInterceptor() {
		return this.interceptor;
	}

	public void setInterceptor(final Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public String toString() {
		return "HibernateAccessInitializationContext [hibernateConfigurationProperties="
				+ this.hibernateConfigurationProperties + ", hibernateMappingsFolders=" + this.hibernateMappingsFolders
				+ ", hibernateMappingsResources=" + this.hibernateMappingsResources + ", modelName=" + this.modelName
				+ ", idGenerators=" + this.idGenerators + ", interceptor=" + this.interceptor + "]";
	}

}
