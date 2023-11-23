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
package com.braintribe.tribefire.jinni.support.request;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Deals with storing and processing aliases.
 * 
 */
public abstract class RequestPersistenceManipulator {

	protected Path installationDir;

	protected static final String SEPARATOR = FileSystems.getDefault().getSeparator();
	private static final Logger LOG = Logger.getLogger(RequestPersistenceManipulator.class);

	public Path getDirectory() {
		return getServiceDirectory(getDirName());
	}

	protected abstract String getDirName();

	public ServiceRequest getServiceRequestFromFile(Path path) {
		try {
			return (ServiceRequest) new YamlMarshaller().unmarshall(Files.newBufferedReader(path),
					GmDeserializationOptions.deriveDefaults().setDecodingLenience(new DecodingLenience(true)).build());
		} catch (Exception ex) {
			LOG.error("Error when reading a persisted request. ", ex);
			return null;
		}
	}

	public String getRequestStringFromFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path));
	}

	public void setInstallationDir(File installationDir) {
		this.installationDir = installationDir.toPath();
	}

	protected Path getServiceDirectory(String directory) {
		Path serviceDir = Paths.get(installationDir + SEPARATOR + directory);

		// create if the directory does not exist
		if (!Files.exists(serviceDir)) {
			LOG.info("Service directory " + serviceDir.toAbsolutePath() + " does not exist. Creating.");
			try {
				Files.createDirectories(serviceDir);
			} catch (IOException ioex) {
				LOG.error("Attempting to delete " + serviceDir.toAbsolutePath() + " caused an error: " + ioex.getMessage(), ioex);
				throw new IllegalStateException("Unable to retrieve or create " + serviceDir.toAbsolutePath() + "directory.", ioex);
			}
		}

		return serviceDir.normalize();
	}

}
