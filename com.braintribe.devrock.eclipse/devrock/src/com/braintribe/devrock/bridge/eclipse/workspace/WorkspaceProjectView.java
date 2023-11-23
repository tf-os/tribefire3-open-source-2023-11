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
package com.braintribe.devrock.bridge.eclipse.workspace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;

/**
 * @author pit
 *
 */
public class WorkspaceProjectView {
	private static Logger log = Logger.getLogger(WorkspaceProjectView.class);
	private Map<EqProxy<VersionedArtifactIdentification>, BasicWorkspaceProjectInfo> artifactsInWorkspace = new HashMap<>();
	private Map<IProject, BasicWorkspaceProjectInfo> projectsInWorkspace = new HashMap<>();
	
	public Map<EqProxy<VersionedArtifactIdentification>, BasicWorkspaceProjectInfo> getArtifactsInWorkspace() {
		return artifactsInWorkspace;
	}
	public Map<IProject, BasicWorkspaceProjectInfo> getProjectsInWorkspace() {
		return projectsInWorkspace;
	}

	public static Logger getLog() {
		return log;
	}

	/**
	 * @param vai - the {@link VersionedArtifactIdentification}
	 * @return - the {@link BasicWorkspaceProjectInfo} or null if not known
	 */
	public BasicWorkspaceProjectInfo getProjectInfo( VersionedArtifactIdentification vai) {
		return artifactsInWorkspace.get(HashComparators.versionedArtifactIdentification.eqProxy(vai));
	}
	
	/**
	 * builds as range from the passed {@link VersionedArtifactIdentification}'s version, using major/minor as lower-boundary, upper boundary standard minor,
	 * if required via {@link StorageLockerSlots#SLOT_AC_REQUIRE_HIGHER_VERSION}, a range matching is tested to be
	 * at least the same version as the passed {@link VersionedArtifactIdentification}'s version
	 * @param vai - the {@link VersionedArtifactIdentification}
	 * @return - the best matching {@link BasicWorkspaceProjectInfo} if any 
	 */
	public BasicWorkspaceProjectInfo getAutoRangedProjectInfo( VersionedArtifactIdentification vai) {
		// build fuzzy - autoranged - version
		Version version;
		try {
			version = Version.parse( vai.getVersion());
		} catch (Exception e) {
			// if in any case the version is anomalous beyond parsing, we simply ignore that artifact - for now
			return null;
		}
		FuzzyVersion fuzzy = FuzzyVersion.from(version);
		// cannot generate a minor, useless fuzzy and definitively not one of our artifacts
		if (fuzzy.getMinor() == null) 
			return null;
		
		// as discussed with Ralf & Peter : they want to be able to change that behavior 
		boolean requireHigherVersion = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_AC_REQUIRE_HIGHER_VERSION, true);
		
		// find all current projects that match the fuzzy range
		List<BasicWorkspaceProjectInfo> matches = new ArrayList<>( projectsInWorkspace.size());
		for (Map.Entry<IProject, BasicWorkspaceProjectInfo> entry : projectsInWorkspace.entrySet()) {
			BasicWorkspaceProjectInfo projectInfo = entry.getValue();
			CompiledArtifactIdentification compiledArtifactIdentification = projectInfo.getCompiledArtifactIdentification();
			Version projectVersion = compiledArtifactIdentification.getVersion();
			if (
					vai.compareTo( compiledArtifactIdentification) == 0 && // must be same ArtifactIdentification  
					fuzzy.matches( projectVersion)  // and it must match the auto-range														
				) {
				// second level test if required
				boolean higherVersion = projectVersion.compareTo( version) >= 0;
				if (higherVersion) {
					matches.add( projectInfo);					
				}
				else {					
					if (requireHigherVersion)
						continue;
					}	
					if (matchReleaseCandidate(version, projectVersion)) {
						matches.add( projectInfo);
					}			
				}			
		}
		// no matches -> no return value
		if (matches.isEmpty()) {
			return null;
		}
		// sort to get the highest version
		matches.sort( new Comparator<BasicWorkspaceProjectInfo>() {
			@Override
			public int compare(BasicWorkspaceProjectInfo o1, BasicWorkspaceProjectInfo o2) {
				return o1.getCompiledArtifactIdentification().getVersion().compareTo(o2.getCompiledArtifactIdentification().getVersion());				
			}			
		});
		
