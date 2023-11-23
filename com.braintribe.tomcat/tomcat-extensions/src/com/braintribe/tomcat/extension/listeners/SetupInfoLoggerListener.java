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
package com.braintribe.tomcat.extension.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.json.JSONParser;

/**
 * Logs setup information for the respective tribefire project, if a setup information file is {@link #setSetupInfoFile(String) specified} or exists
 * at its default location {@value #DEFAULT_SETUP_INFO_FILE}. The file must be a json file that represents a map. Its entries are logged in order, one
 * line for each pair.
 * <p>
 * This class is intentionally very simple, since it's Tomcat specific and thus shouldn't contain too much logic. It's basically just logging
 * key/value pairs.<br>
 * The content to be logged, i.e. what and which detail, must be provided by another (smarter) tool such as Jinni.
 */
public class SetupInfoLoggerListener implements LifecycleListener {

	private static final Log log = LogFactory.getLog(SetupInfoLoggerListener.class);

	/**
	 * The default path where the file is searched, if no other path is {@link #setSetupInfoFile(String) specified}.
	 */
	private static final String DEFAULT_SETUP_INFO_FILE = "../../../setup-info/setup-info.json";

	private String setupInfoFile;

	public String getSetupInfoFile() {
		return setupInfoFile;
	}

	public void setSetupInfoFile(String setupInfoFile) {
		this.setupInfoFile = setupInfoFile;
	}

	/**
	 * Runs {@link #log()} when receiving the initialization event.
	 */
	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		if (Lifecycle.BEFORE_INIT_EVENT.equals(event.getType())) {
			log();
		}
	}

	/**
	 * Logs the setup information, if the respective file exists.
	 */
	private void log() {
		boolean setupInfoFileMustExist = this.setupInfoFile != null;
		File setupInfoFile = new File(setupInfoFileMustExist ? this.setupInfoFile : DEFAULT_SETUP_INFO_FILE);

		if (setupInfoFile.exists()) {
			Map<?, ?> setupInfo = parseSetupInfo(setupInfoFile);

			setupInfo.forEach((key, value) -> {
				// align with this Tomcat log statements
				int minimumKeyStringLength = "Command line argument: ".length();

				String keyString = key + ": ";
				if (keyString.length() < minimumKeyStringLength) {
					// add spaces
					keyString = String.format("%-" + minimumKeyStringLength + "s", keyString);
				}

				log.info(keyString + value);
			});
		} else {
			if (setupInfoFileMustExist) {
				throw new IllegalStateException("The configured setup info file " + setupInfoFile.getAbsolutePath() + " does not exist!");
			} else {
				// default does not exist at its default location.
				// this is fine, i.e. just don't log anything.
			}
		}
	}

	/**
	 * Parses the passed <code>setupInfoFile</code> and returns the result which is expected to be a map.
	 */
	private static LinkedHashMap<?, ?> parseSetupInfo(File setupInfoFile) {
		try {
			try (InputStream inputStream = new FileInputStream(setupInfoFile)) {
				JSONParser parser = new JSONParser(inputStream, "UTF-8");
				Object parsedObject = parser.parse();

				if (!(parsedObject instanceof Map)) {
					throw new IllegalStateException("Expected parsed object to be a map but found " + parsedObject.getClass().getName() + "!");
				}
				return (LinkedHashMap<?, ?>) parsedObject;
			}
		} catch (Exception e) {
			throw new IllegalStateException("Couldn't parse " + setupInfoFile + "!", e);
		}
	}
}
