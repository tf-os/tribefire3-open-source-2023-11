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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.coding.ArtifactWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.ParentContainer;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;
import com.braintribe.model.malaclypse.container.DependencyContainer;
import com.braintribe.model.malaclypse.container.DependencyTraversingEvent;
import com.braintribe.model.malaclypse.container.SolutionContainer;
import com.braintribe.model.malaclypse.container.SolutionTraversingEvent;

/**
 * container for the analysis information collated by the {@link MalaclypseAnalysisMonitor}, this
 * one is specific for a single walk.
 * 
 * @author pit
 *
 */
public class AnalysisContainer {
	private WalkMonitoringResult monitoringResult;
	private long startNanos;

	private Map<String, SolutionTraversingEvent> locationToEventMap;
	private Map<Artifact, SolutionTraversingEvent> artifactToEventMap;
	private Map<Dependency, DependencyTraversingEvent> dependencyToEventMap;
	private Map<Artifact, ParentContainer> codingChildToParentMap;
	private Map<Artifact, ParentContainer> codingParentToContainerMap;

	private Set<Dependency> resultingDependencies = CodingSet.createHashSetBased( new DependencyWrapperCodec());

	
	public AnalysisContainer() {	
		monitoringResult = WalkMonitoringResult.T.create();
		monitoringResult.setTimestamp( new Date());
		
		locationToEventMap = new HashMap<String, SolutionTraversingEvent>();		
		artifactToEventMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		dependencyToEventMap = CodingMap.createHashMapBased( new DependencyWrapperCodec());
		
		codingChildToParentMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		codingParentToContainerMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		
		startNanos = System.nanoTime();				
	}
	
	public WalkMonitoringResult getMonitoringResult(){
		return monitoringResult;
	}
	
	public void acknowledgeStartOn(Solution terminalSolution, WalkDenotationType denotationType){
		monitoringResult.setTerminal(terminalSolution);
		monitoringResult.setWalkDenotationType(denotationType);	
		System.out.println("associating monitor [" + this + "] with [" + NameParser.buildName( terminalSolution) + "]");		
	}
	
	public void acknowledgeEndOn( Solution solution) {
		long endNanos = System.nanoTime();
		monitoringResult.setDurationInMs( new Double((endNanos - startNanos) / 1E6).longValue());
		
		// child to parent 
		monitoringResult.getParentContainerAssociationMap().putAll( codingParentToContainerMap);
		for (Entry<Artifact, ParentContainer> entry : codingChildToParentMap.entrySet()) {
			monitoringResult.getParentAssociationMap().put( entry.getKey(), entry.getValue().getParent());			
		}
		
		
		// imports.. 
		
		// weed-out any unresolved dependency that is not part of the final set 
		Iterator<Dependency> iterator = monitoringResult.getUnresolvedDependencies().iterator();
		while (iterator.hasNext()) {
			Dependency unresolved = iterator.next();
			if (!resultingDependencies.contains(unresolved)) {
				iterator.remove();
			}
		}
	}
	
	public void acknowledgeWalkResult(List<Solution> solutions) {
		monitoringResult.setSolutions(solutions);		
	}
	
	
	public void acknowledgeCollectedDependencies(List<Dependency> dependencies) {
		resultingDependencies.addAll(dependencies);
	}

	public void acknowledgeMerges(Set<Dependency> mergedDependencies) {
		monitoringResult.getMergedDependencies().addAll(mergedDependencies);
	}
	public void acknowledgeDependencyClashes(Dependency winner, List<Dependency> dependencies) {
		DependencyContainer container = DependencyContainer.T.create();
		container.getDependencies().addAll(dependencies);
		Map<Dependency, DependencyContainer> dependencyClashes = monitoringResult.getDependencyClashes();
		dependencyClashes.put(winner, container);	
	}
	public void acknowledgeSolutionClashes(Solution winner, List<Solution> solutions) {	
		SolutionContainer container = SolutionContainer.T.create();
		container.getSolutions().addAll(solutions);
		Map<Solution, SolutionContainer> map = monitoringResult.getSolutionClashes();
		map.put(winner, container);
	}
	public void acknowledgeRedirection( Part part, Solution solution) {
		monitoringResult.getRedirectionMap().put(part, solution);
		acknowledgeSolutionAssociation( part.getLocation(), part);
		SolutionTraversingEvent injected = locationToEventMap.get(part.getLocation());
		injected.setInjectedPerRedirection(true);		
	}
	
