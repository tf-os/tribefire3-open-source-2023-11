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
package com.braintribe.tribefire.jinni.support.request.alias;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.velocity.shaded.commons.io.FilenameUtils;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.yaml.YamlConfigurations;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.config.yaml.ConfigVariableResolver;
import com.braintribe.logging.Logger;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.tribefire.jinni.support.request.RequestPersistenceManipulator;
import com.braintribe.ve.api.VirtualEnvironment;

/**
 * Deals with storing and processing aliases.
 * 
 */
public class JinniAlias extends RequestPersistenceManipulator {

	private static final Logger LOG = Logger.getLogger(JinniAlias.class);

	protected static final String DIR_NAME = "aliases";

	private VirtualEnvironment virtualEnvironment;
	
	/** Sets the installation dir. File for compatibility. */
	@Override
	public void setInstallationDir(File installationDir) {
		this.installationDir = installationDir.toPath();
	}

	@Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	public Map<String, Path> getAliases() {
		try {
			return getAliases(getDirectory());
		} catch (IOException e) {
			throw Exceptions.unchecked(e);
		}
	}

	/** Stores the request in Jinni alias repository. */
	public void createAlias(String name, ServiceRequest request) {
		Path newEntry = getFilenameFromAlias(name);
		
		try {
			Files.createDirectories(newEntry.getParent());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(newEntry)) {
			new YamlMarshaller().marshall(writer, request, GmSerializationOptions.defaultOptions);
		} catch (MarshallException | IOException ex) {
			LOG.error("Error when writing alias. ", ex);
			throw new IllegalStateException("Alias folder writing unsuccessful. Cause: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Gets the expected filename from alias.
	 *
	 * @param name
	 *            the name
	 * @return the filename from alias
	 */
	private Path getFilenameFromAlias(String name) {
		return Paths.get(getDirectory().toAbsolutePath() + SEPARATOR + name + ".yaml");
	}
	
	@Override
	public Path getDirectory() {
		File devEnv = isDevEnv();
		
		if (devEnv != null)
			return new File(devEnv, "commands").toPath();
		
		return super.getDirectory();
	}
	
	private File isDevEnv() {
		File currentWorkingDirectory = new File(System.getProperty("user.dir"));
		return isDevEnv(currentWorkingDirectory);
	}
	
	private File isDevEnv(File folder) {
		File file = new File(folder, "dev-environment.yaml");
		
		if (file.exists()) {
			return folder;
		}
		else {
			File parentFolder = folder.getParentFile();
			
			if (parentFolder != null) {
				return isDevEnv(parentFolder);
			}
			else {
				return null;
			}
		}
	}

	/** Gets the alias entries stored in the repository. */
	private Map<String, Path> getAliases(Path aliasDirectory) throws IOException {
		if (!Files.exists(aliasDirectory))
			return new HashMap<>();
		
		return Files.walk(aliasDirectory).filter(Files::isRegularFile).map(this::getAliasFromFile)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		// these are map entries already, but I still have to collect them manually?
	}

	/** Gets the service request from path. */
	@Override
	public ServiceRequest getServiceRequestFromFile(Path path) {
		File file = path.toFile();
		ConfigVariableResolver variableResolver = new ConfigVariableResolver(virtualEnvironment, file);
		
		ServiceRequest serviceRequest = YamlConfigurations.read(ServiceRequest.T) //
			.placeholders(variableResolver::resolve)
			.from(file)
			.get();
		
		return serviceRequest;
	}

	/** Gets the request string from file. */
	@Override
	public String getRequestStringFromFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path));
	}

	private Map.Entry<String, Path> getAliasFromFile(Path path) {

		try {
			return new AbstractMap.SimpleEntry<String, Path>(FilenameUtils.removeExtension(path.getFileName().toString()), path);
		} catch (MarshallException mex) {
			LOG.error("Error when reading alias. ", mex);
			throw new IllegalStateException(
					"Alias folder " + getDirectory().toAbsolutePath() + "contains invalid entries. Purge is suggested. Cause: " + mex.getMessage(),
					mex);
		}
	}

	@Override
	protected String getDirName() {
		return DIR_NAME;
	}

	/** Resolve alias by name. */
	public ServiceRequest resolveAlias(String requestIdentifier) {
		Path path = getFilenameFromAlias(requestIdentifier);
		if (Files.exists(path)) {
			return getServiceRequestFromFile(path);
		} else {
			return null;
		}
	}


}
