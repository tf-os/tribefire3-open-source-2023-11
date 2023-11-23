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
package com.braintribe.devrock.artifactcontainer.control.monitor;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.listener.PomReaderNotificationListener;
import com.braintribe.build.artifact.retrieval.multi.enriching.listener.SolutionEnricherNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.merger.listener.DependencyMergerNotificationListener;
import com.braintribe.build.artifact.walk.multi.listener.WalkNotificationListener;
import com.braintribe.build.process.ProcessAbortSignaller;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;

public class WalkProgressMonitor implements WalkNotificationListener, 
											SolutionEnricherNotificationListener, 
											ClashResolverNotificationListener, 
											ClasspathEntryBuilderNotificationListener,
											DependencyMergerNotificationListener,
											PomReaderNotificationListener,
											ProcessAbortSignaller{

	private IProgressMonitor progressMonitor;
	private SubMonitor monitor;
	
	public WalkProgressMonitor(IProgressMonitor iProgressMonitor) {
		this.progressMonitor = iProgressMonitor;
		monitor = SubMonitor.convert(iProgressMonitor);
	}
	
	@Override
	public void acknowledgeStartOn(String walkScopeId, Solution solution, WalkDenotationType denotationType) {
		monitor.setTaskName("Synching [" + NameParser.buildName(solution) + "]");
	}
	
	@Override
	public void acknowledgeEndOn(String walkScopeId,Solution arg0) {
		progressMonitor.done();
	}
	

	@Override
	public void acknowledgeWalkResult(String walkScopeId,List<Solution> solutions) {}
	
	@Override
	public void acknowledgeCollectedDependencies(String walkScopeId,List<Dependency> dependencies) {}

	@Override
	public void acknowledgeReassignedDependency(String walkScopeId,Dependency undetermined, Dependency replaced) {}

	@Override
	public void acknowledgeTraversingEndpoint(String walkScopeId,Dependency dependency, Solution parent, int level) {}
	@Override
	public void acknowledgeTraversingEndpoint(String walkScopeId,Solution solution, Dependency parent, int level) {}

	@Override
	public void acknowledgeTraversing(String walkScopeId,Solution solution, Dependency parent, int level, boolean valid) {
		monitor.subTask( "examining [" + NameParser.buildName(solution) + "]");		
	}
	
	@Override
	public void acknowledgeTraversing(String walkScopeId, Dependency dependency, Solution parent, int arg1) {			
		monitor.subTask( "examining [" + NameParser.buildName(dependency) + "]");
	}

	@Override
	public void acknowledgeUndeterminedDependency(String walkScopeId, Dependency dependency) {
		Artifact parent = dependency.getRequestors().iterator().next();
		String msg="Undetermined: [" + NameParser.buildName(dependency) + "] of [" + NameParser.buildName(parent, parent.getVersion()) +"]";
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.WARNING);
		ArtifactContainerPlugin.getInstance().log(status);	
	}

	@Override
	public void acknowledgeUnresolvedDependency(String walkScopeId, Dependency dependency) {		
		Artifact parent = dependency.getRequestors().iterator().next();
		String msg="Unresolved: [" + NameParser.buildName(dependency) + "] of [" + NameParser.buildName(parent, parent.getVersion()) +"]";
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);	
	}

	@Override
	public void acknowledgeDependencyClashResolvingPhase(String walkScopeId, int num) {
		monitor.beginTask("dependency clash resolving", IProgressMonitor.UNKNOWN);
	}
	

	@Override
	public void acknowledgeDeterminationPhase(String walkScopeId, int num) {
		monitor.beginTask("determining undetermined", IProgressMonitor.UNKNOWN);		
	}

	@Override
	public void acknowledgeEnrichingPhase(String walkScopeId, int num) {
		monitor.beginTask("enriching", IProgressMonitor.UNKNOWN);
	}

	@Override
	public void acknowledgeSolutionClashResolvingPhase(String walkScopeId, int num) {
		monitor.beginTask("solution clash resolving", IProgressMonitor.UNKNOWN);
	}

	@Override
	public void acknowledgeTraversingPhase(String walkScopeId ) {
		monitor.beginTask( "traversing", IProgressMonitor.UNKNOWN);
	}

	@Override
	public void acknowledgeFileEnrichmentFailure(String walkScopeId, Solution solution, PartTuple partTuple) {}

	@Override
	public void acknowledgeFileEnrichmentSuccess(String walkScopeId, String name) {}

	@Override
	public void acknowledgeSolutionEnriching(String walkScopeId, Solution solution) {	
		monitor.subTask( "examining [" + NameParser.buildName( solution) + "]");		
	}

	@Override
	public void acknowledgeDependencyClashResolving(String walkScopeId, Dependency dependency) {
		monitor.subTask( "clash resolving on dependency [" + NameParser.buildName( dependency) + "]");		
	}

	@Override
	public void acknowledgeDependencyClashes(String walkScopeId, Dependency winner, List<Dependency> dependencies) {	
	}

	@Override
	public void acknowledgeSolutionClashResolving(String walkScopeId, Solution solution) {
		monitor.subTask( "clash resolving on solution [" + NameParser.buildName( solution) + "]");
		
	}

	@Override
	public void acknowledgeSolutionClashes(String walkScopeId, Solution winner, List<Solution> solutions) {}

	@Override
	public boolean abortScan() {	
		return progressMonitor.isCanceled();
	}

	
	@Override
	public void acknowledgeClasspathBuilding() {
		monitor.beginTask("building classpath", IProgressMonitor.UNKNOWN);
		
	}

	@Override
	public void acknowledgeJarClasspathEntry(Solution solution) {
		monitor.subTask( "built jar entry for [" + NameParser.buildName(solution) + "]");
		
	}
	@Override
	public void acknowledgeProjectClasspathEntry(Solution solution) {
		monitor.subTask( "built project entry for [" + NameParser.buildName(solution) + "]");
		
	}

	@Override
	public void acknowledgeMerges(String walkScopeId, Set<Dependency> merges) {}



	@Override
	public void acknowledgeReadErrorOnFile(String walkScopeId, String location, String reason) {		
		String msg="Read error on pom [" + location + "]:" + reason;
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);				
	}
	@Override
	public void acknowledgeVariableResolvingError(String walkScopeId, Artifact artifact, String message) {
		String msg="Variable resolving error on pom [" + NameParser.buildName(artifact, artifact.getVersion()) + "]:" + message;
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);			
	}

	@Override
	public void acknowledgeReadErrorOnArtifact(String walkScopeId, Artifact artifact, String message) {
		String msg="Read error on pom [" + NameParser.buildName(artifact, artifact.getVersion()) + "]:" + message;
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);	
	}

	@Override
	public void acknowledgeReadErrorOnString(String walkScopeId, String contents, String message) {
		String msg="Read error on pom contents [" + contents + "]:" + message;
		ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.ERROR);
		ArtifactContainerPlugin.getInstance().log(status);		
		
	}

	@Override
	public void acknowledgeSolutionAssociation(String walkScopeId, String location, Artifact artifact) {}

	@Override
	public void acknowledgeParentAssociation(String walkScopeId, Artifact child, Solution parent) {}
	
	

	@Override
	public void acknowledgeParentAssociationError(String walkScopeId, Artifact child, String groupId, String artifactId, String version) {}

	@Override
	public void acknowledgeImportAssociation(String walkScopeId, Artifact requesting, Solution requested) {}

	@Override
	public void acknowledgeImportAssociationError(String walkScopeId, Artifact requesting, String groupId, String artifactId, String version) {}

	@Override
	public void acknowledgeClashOnDependencyClassifier(String walkScopeId, Dependency dependency, String current, String requested) {
		String msg="Classifier clash detected: dependency [" + NameParser.buildName(dependency) + "] occurs with two differing classifiers, [" + current + "] and [" + requested + "]";
		ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
		ArtifactContainerPlugin.getInstance().log(status);	
	}	
	
	
	

	
	
}
