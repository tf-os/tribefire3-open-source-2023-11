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
package com.braintribe.devrock.artifactcontainer.container.diagnostics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.model.artifact.Solution;

public class ContainerClasspathDiagnosticsRegistry implements ContainerClasspathDiagnosticsListener {
	
	private ArtifactContainerRegistry containerRegistry;
	private Map<IProject, ProjectClasspathDiagnosticsResult> projectToResultMap = new HashMap<IProject, ProjectClasspathDiagnosticsResult>();
	
	@Required @Configurable
	public void setContainerRegistry(ArtifactContainerRegistry containerRegistry) {
		this.containerRegistry = containerRegistry;
	}
	
	/**
	 * lazy access to the {@link ProjectClasspathDiagnosticsResult}
	 * @param container - the {@link ArtifactContainer} 
	 * @return - {@link ProjectClasspathDiagnosticsResult} if any or null if no project info stored 
	 */
	private ProjectClasspathDiagnosticsResult getProjectDiagnostics( ArtifactContainer container) {
		IProject project = containerRegistry.getProjectOfContainer(container);
		if (project != null) { 
			ProjectClasspathDiagnosticsResult projectClasspathDiagnosticsResult = projectToResultMap.get( project);
			if (projectClasspathDiagnosticsResult == null) {
				projectClasspathDiagnosticsResult = new ProjectClasspathDiagnosticsResult();
				projectToResultMap.put(project, projectClasspathDiagnosticsResult);
			}
			return projectClasspathDiagnosticsResult;
		}
		return null;
	}
	
	/**
	 * lazy access to the {@link ContainerClasspathDiagnosticsResult}
	 * @param container - the {@link ArtifactContainer}
	 * @param requestType -the {@link ArtifactContainerUpdateRequestType}
	 * @return - the {@link ContainerClasspathDiagnosticsResult} if any, or null if no project info's stored 
	 */
	private ContainerClasspathDiagnosticsResult getContainerDiagnostics( ArtifactContainer container, ArtifactContainerUpdateRequestType requestType) {
		ProjectClasspathDiagnosticsResult projectDiagnostics = getProjectDiagnostics(container);
		if (projectDiagnostics == null) {
			return null;
		}
		
		Map<ArtifactContainerUpdateRequestType, ContainerClasspathDiagnosticsResult> mappedResult = projectDiagnostics.getResults();
		if (mappedResult == null) {
			mappedResult = new HashMap<ArtifactContainerUpdateRequestType, ContainerClasspathDiagnosticsResult>();
			projectDiagnostics.setResults(mappedResult);
		}
		ContainerClasspathDiagnosticsResult containerDiagnostics = mappedResult.get(requestType);
		if (containerDiagnostics == null) {
			containerDiagnostics = new ContainerClasspathDiagnosticsResult();
			mappedResult.put(requestType, containerDiagnostics);
		}
		return containerDiagnostics;
	}

	@Override
	public void acknowledgeContainerProcessingStart(ArtifactContainer container, ArtifactContainerUpdateRequestType mode) {
		IProject project = containerRegistry.getProjectOfContainer(container);
		if (project != null) { 
			ProjectClasspathDiagnosticsResult projectClasspathDiagnosticsResult = projectToResultMap.get( project);
			if (projectClasspathDiagnosticsResult == null) {
				projectClasspathDiagnosticsResult = new ProjectClasspathDiagnosticsResult();
				projectToResultMap.put(project, projectClasspathDiagnosticsResult);
			}
			Map<ArtifactContainerUpdateRequestType, ContainerClasspathDiagnosticsResult> mappedResult = projectClasspathDiagnosticsResult.getResults();
			if (mappedResult != null) {
				mappedResult.put(mode, new ContainerClasspathDiagnosticsResult());
			}					
		}	
	}

	@Override
	public void acknowledgeContainerProcessingEnd(ArtifactContainer container, ArtifactContainerUpdateRequestType mode) {		
	}

	@Override
	public void acknowledgeSolutionPomPackagedAndReferencedAsPom(ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution) {
		ContainerClasspathDiagnosticsResult containerDiagnostics = getContainerDiagnostics(container, requestType);
		if (containerDiagnostics != null)
			containerDiagnostics.addToPomAggregates(solution);
	}

	@Override
	public void acknowledgeSolutionPomPackagedAndReferencedAsJarSolution(ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution) {
		ContainerClasspathDiagnosticsResult containerDiagnostics = getContainerDiagnostics(container, requestType);
		if (containerDiagnostics != null)
			containerDiagnostics.addToPomAggregatesReferencedAsJars(solution);
		
	}

