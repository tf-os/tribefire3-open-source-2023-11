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
package com.braintribe.devrock.ac.container.resolution.viewer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class PcArtifactFilter {

	private RepositoryReflection repositoryReflection;
	private Map<AnalysisArtifact, File> pcArtifacts = new HashMap<>();
	 

	public PcArtifactFilter() {
		repositoryReflection = DevrockPlugin.mcBridge().reflectRepositoryConfiguration().get();			
	}
		
	
	public boolean filter(AnalysisArtifact artifact) {
		String repositoryOrigin;
		Part pomPart = artifact.getParts().get(":pom");		
		if (pomPart == null) {			
			DevrockPluginStatus status = new DevrockPluginStatus("no pom found, cannot determine origin of :" + artifact.asString(), IStatus.ERROR);
			DevrockPlugin.instance().log(status);
			return false;
		}
		else {
			repositoryOrigin = pomPart.getRepositoryOrigin();
		}
		
		Repository repository = repositoryReflection.getRepository( repositoryOrigin);
		if (repository == null) {
			return false;
		}
		if (repository instanceof MavenFileSystemRepository == false) {			
			return false;
		}
		
		MavenFileSystemRepository fileRepo = (MavenFileSystemRepository) repository;
		File repoRoot = new File( fileRepo.getRootPath());
		pcArtifacts.put(artifact, repoRoot);
		return true;
	}


	public Map<AnalysisArtifact, File> getPcArtifacts() {
		return pcArtifacts;
	}
	
	public Map<VersionedArtifactIdentification, File> getPcIdentifications() {
		Map<VersionedArtifactIdentification, File> result = new HashMap<>( pcArtifacts.size());
		for (Map.Entry<AnalysisArtifact, File> entry : pcArtifacts.entrySet()) {
			result.put( entry.getKey(), entry.getValue());
		}
		return result;
	}
	
}
