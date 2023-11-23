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
package com.braintribe.artifact.processing.backend;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.maven.settings.Settings;

/**
 * produces the path to the local repository as required by MC 
 *
 * a) find drive section .. c:/bla/myrepo
 * b) find variable.. ${M2_REPO}/bla
 * or
 * just a relative path <root>/repo/bla. repo/bla?
 * currently: no support 
 * 
 * @author pit
 *
 */
public class ArtifactProcessingLocalRepositoryLocationExpert implements LocalRepositoryLocationProvider {
	private String configuredExpression;
	private String root;
	private String processedPath;

	@Override
	public String getLocalRepository(String expression) throws RepresentationException {
		if (processedPath == null) {
			processedPath = processPath();
		}
		 
		return processedPath;
	}

	/**
	 * sets the currently configured expression as from the {@link RepositoryConfiguration} so that it can be used as base for
	 * computation of the actual path  
	 * @param configuredExpression - the expression as defined (via the converted {@link Settings}
	 */
	public void setConfiguredExpression(String configuredExpression) {
		this.configuredExpression = configuredExpression;
		
	}

	/**
	 * sets the root path of 
	 * @param object
	 */
	public void setLocalRepositoryFilesystemRoot(String root) {
		this.root = root;		
	}
	
	/**
	 * actually build a path from the file system root and the expression 
	 * @return - the processed path 
	 */
	private String processPath() {
		// do something smart here, for now it's what's specified in the deployables space
		return root;				
	}

}