	public void acknowledgeSolutionAssociation( String location, Artifact solution){
		SolutionTraversingEvent traversingEvent = locationToEventMap.get(location);
		if (traversingEvent == null || traversingEvent.getInjectedPerRedirection()) {
			traversingEvent = artifactToEventMap.get(solution);
		}
		if (traversingEvent == null) {
			traversingEvent = SolutionTraversingEvent.T.create();			
			monitoringResult.getTraversingEvents().add(traversingEvent);
			locationToEventMap.put(location, traversingEvent);
			artifactToEventMap.put(solution, traversingEvent);
		}
		traversingEvent.setLocation(location);
		traversingEvent.setArtifact( solution);
		traversingEvent.setValidity(true);
	}
	
	public void acknowledgeParentAssociation(Artifact requesting, Solution requested) {
		ParentContainer parentContainer = codingParentToContainerMap.get(requested);
		if (parentContainer == null) {		 
			parentContainer = ParentContainer.T.create();
			parentContainer.setParent(requested);
			codingParentToContainerMap.put( requested, parentContainer);
			//System.out.println("no parent container found for child [" + NameParser.buildName( requesting) + "]: assigning new one to child");
		}
		else {
			//System.out.println("found parent container for child [" + NameParser.buildName( requesting) + "]: assigning existing one to child");
			;
			// might need to update the solution, as it may be faked
			//parentContainer.setParent(requested);
		}
		codingChildToParentMap.put( requesting, parentContainer);
		
		SolutionTraversingEvent parentEvent = artifactToEventMap.get(requested);
		if (parentEvent != null) {
			parentEvent.setParentNature( true);
			//System.out.println(  "parent event created to associate requesting [" + NameParser.buildName(requesting) + "] to requested parent [" + NameParser.buildName(requested) + "]");
		} 
		else {
			ArtifactContainerPlugin.log( "no parent event found for [" + NameParser.buildName(requesting) + "] to assign parent [" + NameParser.buildName(requested) + "]");
		}		
	}
	
	public void acknowledgeParentAssociationError( Artifact requesting, String groupId, String artifactId, String version){
		SolutionTraversingEvent parentEvent = artifactToEventMap.get( requesting);
		String requested = groupId + ":" + artifactId + "#" + version;
		if (parentEvent != null) {
			parentEvent.getFailedImports().add( requested);
		}
		else {
			ArtifactContainerPlugin.log( "no parent event found for [" + NameParser.buildName( requesting) + "] to assign import error on [" + requested + "]");
		}
	}
	
	public void acknowledgeImportAssociation( Artifact requesting, Solution requested) {
		ParentContainer parentContainer = codingParentToContainerMap.get(requesting);
		if (parentContainer == null) {
			//System.out.println("no parent container found yet for parent [" + NameParser.buildName( requesting) + "]: creating");
			parentContainer = ParentContainer.T.create();
			Solution solution = Solution.T.create();
			solution.setGroupId( requesting.getGroupId());
			solution.setArtifactId( requesting.getArtifactId());
			solution.setVersion( requesting.getVersion());
			solution.getImported().addAll( requesting.getImported());
			parentContainer.setParent( solution);
			codingParentToContainerMap.put( requesting, parentContainer);
		}
		
		parentContainer.getImports().add(requested);
		
		
		SolutionTraversingEvent parentEvent = artifactToEventMap.get( requested);
		if (parentEvent != null) {
			parentEvent.setImportNature( true);
			//System.out.println( "import association event created to associate requesting [" + NameParser.buildName(requesting) + "] to requested import [" + NameParser.buildName(requested) + "]");
		} 
		else {
			ArtifactContainerPlugin.log( "no parent event found for [" + NameParser.buildName(requesting) + "] to assign import [" + NameParser.buildName(requested) + "]");
		}
	}
	public void acknowledgeImportAssociationError( Artifact requesting, String groupId, String artifactId, String version){
		SolutionTraversingEvent parentEvent = artifactToEventMap.get( requesting);
		String requested = groupId + ":" + artifactId + "#" + version;
		if (parentEvent != null) {
			parentEvent.getFailedImports().add( requested);
		}
		else {
			ArtifactContainerPlugin.log( "no parent event found for [" + NameParser.buildName( requesting) + "] to assign import error on [" + requested + "]");
		}
	}
	
