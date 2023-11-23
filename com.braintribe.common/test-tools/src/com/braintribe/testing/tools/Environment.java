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
package com.braintribe.testing.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.MapTools;

/**
 * The <code>Environment</code> can be used to run integration tests (that depend on external components, e.g. database, external system like
 * Documentum, files, etc.) in multiple environments (e.g. with different database connection settings). Basically it's just a convenience class that
 * holds environment settings that are read from environment specific {@link Properties} files (and a {@link #DEFAULTENVIRONMENT_ENVIRONMENTID
 * default} properties file as fallback).
 * <p>
 * Properties files must be placed in {@link #getEnvironmentFolder() environment folder}, by default {@value #ENVIRONMENTSETTINGS_FOLDER_DEFAULT}.
 * Properties files are named <code>[PREFIX][ENVIRONMEN_ID].properties</code>. The prefix can be {@link #setEnvironmentFileNamePrefix(String)
 * configured}. The environment id is specified via {@link #setEnvironmentIdProperty(String) system property}.
 * <p>
 * One can access properties via method {@link #getProperty(String)}.
 *
 * @author michael.lafite
 */
public class Environment {

	private static Logger logger = Logger.getLogger(Environment.class);

	private static final String DEFAULTENVIRONMENT_ENVIRONMENTID = "default";

	private static final String PROPERTY_ENVIRONMENTID_DEFAULT = "BT__ENVIRONMENT_ID";
	private static final String ENVIRONMENTSETTINGS_FOLDER_DEFAULT = "res-junit/environment";
	private static final String ENVIRONMENTSETTINGS_FILENAMEPREFIX_DEFAULT = "environment_";

	private String environmentIdProperty = PROPERTY_ENVIRONMENTID_DEFAULT;
	private String environmentFolder = ENVIRONMENTSETTINGS_FOLDER_DEFAULT;
	private String environmentFileNamePrefix = ENVIRONMENTSETTINGS_FILENAMEPREFIX_DEFAULT;

	private Map<String, String> properties;

	private Collection<String> mandatoryProperties;

	private boolean initialized = false;

	public Environment() {
		// nothing to do
	}

	public void initialize() {

		this.properties = new TreeMap<>();
		// first read the default environment settings
		final Map<String, String> defaultEnvironmentProperties = readPropertiesFileIfItExists(
				getPropertiesFilePath(DEFAULTENVIRONMENT_ENVIRONMENTID));
		this.properties.putAll(defaultEnvironmentProperties);

		// get environment id (first check system properties, then environment properties)
		String environmentId = System.getProperty(getEnvironmentIdProperty());
		if (environmentId == null) {
			environmentId = System.getenv(getEnvironmentIdProperty());
		}

		if (environmentId != null) {
			// get custom environment properties and override defaults
			final Map<String, String> customEnvironmentProperties = readPropertiesFileIfItExists(getPropertiesFilePath(environmentId));
			this.properties.putAll(customEnvironmentProperties);
		}

		List<String> missingMandatoryProperties = CollectionTools.getMissingElements(this.properties.keySet(), getMandatoryProperties());

		if (!missingMandatoryProperties.isEmpty()) {
			throw new GenericRuntimeException("The following mandatory properties are not available: " + missingMandatoryProperties);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(getClass().getSimpleName() + " initialized successfully. Properties: " + this.properties);
		}

		this.initialized = true;
	}

	private void initializeIfRequired() {
		if (!this.initialized) {
			initialize();
		}
	}

	private String getPropertyWithoutCheck(final String name) {
		initializeIfRequired();

		final String propertyValue = this.properties.get(name);
		return propertyValue;
	}

	public String getProperty(final String name) {
		final String propertyValue = getPropertyWithoutCheck(name);

		if (propertyValue == null) {
			throw new IllegalArgumentException(
					"Property not available! " + CommonTools.getParametersString("property", name, "available properties", getPropertyNames()));
		}
		return propertyValue;
	}

	public String getProperty(final String name, String defaultValue) {
		String value = getPropertyWithoutCheck(name);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	public Collection<String> getMandatoryProperties() {
		return this.mandatoryProperties;
	}

	public void setMandatoryProperties(Collection<String> mandatoryProperties) {
		this.mandatoryProperties = mandatoryProperties;
	}

	public Map<String, String> getProperties() {
		initializeIfRequired();
		return this.properties;
	}

	public List<String> getPropertyNames() {
		initializeIfRequired();
		return new ArrayList<>(this.properties.keySet());
	}

	private String getPropertiesFilePath(final String environmentID) {
		return getEnvironmentFolder() + "/" + getEnvironmentFileNamePrefix() + environmentID + ".properties";
	}

	private static Map<String, String> readPropertiesFileIfItExists(final String filePath) {
		final File file = new File(filePath);
		if (!file.exists()) {
			return new HashMap<>();
		}
		return MapTools.readPropertiesFile(filePath);
	}

	public String getEnvironmentIdProperty() {
		return this.environmentIdProperty;
	}

	public Environment setEnvironmentIdProperty(final String environmentIdProperty) {
		this.environmentIdProperty = environmentIdProperty;
		return this;
	}

	public String getEnvironmentFolder() {
		return this.environmentFolder;
	}

	public Environment setEnvironmentFolder(String environmentFolder) {
		this.environmentFolder = environmentFolder;
		return this;
	}

	public String getEnvironmentFileNamePrefix() {
		return this.environmentFileNamePrefix;
	}

	public Environment setEnvironmentFileNamePrefix(String environmentFileNamePrefix) {
		this.environmentFileNamePrefix = environmentFileNamePrefix;
		return this;
	}

	@Override
	public String toString() {
		return Environment.class.getSimpleName() + "[environmentIdProperty=" + this.environmentIdProperty + ", environmentFolder="
				+ this.environmentFolder + ", environmentFileNamePrefix=" + this.environmentFileNamePrefix + ", properties=" + this.properties
				+ ", initialized=" + this.initialized + "]";
	}

}
