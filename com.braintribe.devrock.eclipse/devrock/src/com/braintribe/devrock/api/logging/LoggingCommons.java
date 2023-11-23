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
package com.braintribe.devrock.api.logging;

import java.io.File;
import java.util.Optional;

import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;
import com.braintribe.logging.LoggerInitializer;

/**
 * helpers for logging 
 * @author pit
 *
 */
public class LoggingCommons {
	private static Logger log = Logger.getLogger(LoggingCommons.class);

	/**
	 * @param location - the {@link File} where the properties file is 
	 * @param prefix - the prefix to append to the name or null for standard 'logger.properties'
	 * @return
	 */
	public static LoggerInitializer initialize(File location, String prefix) {
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		File file = declareLoggerFile(location, prefix);
	
		try {							
			if (file.exists()) {
				loggerInitializer.setLoggerConfigUrl( file.toURI().toURL());		
				loggerInitializer.afterPropertiesSet();				
			}
			return loggerInitializer;
		} catch (Exception e) {		
			String msg = "cannot initialize logging while looking for [" + file.getAbsolutePath() + "]";
			log.info(msg, e);
		}
		return null;
	}
	
	/**
	 * @param prefix - the prefix to add to the 'logger.properties' file 
	 * @return - a {@link LoggerInitializer} configured from a  {@code '<prefix>logger.properties'} file either 
	 * in the dev-env root or the workspace specific storage
	 */
	public static LoggerInitializer initialize(String prefix) {
		Optional<File> optional = DevrockPlugin.envBridge().getDevEnvironmentRoot();
		File location = null;
		if (!optional.isPresent()) {
			location = DevrockPlugin.envBridge().workspaceSpecificStorageLocation();
		}
		else {
			location = optional.get();
		}
		return initialize( location, prefix);
	}
	/**
	 * @return - a {@link LoggerInitializer} configured from a 'logger.properties' either 
	 * in the dev-env root or the workspace specific storage
	 */
	public static LoggerInitializer initialize() {
		Optional<File> optional = DevrockPlugin.envBridge().getDevEnvironmentRoot();
		File location = null;
		if (!optional.isPresent()) {
			location = DevrockPlugin.envBridge().workspaceSpecificStorageLocation();
		}
		else {
			location = optional.get();
		}
		return initialize( location, null);
	}
	
	/**
	 * looks for a file with {@code '<prefix>logger.properties'} and if not present, will
	 * revert to 'logger.properties'
	 * @param prefix - the prefix for the preferred properties file
	 * @return - a initialzed {@link LoggerInitializer}
	 */
	public static LoggerInitializer initializeWithFallback(String prefix) {
		Optional<File> optional = DevrockPlugin.envBridge().getDevEnvironmentRoot();
		File location = null;
		if (!optional.isPresent()) {
			location = DevrockPlugin.envBridge().workspaceSpecificStorageLocation();
		}
		else {
			location = optional.get();
		}
		File properties = declareLoggerFile(location, prefix);
		if (properties.exists()) 
			return initialize( location, prefix);
		else 
			return initialize( location, null);
	}
	 
	/**
	 * @param location
	 * @param prefix
	 * @return
	 */
	private static File declareLoggerFile( File location, String prefix) {
		File file = new File(location, prefix != null ? prefix + ".logger.properties" : "logger.properties");
		return file;
	}
}
