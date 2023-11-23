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
package com.braintribe.cartridge.common.processing.bootstrapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import com.braintribe.config.configurator.Configurator;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.config.configurator.ConfiguratorPriority;
import com.braintribe.config.configurator.ConfiguratorPriority.Level;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.IOTools;

/**
 * This implementation supports to specify Tribefire Runtime properties in a separate file.
 * 
 * When the tribefire services or a cartridge is loaded, the following functionality is in place:
 *
 * 1) When a tribefire runtime property "TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION" is specified in catalina.properties,
 *    the file identified by this property will be loaded and treated as a properties file 
 *    (key/values pairs, line by line, separated by either "=" or ":")
 *
 * 2) When TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION is not specified, the default location conf/tribefire.properties will be loaded (if available).
 *
 * 3) When TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION is specified, it may refer to a file, a URL where the properties can be downloaded or a classpath resource.
 *
 *
 */
@ConfiguratorPriority(value=Level.high, order=-90) // Mark this configurator with high priority to ensure it's running as first configurator.
public class ExternalPropertiesConfigurator implements Configurator {

	private static final Logger logger = Logger.getLogger(ExternalPropertiesConfigurator.class);

	protected final static String DEFAULT_FILENAME = "tribefire.properties";
	
	@Override
	public void configure() throws ConfiguratorException {

		boolean debug = logger.isDebugEnabled();
		
		String rootDir = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR);
		if (rootDir == null) {
			logger.debug(() -> "No "+TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR+" property defined. Trying to use the working folder.");
			rootDir = ".";
		}
		
		// CORETS-182: changing default location of the conf folder. 
		// This was already wrong in old versions of this class but additionally some magic tryAlternatives method tried to identifiy 
		// the location on multiple higher folder levels. this was intentionally removed.
		// So, now the only checked default location is ${CONTAINER_ROOT_DIR/../../conf
		Path confDir = Paths.get(rootDir, "../../conf");   
		
		String externalLocation = TribefireRuntime.getResolvedProperty(TribefireRuntime.ENVIRONMENT_EXTERNAL_PROPERTIES_LOCATION).orElseGet(() -> null);
		File externalFile = null;
		
		if (externalLocation == null || externalLocation.trim().length() == 0) {

			File propertiesFile = confDir.resolve(DEFAULT_FILENAME).toFile();
			logger.info(() -> "No "+TribefireRuntime.ENVIRONMENT_EXTERNAL_PROPERTIES_LOCATION+" property defined. Trying to use the default location: '"+propertiesFile.getAbsolutePath()+"'");
			
			if (!propertiesFile.exists())
				return;
			
			externalFile = propertiesFile;
			
		} else {
			
			logger.info(() -> "Trying to locate external properties at: '"+externalLocation+"'");
			externalFile = resolveExternalPropertiesFile(externalLocation, confDir);
			
		}
		
		boolean success = false;
		
		if (externalFile != null && externalFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(externalFile);
				if (debug) logger.debug("Loading external properties from file: '"+externalFile.getAbsolutePath()+"'");
				this.loadExternalProperties(fis);
				success = true;
			} catch(Exception e) {
				logger.error("Could not load external properties from: '"+externalFile.getAbsolutePath()+"'", e);
			} finally {
				IOTools.closeCloseable(fis, logger);
			}
		} else {
			logger.debug(() -> "The location "+externalLocation+" does not exist as a file. Trying to interpret it as a URL.");
			InputStream is = null;
			try {
				URL url = new URL(externalLocation);
				is = url.openStream();
				if (is != null) {
					logger.debug(() -> "Loading external properties from the URL: '"+externalLocation+"'");
					this.loadExternalProperties(is);
					success = true;
				}
			} catch (Exception e) {
				logger.trace(() -> "Could not interpret the location: '"+externalLocation+"' as a URL.", e);
			} finally {
				IOTools.closeCloseable(is, logger);
			}
			if (!success) {
				logger.debug(() -> "The location "+externalLocation+" is neither a file path nor a URL. Trying to load it as a classpath resource.");
				try {
					ClassLoader classLoader = this.getClass().getClassLoader();
					InputStream resourceAsStream = classLoader.getResourceAsStream(externalLocation);
					if (resourceAsStream != null) {
						logger.debug(() -> "Loading external properties from the classpath resource: '"+externalLocation+"'");
						this.loadExternalProperties(resourceAsStream);
						success = true;
					}
				} catch(Exception e) {
					logger.debug(() -> "Could not load: '"+externalLocation+"' as a classpath resource.", e);
				}
			}
		}
		
		if (debug) {
			if (!success) {
				logger.debug("Could not find ANY way of how to deal with the path: '"+externalLocation+"'");
			} 
		}

	}

	protected File resolveExternalPropertiesFile(final String externalLocation, Path confDir) {
		final File f = new File(externalLocation);
		File resultingFile = null;

		if (f.isAbsolute()) {
			logger.debug(() -> "The file path '" + f.getAbsolutePath() + "' is absolute.");

			resultingFile = f;
		}
		else {
			logger.debug(() -> "The file path '" + f.getPath() + "' is relative. Using confDir as base: '" + confDir.toAbsolutePath().toString()+"'");
			
			resultingFile = confDir.resolve(externalLocation).toFile();
		}

		if (resultingFile.exists())
			return resultingFile;
		
		logger.debug(() -> "Could not find any file by the reference: '"+externalLocation+"'");
		return null;
	}

	protected void loadExternalProperties(InputStream fis) throws Exception {
		
		Properties props = new Properties();
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(fis, "UTF-8");
			props.load(isr);
		} finally {
			IOTools.closeCloseable(isr, logger);
		}
		logger.debug(() -> "Found "+props.size()+" properties: "+props);
		
		for (Map.Entry<Object,Object> entry : props.entrySet()) {
			String name = (String) entry.getKey();
			String value = (String) entry.getValue();
			logger.debug(() -> "Setting the tribefire Runtime property: "+name+"="+value);
			TribefireRuntime.setProperty(name, value);
		}
	}
	
	@Override
	public String toString() {
		String externalLocation = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_EXTERNAL_PROPERTIES_LOCATION);
		String rootDir = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR);
		StringBuilder sb = new StringBuilder("ExternalPropertiesConfigurator (");
		sb.append("external location: ");
		sb.append(externalLocation != null ? externalLocation : "<not set>");
		sb.append(", container root: ");
		sb.append(rootDir != null ? rootDir : "<not set>");
		sb.append(')');
		return sb.toString();
	}

}
