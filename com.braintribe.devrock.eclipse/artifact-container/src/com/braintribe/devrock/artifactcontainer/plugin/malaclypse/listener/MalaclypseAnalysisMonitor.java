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
package com.braintribe.devrock.artifactcontainer.plugin.malaclypse.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.merger.listener.DependencyMergerNotificationListener;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;

/**
 * collating information during the walk for analysis purposes, 
 * based on the notifications that are broadcast from the actual workers<br/>
 * uses an internal {@link Map} of Walk Id to {@link AnalysisContainer} to contain the different 
 * notifications of the running walks. 
 * @author pit
 *
 */
public class MalaclypseAnalysisMonitor implements 	WalkNotificationListener, 
													PomReaderNotificationListener, 
													ClashResolverNotificationListener, 
													DependencyMergerNotificationListener, 
													DependencyResolverNotificationListener, 
													SolutionEnricherNotificationListener {


	private Map<String, AnalysisContainer> scopeIdToContainerMap = new HashMap<String, AnalysisContainer>();
	private String requestId;
	
	public MalaclypseAnalysisMonitor(String requestId) {
		this.requestId = requestId;
	}
	
	public MalaclypseAnalysisMonitor(MalaclypseAnalysisMonitor sibling) {
		scopeIdToContainerMap.putAll(sibling.scopeIdToContainerMap);
		this.requestId = sibling.requestId;
	}
	
	private AnalysisContainer getContainerForId( String walkScopeId) {
		AnalysisContainer container = scopeIdToContainerMap.get(walkScopeId);
		if (container == null) {			
			if (ArtifactContainerPlugin.isDebugActive()) {
				String msg =  "no associated container found in walk scope [" + walkScopeId + "] in monitor for [" + requestId + "]";
				ArtifactContainerPlugin.log(msg);					
			}
			
			container = new AnalysisContainer();
			scopeIdToContainerMap.put(walkScopeId, container);
		}
		return container;
	}
	
	public WalkMonitoringResult getWalkMonitoringResult(String walkScopeId) {
		return getContainerForId( walkScopeId).getMonitoringResult();
	}
	
	@Override
	public void acknowledgeStartOn(String walkScopeId, Solution solution, WalkDenotationType denotationType) {
		AnalysisContainer container = new AnalysisContainer();
		scopeIdToContainerMap.put(walkScopeId, container);		
		container.acknowledgeStartOn(solution, denotationType);
	}
	
	@Override
	public void acknowledgeEndOn(String walkScopeId, Solution solution) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeEndOn(solution);
	}
	
	
	
	@Override
	public void acknowledgeWalkResult(String walkScopeId, List<Solution> solutions) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeWalkResult(solutions);			
	}
	
	
	@Override
	public void acknowledgeCollectedDependencies(String walkScopeId, List<Dependency> dependencies) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeCollectedDependencies(dependencies);
	}
	
	@Override
	public void acknowledgeFileEnrichmentFailure(String walkScopeId, Solution solution, PartTuple tuple) {		
	}



	@Override
	public void acknowledgeMerges(String walkScopeId, Set<Dependency> mergedDependencies) {		
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeMerges(mergedDependencies);
	}

	@Override
	public void acknowledgeDependencyClashes(String walkScopeId, Dependency winner, List<Dependency> dependencies) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeDependencyClashes(winner, dependencies);
		
	}

	@Override
	public void acknowledgeSolutionClashes(String walkScopeId, Solution winner, List<Solution> solutions) {	
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeSolutionClashes(winner, solutions);
	}
	
	
	@Override
	public void acknowledgeRedirection(String walkScopeId, Part part, Solution solution) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeRedirection( part, solution);		
	}
	
	@Override
	public void acknowledgeSolutionAssociation(String walkScopeId, String location, Artifact solution) {	
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeSolutionAssociation( location, solution);		
	}
	
	
	@Override
	public void acknowledgeParentAssociation(String walkScopeId, Artifact child, Solution parent) {	
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeParentAssociation( child, parent);			
	}
	
	@Override
	public void acknowledgeParentAssociationError(String walkScopeId, Artifact child, String groupId, String artifactId, String version) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeParentAssociationError( child, groupId, artifactId, version);		
		// log status 
		String msg="Parent lookup failed in [" + NameParser.buildName(child) + "] for dependency [" + groupId + ":" + artifactId + "#" + version;
		ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);	
	}
	
	@Override
	public void acknowledgeImportAssociation(String walkScopeId, Artifact requesting, Solution requested) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeImportAssociation( requesting, requested);		
	}

	@Override
	public void acknowledgeImportAssociationError(String walkScopeId, Artifact requesting, String groupId, String artifactId, String version) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeImportAssociationError( requesting, groupId, artifactId, version);		
		// log status 
		String msg="Import lookup failed in [" + NameParser.buildName(requesting) + "] for dependency [" + groupId + ":" + artifactId + "#" + version;
		ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);	
	}

	@Override
	public void acknowledgeReadErrorOnFile(String walkScopeId, String location, String reason) {			
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeReadErrorOnFile( location, reason);			
	}
	

	@Override
	public void acknowledgeReadErrorOnArtifact(String walkScopeId, Artifact solution, String reason) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeReadErrorOnArtifact( solution, reason);					
	}
	
	@Override
	public void acknowledgeReadErrorOnString(String walkScopeId, String string, String reason) {
	}
	
	@Override
	public void acknowledgeVariableResolvingError(String walkScopeId, Artifact artifact, String reason) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeVariableResolvingError(artifact, reason);		
	}
	
	
	@Override
	public void acknowledgeTraversing(String walkScopeId, Solution solution, Dependency parent, int level, boolean valid) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeTraversing(solution, parent, level, valid);			
	}
	
		
	@Override
	public void acknowledgeTraversing(String walkScopeId, Dependency dependency, Solution parent, int level) {	
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeTraversing( dependency, parent, level);
	}
	
	@Override
	public void acknowledgeTraversingEndpoint(String walkScopeId, Dependency dependency, Solution parent, int level) {		
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeTraversingEndpoint( dependency, parent, level);
	}
	@Override
	public void acknowledgeTraversingEndpoint(String walkScopeId, Solution solution, Dependency dependency, int level) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeTraversingEndpoint( solution, dependency, level);
	}
	
	@Override
	public void acknowledgeReassignedDependency(String walkScopeId, Dependency undetermined, Dependency replace) {		
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeReassignedDependency(undetermined, replace);
	}
	@Override
	public void acknowledgeUndeterminedDependency(String walkScopeId, Dependency undetermined) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeUndeterminedDependency(undetermined);
	}

	@Override
	public void acknowledgeUnresolvedDependency(String walkScopeId, Dependency unresolved) {
		AnalysisContainer container = getContainerForId( walkScopeId);
		container.acknowledgeUnresolvedDependency( unresolved);
	}
	
	/*
	 * IRRELEVANT CALLBACKS FOR DATA COLLECTION
	 */
	@Override
	public void acknowledgeFileEnrichmentSuccess(String walkScopeId, String arg0) {		
	}



	@Override
	public void acknowledgeDependencyClashResolvingPhase(String walkScopeId, int arg0) {		
	}

	@Override	
	public void acknowledgeDeterminationPhase(String walkScopeId, int arg0) {		
	}

	@Override
	public void acknowledgeEnrichingPhase(String walkScopeId, int arg0) {
	}

	@Override
	public void acknowledgeSolutionClashResolvingPhase(String walkScopeId, int arg0) {		
	}

	@Override
	public void acknowledgeTraversingPhase(String walkScopeId) {	
	}

	@Override
	public void acknowledgeSolutionEnriching(String walkScopeId, Solution solution) {	
	}

	@Override
	public void acknowledgeDependencyClashResolving(String walkScopeId, Dependency arg0) {		
	}

	@Override
	public void acknowledgeSolutionClashResolving(String walkScopeId, Solution solution) {		
	}

	@Override
	public void acknowledgeClashOnDependencyClassifier(String walkScopeId, Dependency dependency, String current, String requested) {}	

}