	public void acknowledgeReadErrorOnFile(String location, String reason){
		SolutionTraversingEvent event = locationToEventMap.get( location);
		if (event != null) {
			event.setValidity(false);
			event.getErrorMessages().add(reason);
		}
	}
	public void acknowledgeReadErrorOnArtifact(Artifact solution, String reason) {
		SolutionTraversingEvent event = artifactToEventMap.get( solution);
		if (event != null) {
			event.setValidity(false);
			event.getErrorMessages().add( reason);
		}	
	}
	public void acknowledgeVariableResolvingError(Artifact artifact, String reason) {
		SolutionTraversingEvent event = artifactToEventMap.get( artifact);
		if (event != null) {
			event.setValidity( false);
			event.getErrorMessages().add(reason);
		}
	}
	public void acknowledgeTraversing( Solution solution, Dependency parent, int level, boolean valid){
		SolutionTraversingEvent event = artifactToEventMap.get( solution);
		if (event == null) {
			event = SolutionTraversingEvent.T.create();			
			event.setArtifact(solution);
			artifactToEventMap.put(solution, event);
			monitoringResult.getTraversingEvents().add( event);
		}
		event.setParent(parent);
		event.setIndex(level);
		event.setValidity(valid);
	}
	public void acknowledgeTraversing( Dependency dependency, Solution parent, int level) {
		DependencyTraversingEvent event = dependencyToEventMap.get( dependency);
		if (event == null) {
			event = DependencyTraversingEvent.T.create();			
			event.setDependency(dependency);
			event.setParent(parent);
			monitoringResult.getTraversingEvents().add( event);
		}
		event.setIndex(level);
	}
	public void acknowledgeTraversingEndpoint( Dependency dependency, Solution parent, int level) {		
		DependencyTraversingEvent event = DependencyTraversingEvent.T.create();			
		event.setDependency(dependency);
		event.setParent(parent);
		event.setEndpoint( true);			
		monitoringResult.getTraversingEvents().add( event);		
		event.setIndex(level);
	}
	public void acknowledgeTraversingEndpoint( Solution solution, Dependency dependency, int level) {
		SolutionTraversingEvent event = SolutionTraversingEvent.T.create();
		event.setArtifact(solution);
		event.setParent(dependency);
		event.setIndex(level);
		event.setEndpoint(true);
		monitoringResult.getTraversingEvents().add(event);
	}
	public void acknowledgeReassignedDependency( Dependency undetermined, Dependency replace) {		
		monitoringResult.getDependencyReassignments().put( undetermined, replace);
		// AC's display logic requires reassigned deps to also appear as undetermined
		// even if they aren't anymore for the walk itself. 
		acknowledgeUndeterminedDependency(undetermined);
	}
	public void acknowledgeUndeterminedDependency( Dependency undetermined) {
		monitoringResult.getUndeterminedDependencies().add(undetermined);
	}
	
	public void acknowledgeUnresolvedDependency( Dependency unresolved) {
		monitoringResult.getUnresolvedDependencies().add(unresolved);
	}
}