		// return highest version
		return matches.get(0);			
	}
	
	/**
	 * tests whether the requested version is EXACTLY one incrementation of the revision 
	 * @param rVersion - the requested {@link Version}
	 * @param pVersion - the {@link Version} to test
	 * @return - true if the tested version is acceptable
	 */
	// TODO : update once we move away from revisions
	private static boolean matchReleaseCandidate(Version rVersion, Version pVersion) {
		if (!rVersion.isPreliminary()) 
			return false;			
		
		
		
		Integer rRevision = rVersion.getRevision() != null ? rVersion.getRevision() : 0;
		Integer pRevision = pVersion.getRevision() != null ? pVersion.getRevision() : 0; 							
		if (rRevision == pRevision + 1) {
			return true;
		}
					
		return false;
	}
	
	/**
	 * @param cai - the {@link CompiledArtifactIdentification} (which is converted to a {@link VersionedArtifactIdentification})
	 * @return - the {@link BasicWorkspaceProjectInfo} or null if not known
	 */
	public BasicWorkspaceProjectInfo getProjectInfo( CompiledArtifactIdentification cai) {
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.create( cai.getGroupId(), cai.getArtifactId(), cai.getVersion().asString());
		return getProjectInfo(vai);
	}
	
	/**
	 * @param project - the {@link IProject}
	 * @return - the {@link BasicWorkspaceProjectInfo} or null if not known
	 */
	public BasicWorkspaceProjectInfo getProjectInfo( IProject project) {
		return projectsInWorkspace.get( project);
	}
	
	
	/**
	 * returns either the artifactId of the project or the name of the project (if no info present)
	 * @param project - the {@link IProject} to get the display name for
	 * @return - the artifactId or the project name
	 */
	public String getProjectDisplayName( IProject project) {
		BasicWorkspaceProjectInfo projectInfo = projectsInWorkspace.get( project);
		if (projectInfo != null) {
			return projectInfo.getVersionedArtifactIdentification().getArtifactId();
		}
		else {
			return project.getName();
		}
	}
	
	public boolean equals( WorkspaceProjectView other) {
		// size
		if (artifactsInWorkspace.size() != other.artifactsInWorkspace.size()) 
			return false;
		
		// contents
		List<VersionedArtifactIdentification> vais = artifactsInWorkspace.keySet().stream().map( eq -> eq.get()).collect(Collectors.toList());
		List<VersionedArtifactIdentification> otherVais = other.artifactsInWorkspace.keySet().stream().map( eq -> eq.get()).collect(Collectors.toList());
		
		Comparator<VersionedArtifactIdentification> comparator = new Comparator<VersionedArtifactIdentification>() {

			@Override
			public int compare(VersionedArtifactIdentification o1, VersionedArtifactIdentification o2) {			
				int retval = o1.getGroupId().compareTo( o2.getGroupId());
				if (retval != 0)
					return retval;
				
				retval = o1.getArtifactId().compareTo( o2.getArtifactId());
				if (retval != 0)
					return retval;
				
				retval = o1.getVersion().compareTo( o2.getVersion());
				if (retval != 0)
					return retval;
				return 0;
			}
			
		};
		// sort the lists 
		
		vais.sort(comparator);
		otherVais.sort(comparator);
		
		// compare data - size IS equal 
		for (int i = 0; i < vais.size(); i++) {
			if (vais.get(i).equals( otherVais.get(i)))
				return false;
		}
		return true;		
		
	}
	
}
