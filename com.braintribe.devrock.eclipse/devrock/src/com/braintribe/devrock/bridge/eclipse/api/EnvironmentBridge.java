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
package com.braintribe.devrock.bridge.eclipse.api;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.bridge.eclipse.environment.BasicStorageLocker;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;

public interface EnvironmentBridge {
	/**
	 * @return - the folder where workspace-specific data should be stored
	 */
	File workspaceSpecificStorageLocation(); // passive, derived from dev-environment, remains workspace location
	
	/**
	 * @return - the currently configured map of 'archetype' to 'tag' - required by the dependency injectors 
	 */
	Map<String,String> archetypeToTagMap();  // passive, derived from dev-environment (or even hard-coded)
	
	
	/**
	 * @return - the current data container for data that should be persisted 
	 */
	BasicStorageLocker storageLocker(); // active
	
	/**
	 * @return - the root {@link File} of the dev-environment 
	 */
	Optional<File> getDevEnvironmentRoot(); // passive, derived from dev-envionment
	
	
	
	/**
	 * @return - the root of the scan directories (the'git')
	 */
	Optional<List<Pair<String, File>>> getDevEnvScanRoots(); // passive, derived from dev-environment
	
	
	Optional<File> getDevEnvBuildRoot(); // active, managed by user
		
	/**
	 * @return - the roots of the scan directories as added by the user 
	 */
	Optional<List<File>> getWorkspaceScanDirectories(); // active, managed by user

	
	List<SourceRepositoryEntry> getScanRepositories(); // combination of getDevEnvScanDirectories & getWorkspaceScanDirectories
	
	List<File> getScanDirectories(); // combination of getDevEnvScanDirectories & getWorkspaceScanDirectories
	
		
	/**
	 * @return - the devenv's configuration
	 */
	Optional<File> getRepositoryConfiguration(); // passive, derived from dev-envionment
	
	/**
	 * @return - the local cache aka the 'local repository'
	 */
	Optional<File> getLocalCache(); // passive, derived from dev-envionment

	/**
	 * @return - the current {@link OverridingEnvironment}
	 */
	VirtualEnvironment virtualEnviroment(); // active, managed by user
	
	
	
	
}
