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
package com.braintribe.model.access.smood.collaboration.deployment;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.FileTools;

/**
 * @author peter.gazdik
 */
public abstract class AbstractCsaDeployedUnit<CSA extends CollaborativeSmoodAccess> {

	public static String trunkStageName = "trunk";
	public static String resourcesFolderName = "RESOURCES";

	public CSA csa;
	public File baseFolder;
	public File jsonFile;
	public File resourcesBaseAbsoluteFile;

	public CsaStatePersistence statePersistence;
	public Supplier<CollaborativeSmoodConfiguration> configurationSupplier;
	public CollaborativeSmoodConfiguration configuration;
	public Supplier<PersistenceGmSession> sessionFactory;
	public PersistenceGmSession session;

	public void postConstruct() {
		csa.postConstruct();
		session = newSession();
	}

	public PersistenceGmSession newSession() {
		return sessionFactory.get();
	}

	public final void cleanup() {
		try {
			FileTools.deleteDirectoryRecursively(baseFolder);

		} catch (IOException e) {
			throw new GenericModelException("Deleting temporary folder failed: " + baseFolder.getAbsolutePath(), e);
		}
	}

	public File stageManFile(String stageName) {
		return stageManFile(stageName, false);
	}

	public File stageManFile(String stageName, boolean isModel) {
		File stageFolder = new File(baseFolder, stageName);

		String contentFileName = isModel ? "model.man" : "data.man";
		return new File(stageFolder, contentFileName);
	}

	public <T> T eval(CollaborativePersistenceRequest request) {
		return (T) csa.processCustomRequest(null, request);
	}

	public File resourceFile(String relativePath) {
		return new File(resourcesBaseAbsoluteFile, relativePath);
	}

}