	@Override
	public void acknowledgeSolutionJarPackagedAndReferencedAsPom(ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution) {
		ContainerClasspathDiagnosticsResult containerDiagnostics = getContainerDiagnostics(container, requestType);
		if (containerDiagnostics != null)
			containerDiagnostics.addtoJarReferencedAsPomAggregates(solution);		
	}
	


	@Override
	public void acknowledgeSolutionNonJarPackagedAndReferencedAsClassesJarSolution(ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution) {
		ContainerClasspathDiagnosticsResult containerDiagnostics = getContainerDiagnostics(container, requestType);
		if (containerDiagnostics != null)
			containerDiagnostics.addToNonJarReferencedAsClassesJar(solution);
		
	}

	/**
	 * get the {@link ClasspathDiagnosticsClassification} of the given {@link Solution} in the container 
	 * @param project - the {@link IProject} that contains the container
	 * @param requestType - the {@link ArtifactContainerUpdateRequestType} of the container 
	 * @param solution - the {@link Solution} to look for
	 * @return - the {@link ClasspathDiagnosticsClassification} for the {@link Solution}
	 */
	public ClasspathDiagnosticsClassification getClasspathClassificationForSolution( IProject project, ArtifactContainerUpdateRequestType requestType, Solution solution) {		
		if (project == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
			 
		ProjectClasspathDiagnosticsResult projectClasspathDiagnosticsResult = projectToResultMap.get( project);
		if (projectClasspathDiagnosticsResult == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		Map<ArtifactContainerUpdateRequestType, ContainerClasspathDiagnosticsResult> mappedResult = projectClasspathDiagnosticsResult.getResults();
		if (mappedResult == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		ContainerClasspathDiagnosticsResult containerDiagnostics = mappedResult.get(requestType);
		if (containerDiagnostics == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		return containerDiagnostics.getDiagnosticsClassificationOfSolution(solution);								
	}
	
	/**
	 * get the overall {@link ClasspathDiagnosticsClassification} of a container
	 * @param project - the {@link IProject} of the container 
	 * @param requestType - the {@link ArtifactContainerUpdateRequestType} of the container 
	 * @return - the overall {@link ClasspathDiagnosticsClassification} of the container 
	 */
	public ClasspathDiagnosticsClassification getClasspathClassificationForSolution( IProject project, ArtifactContainerUpdateRequestType requestType) {
		if (project == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
			 
		ProjectClasspathDiagnosticsResult projectClasspathDiagnosticsResult = projectToResultMap.get( project);
		if (projectClasspathDiagnosticsResult == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		Map<ArtifactContainerUpdateRequestType, ContainerClasspathDiagnosticsResult> mappedResult = projectClasspathDiagnosticsResult.getResults();
		if (mappedResult == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		ContainerClasspathDiagnosticsResult containerDiagnostics = mappedResult.get(requestType);
		return containerDiagnostics.getDiagnosticsClassification();
	}
	
	/**
	 * get the overall {@link ClasspathDiagnosticsClassification} of a project 
	 * @param project - the {@link IProject} 
	 * @return - the overall {@link ClasspathDiagnosticsClassification} of the project 
	 */
	public ClasspathDiagnosticsClassification getClasspathClassificationForSolution( IProject project) {
		if (project == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
			 
		ProjectClasspathDiagnosticsResult projectClasspathDiagnosticsResult = projectToResultMap.get( project);
		if (projectClasspathDiagnosticsResult == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		Map<ArtifactContainerUpdateRequestType, ContainerClasspathDiagnosticsResult> mappedResult = projectClasspathDiagnosticsResult.getResults();
		if (mappedResult == null) {
			return ClasspathDiagnosticsClassification.standard;
		}
		ClasspathDiagnosticsClassification classification = ClasspathDiagnosticsClassification.standard;
		
		ContainerClasspathDiagnosticsResult containerDiagnostics = mappedResult.get(ArtifactContainerUpdateRequestType.compile);
		if (containerDiagnostics != null) {
			classification = containerDiagnostics.getDiagnosticsClassification();
		}
		
		if (classification != ClasspathDiagnosticsClassification.standard)
			return classification;
		
		containerDiagnostics = mappedResult.get(ArtifactContainerUpdateRequestType.launch);
		if (containerDiagnostics != null) {
			classification = containerDiagnostics.getDiagnosticsClassification();
		}
		
		return classification;
	}
	
}
