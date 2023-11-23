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
package com.braintribe.model.processing.library.service.owasp;

import java.io.File;

import org.owasp.dependencycheck.utils.Settings;

import com.braintribe.logging.Logger;

public class OwaspSettings {

	private final static Logger logger = Logger.getLogger(OwaspSettings.class);

	protected static void clearSettings(Settings settings) {
		if (settings == null) {
			return;
		}
		try {
			settings.cleanup(true);
		} catch (Exception e) {
			logger.info("Could not cleanup settings of vulnerability parser.", e);
		}
	}

	protected static void populateSettings(Settings settings, File dataDirectory, String nvdMirrorBasePath) {

		settings.setString(Settings.KEYS.DATA_DIRECTORY, dataDirectory.getAbsolutePath());
		settings.setBoolean(Settings.KEYS.ANALYZER_ASSEMBLY_ENABLED, false);
		settings.setString(Settings.KEYS.CONNECTION_TIMEOUT, "10000");
		settings.setInt(Settings.KEYS.CVE_START_YEAR, 2020);
		/* settings.setBoolean(Settings.KEYS.ANALYZER_CENTRAL_ENABLED, false); settings.setBoolean(Settings.KEYS.ANALYZER_DEPENDENCY_BUNDLING_ENABLED,
		 * false); settings.setBoolean(Settings.KEYS.ANALYZER_DEPENDENCY_MERGING_ENABLED, false); */
		settings.setBoolean("failOnError", false);

		String pathWithProtocol = nvdMirrorBasePath.startsWith("file://") ? nvdMirrorBasePath : "file://" + nvdMirrorBasePath;

		String baseUrl = pathWithProtocol + "/nvdcve-1.1-%d.json.gz";
		String modifiedUrl = pathWithProtocol + "/nvdcve-1.1-modified.json.gz";

		settings.setString(Settings.KEYS.CVE_BASE_JSON, baseUrl);
		settings.setString(Settings.KEYS.CVE_MODIFIED_JSON, modifiedUrl);

	}
}
