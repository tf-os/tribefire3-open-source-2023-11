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
package com.braintribe.devrock.zed.forensics.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.zarathud.model.data.Artifact;

/**
 * a registry that knows all tags of the dependencies involved, and respects the different requesters
 * (a different requester can use different tags for the same dependency) 
 * 
 * @author pit
 *
 */
public class DependencyStructureRegistry implements HasDependencyTagTokens {		
	private static Logger log = Logger.getLogger(DependencyStructureRegistry.class);
	private Map<String, DependencyStructureEntry> nameToEntryMap = new HashMap<>();	
	private Collection<CompiledArtifact> collectedClasspathSolutions;
	
	/**
	 * creates a new instance of {@link DependencyStructureEntry} and parameterizes it
	 * @param terminal - the {@link Solution} standing as terminal
	 * @param collectedClasspathSolutions - the {@link Solution}s making up the cp (terminal included)
	 * @return
	 */
	public static DependencyStructureRegistry buildRegistry( Collection<CompiledArtifact> collectedClasspathSolutions) {
		DependencyStructureRegistry registry = new DependencyStructureRegistry();		
		registry.collectedClasspathSolutions = collectedClasspathSolutions;
				
		registry.build();
		return registry;
	}
	
	/**
	 * hiding that one 
	 */
	private DependencyStructureRegistry() { 
	}
	
	/**
	 * build up the registry be marching through all solutions in the CP
	 */
	private void build() {
		
		for (CompiledArtifact solution : collectedClasspathSolutions) {
			build( solution);
		}
	}
	
	/**
	 * match a dependency (as it's written in the pom) to a real-life solution 
	 * (using groupId & artifactId) 
	 * @param dependency - the {@link Dependency} to search for
	 * @return - the {@link Solution} if any exists in the CP
	 */
	private CompiledArtifact findMatchingSolution( CompiledDependency dependency) {			
		
		for (CompiledArtifact solution : collectedClasspathSolutions) {
			if (
					solution.getGroupId().equalsIgnoreCase( dependency.getGroupId()) &&
					solution.getArtifactId().equalsIgnoreCase( dependency.getArtifactId())
				){
					return solution;
				}
		}
		// no solution found.. 
		return null;
	}
	
	/**
	 * process a single solution 
	 * @param solution
	 */
	private void build( CompiledArtifact solution) {
		String name = solution.asString();
				
		
		// 
		for (CompiledDependency dependency : solution.getDependencies()) {
			// find the solution that matches this dependency 
			CompiledArtifact resolvedSolution = findMatchingSolution(dependency);
			if (resolvedSolution != null) {
				//add the tags the solution used for the declaration  
				String key = resolvedSolution.asString();
				DependencyStructureEntry entry = nameToEntryMap.computeIfAbsent(key, k -> new DependencyStructureEntry(k));
				List<String> tagList = entry.getRequestorToTagMap().computeIfAbsent( name, k -> new ArrayList<String>());
				tagList.addAll( dependency.getTags());
			}
			else {
				log.debug("no solution found for dependency [" + dependency.getGroupId() + ":" + dependency.getArtifactId()  + "] within [" + name + "]. Dropped?");
			}
		}				
	}
	
	public boolean isMappedAs( Artifact artifact, Artifact requester, String tag) {
		return isMappedAs(artifact.toVersionedStringRepresentation(), requester.toVersionedStringRepresentation(), tag);
	}
		
	/**
	 * tells you whether the artifact has been marked by the requester with the tag 
	 * @param artifactKey - the {@link Artifact}'s key
	 * @param requesterKey - the requesting {@link Artifact}'s key
	 * @param tag - the tag to search for
	 * @return - true if the requester used the tag in its pom 
	 */
	public boolean isMappedAs( String artifactKey, String requesterKey, String tag) {
		DependencyStructureEntry e = nameToEntryMap.get(artifactKey);
		if (e == null) {
			log.warn("no solution found for dependency [" + artifactKey + "] to check for [" + tag +"]");
			return false;
		}
		List<String> dependencyTagViaRequestor = e.getDependencyTagViaRequestor(requesterKey);
		if (dependencyTagViaRequestor != null && dependencyTagViaRequestor.contains(tag)) {
			return true;
		}
		return false;
	}
	
	public boolean isMappedAs( Artifact artifact, String tag) {
		return isMappedAs( artifact.toVersionedStringRepresentation(), tag);
	}
	
	/**
	 * tells you whether the artifact has been taged by *any* requester with the tag
	 * @param artifactKey - the {@link Artifact}'s key
	 * @param tag - the tag
	 * @return - true if any requester has used the tag in its pom
	 */
	public boolean isMappedAs( String artifactKey, String tag) {
		DependencyStructureEntry e = nameToEntryMap.get(artifactKey);
		if (e == null) {
			log.warn("no solution found for dependency [" + artifactKey + "] to check for [" + tag +"]");
			return false;
		}
		for (Entry<String, List<String>> entry : e.getRequestorToTagMap().entrySet()) {
			if (entry.getValue().contains(tag))
				return true;
		}
		return false;
	}
	
	/**
	 * @param artifactKey - the {@link Artifact}s key
	 * @return - a {@link List} with all found tags (only one occurence of a tag)
	 */
	public List<String> getAllTagsOfArtifact(String artifactKey) {
		DependencyStructureEntry e = nameToEntryMap.get(artifactKey);
		if (e == null) {
			log.warn("no solution found for dependency [" + artifactKey + "]");
			return null;
		}
		return e.getAllDependencyTags();
	}

	public boolean hasAggregationLineage(String terminalKey, String key) {
		DependencyStructureEntry e = nameToEntryMap.get( key);
		Map<String, List<String>> requestorToTagMap = e.getRequestorToTagMap();
		for (Entry<String, List<String>> entry : requestorToTagMap.entrySet()) {
			if (
					entry.getValue().contains( AGGREGATE) ||
					entry.getValue().contains( AGGREGATOR)
				) {
				if (entry.getKey().equalsIgnoreCase(terminalKey)) {
					return true;
				}
				else {
					if (hasAggregationLineage(terminalKey, entry.getKey())) {
						return true;
					}
				}
				
			}
		}		
		return false;
	}
}
