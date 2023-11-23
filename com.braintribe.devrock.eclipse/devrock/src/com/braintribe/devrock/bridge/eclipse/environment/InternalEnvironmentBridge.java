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
package com.braintribe.devrock.bridge.eclipse.environment;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.bridge.eclipse.api.EnvironmentBridge;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.devrock.eclipse.model.ve.EnvironmentOverride;
import com.braintribe.devrock.eclipse.model.ve.OverrideType;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

public class InternalEnvironmentBridge implements EnvironmentBridge {
	private static final String DEV_ENV_SOURCES = "dev-env";
	private static final String DEV_ENV_TF_SOURCES = "dev-tfsetups";
	private static final String CACHE_DIR = "artifacts";
	private static final String REPOSITORY_CONFIGURATION = "repository-configuration.yaml";
	private static final String SCAN_ROOT_GIT = "git";
	private static final String SCAN_ROOT_TF = "tf-setups";
	
	private static final String BUILD_ROOT = "git";
	private static final String DEV_ENVIRONMENT_MARKER_YAML = "dev-environment.yaml";
	private BasicStorageLocker storageLocker = new BasicStorageLocker();
	private Map<String,String> archetypeToTagMap = new HashMap<>();
	{
		archetypeToTagMap.put("model", "asset");
	}
	 

	@Override
	public File workspaceSpecificStorageLocation() {
		String path = DevrockPlugin.instance().getStateLocation().toOSString();
		//return new File( path + File.separator + DevrockPlugin.PLUGIN_ID);
		return new File( path);
	}

	@Override
	public Map<String, String> archetypeToTagMap() {		
		return archetypeToTagMap;
	}

	@Override
	public BasicStorageLocker storageLocker() {	
		return storageLocker;
	}

		

	@Override
	public Optional<File> getDevEnvironmentRoot() {
		File workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		File devRoot = workspaceLocation.getParentFile();
		File devEnvYaml = new File( devRoot, DEV_ENVIRONMENT_MARKER_YAML);
		if (devEnvYaml.exists()) {
			return Optional.of( devRoot);
		}
		return Optional.empty();
	}
	
	
	@Override
	public Optional<List<Pair<String,File>>> getDevEnvScanRoots() {
		Optional<File> optional = getDevEnvironmentRoot();
		if (optional.isPresent()) {
			File root = optional.get();
			List<Pair<String,File>> scanRoots = new ArrayList<>();
			File gitFolder = new File( root, SCAN_ROOT_GIT);
			if (gitFolder.exists()) {
				scanRoots.add( Pair.of( DEV_ENV_SOURCES, gitFolder));				
			}
			File tfsetupFolder = new File( root, SCAN_ROOT_TF);
			if (tfsetupFolder.exists()) {
				scanRoots.add(Pair.of( DEV_ENV_TF_SOURCES, tfsetupFolder));
			}
			return Optional.of( scanRoots);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<File> getDevEnvBuildRoot() {
		Optional<File> optional = getDevEnvironmentRoot();
		if (optional.isPresent()) {
			File root = optional.get();
			File gitFolder = new File( root, BUILD_ROOT);
			if (gitFolder.exists()) {
				return Optional.of( gitFolder);
			}
		}
		return Optional.empty();
	}
	

	@Override
	public Optional<List<File>> getWorkspaceScanDirectories() {
		Optional<List<SourceRepositoryEntry>> value = storageLocker().getValue( StorageLockerSlots.SLOT_SCAN_DIRECTORIES);
		if (!value.isPresent()) {
			return Optional.empty();
		}
		List<SourceRepositoryEntry> entries = value.get();
		List<File> files = new ArrayList<>( entries.size());
		for (SourceRepositoryEntry entry : entries) {
			files.add( new File( entry.getActualFile()));
		}				
		return Optional.of( files);		
	}
	
	
	
	
	@Override
	public List<SourceRepositoryEntry> getScanRepositories() {		
		int devSize = 0, wsSize = 0;
		
		Optional<List<Pair<String,File>>> devEnvScanDirectoryOptional = getDevEnvScanRoots();
		if (devEnvScanDirectoryOptional.isPresent()) {
			devSize = devEnvScanDirectoryOptional.get().size();
		}
		
		Optional<List<SourceRepositoryEntry>> workspaceScanDirectoriesOptional = storageLocker().getValue( StorageLockerSlots.SLOT_SCAN_DIRECTORIES);	
		if (workspaceScanDirectoriesOptional.isPresent()) {
			wsSize = workspaceScanDirectoriesOptional.get().size();
		}
		
		List<SourceRepositoryEntry> entries = new ArrayList<>( devSize + wsSize);
		if (devSize > 0) {			
			List<Pair<String,File>> scanRoots = devEnvScanDirectoryOptional.get();
			for (Pair<String, File> scanRootPair : scanRoots) {
				SourceRepositoryEntry entry = SourceRepositoryEntry.T.create();
				entry.setEditable(false);						
				File scanRootFile = scanRootPair.second;
				entry.setActualFile( scanRootFile.getAbsolutePath());
				entry.setKey( scanRootPair.first);
				if (Files.isSymbolicLink( scanRootFile.toPath())) {
					entry.setSymbolLink(true);
				}			
				entries.add(entry);			
			}
		}
		if (wsSize > 0) {
			// no further post-processing as everything's stored in the entries
			entries.addAll( workspaceScanDirectoriesOptional.get());
		}
		return entries;
	}

	@Override
	public List<File> getScanDirectories() {	
		return getScanRepositories().stream().map( e -> new File(e.getActualFile())).collect(Collectors.toList());
	}

	@Override
	public Optional<File> getRepositoryConfiguration() {
		Optional<File> optional = getLocalCache();
		if (optional.isPresent()) {
			File artifacts = optional.get();
			File cfg = new File( artifacts, REPOSITORY_CONFIGURATION);
			if (cfg.exists()) {
				return Optional.of(cfg);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<File> getLocalCache() {
		Optional<File> optional = getDevEnvironmentRoot();
		if (optional.isPresent()) {
			File root = optional.get();
			File artifacts = new File( root, CACHE_DIR);
			if (artifacts.exists()) {
				return Optional.of( artifacts);
			}
		}			
		return Optional.empty();
	}
	

	@Override
	public VirtualEnvironment virtualEnviroment() {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		Optional<List<EnvironmentOverride>> optional = storageLocker().getValue( StorageLockerSlots.SLOT_VE_ENTRIES);
		if (optional.isPresent()) {
			List<EnvironmentOverride> overrides = optional.get();
			overrides.stream().forEach( eo -> {
				if (eo.getOverrideNature() == OverrideType.environment) {
					ove.setEnv( eo.getName(), eo.getValue());
				}
				else {
					ove.setProperty( eo.getName(), eo.getValue());
				}
			});
		}
		
		return ove;
	}
	

}
