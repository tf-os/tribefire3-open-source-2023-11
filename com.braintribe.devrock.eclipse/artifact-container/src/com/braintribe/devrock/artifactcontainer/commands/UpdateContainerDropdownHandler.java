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
package com.braintribe.devrock.artifactcontainer.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.plugin.commons.commands.MultiDropdownHandler;

public class UpdateContainerDropdownHandler extends MultiDropdownHandler {
	private static Logger log = Logger.getLogger(UpdateContainerDropdownHandler.class);
	private WorkspaceProjectRegistry workspaceRegistry = ArtifactContainerPlugin.getWorkspaceProjectRegistry();
	private String PARM_MSG = "com.braintribe.devrock.artifactcontainer.common.commands.command.param.sync";
	protected ArtifactContainerUpdateRequestType walkMode;
	
	public UpdateContainerDropdownHandler(ArtifactContainerUpdateRequestType mode){
		this.walkMode = mode;
	}
	
	
	@Override
	protected String getParamKey() {	
		return PARM_MSG;
	}

	@Override
	public void executeSingle(final IProject project) {
		// update registry
		ArtifactContainerPlugin.getWorkspaceProjectRegistry().update();
		
		if (project != null) {			
			Artifact artifact = workspaceRegistry.getArtifactForProject(project);
			if (artifact == null)
				return;									
							
			ArtifactContainer container = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerOfProject( project);
			if (container == null) {
				ArtifactContainerStatus status = new ArtifactContainerStatus( "Project [" + project.getName() + "] has no associated ArtifactContainer", IStatus.WARNING);
				ArtifactContainerPlugin.getInstance().log(status);	
				return;
			}
			
			List<ArtifactContainer> containers = new ArrayList<>();
			// try to find the dependers..
			if (ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getChainArtifactSync()) {
				List<ArtifactContainer> dependerContainers = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getDependerContainers( container);
				if (dependerContainers.size() > 0) {
					containers.addAll( dependerContainers);
					String synchedProjects = dependerContainers.stream().map( c -> {
						return c.getProject().getProject().getName();
					}).collect( Collectors.joining( ","));
					String msg = "add [" + synchedProjects + "]  containers as they are dependers of [" + project.getName() + "] which was the sync target ";
					log.debug(msg);
					ArtifactContainerPlugin.log(msg);
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.INFO);
					ArtifactContainerPlugin.getInstance().log(status);		
				}
			}
			else {
				containers.add(container);
			}
			
			
			// if we do sync, we remove the stored data and let the processor refill it 
			switch (walkMode) {
				case combined :
					container.clear();
					break;
				default:
					break;
			}			
			WiredArtifactContainerWalkController.getInstance().updateContainers( containers, walkMode);														
		}
	}

	@Override
	protected void executeMultipleCommand(boolean chain, IProject... projects) {
		// update registry
		ArtifactContainerPlugin.getWorkspaceProjectRegistry().update();
		
		Set<ArtifactContainer> containers = new HashSet<ArtifactContainer>();
		if (projects != null) {
			for (IProject project : projects) {
				Artifact artifact = workspaceRegistry.getArtifactForProject(project);
				if (artifact == null)
					continue;
				ArtifactContainer container = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerOfProject( project);
				if (container == null) {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "Project [" + project.getName() + "] has no associated ArtifactContainer", IStatus.WARNING);
					ArtifactContainerPlugin.getInstance().log(status);	
					continue;
				}
				// if we do sync, we remove the stored data and let the processor refill it
				switch (walkMode) {
					case combined :
						container.clear();
						break;
					default:
						break;
				}			
				containers.add(container);
				
				// calculate dependers
				if (chain && ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getChainArtifactSync()) {
					List<ArtifactContainer> dependerContainers = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getDependerContainers(container);
					if (dependerContainers.size() > 0) {
						containers.addAll( dependerContainers);
						String synchedProjects = dependerContainers.stream().map( c -> {
							return c.getProject().getProject().getName();
						}).collect( Collectors.joining( ","));
						String msg = "adding [" + synchedProjects + "] (" + dependerContainers.size() + ") containers as they are dependers of [" + project.getName() + "] which was the sync target ";
						log.debug(msg);
						ArtifactContainerPlugin.log(msg);
							
					}
				}								
			}
		}
		// shouldn't be required... but *maybe* there a several containers for the same project?
		Set<String> projectNames = new HashSet<>();
		Iterator<ArtifactContainer> iterator = containers.iterator();
		while (iterator.hasNext()) {
			ArtifactContainer container = iterator.next();
			String name = container.getProject().getProject().getName();
			if (!projectNames.add( name)) {
				String msg = "duplicate container [" + name + "] detected, skipping";
				log.debug(msg);
				ArtifactContainerPlugin.log(msg);
				iterator.remove();
			}			
		}

		// protocol 
		if (ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getChainArtifactSync()) {
			String initiations = Arrays.asList( projects).stream().map( c -> {
				return c.getProject().getProject().getName();
			}).collect( Collectors.joining( ","));		
			String msg;
			if (chain) {
				String requests = projectNames.stream().collect(Collectors.joining(","));
				msg = "auto-chain : sync initiation on [" + initiations + "] lead to (" + projectNames.size() + ") resync requests for [" + requests + "]";
			}
			else {
				msg = "no chaining : sync initiation on [" + initiations + "] doesn't trigger any other requests";
			}
			log.debug(msg);
			ArtifactContainerPlugin.log(msg);
			
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.INFO);
			ArtifactContainerPlugin.getInstance().log(status);
		}
				
		
		WiredArtifactContainerWalkController.getInstance().updateContainers( new ArrayList<>(containers), walkMode);
	}

	

	
}
