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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.api.ui.viewers.artifacts.ResolutionViewerContextStorage;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer.Transposer;
import com.braintribe.devrock.eclipse.model.resolution.CapabilityKeys;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.storage.TranspositionContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;

/**
 * class that controls 
 * a) the capabilities that the viewers should expose
 * b) the content that the viewers should see
 *  
 * @author pit
 *
 */
public class ContainerResolutionViewController {
	
	private Transposer transposer;		
	private Map<String, Future<List<Node>>> futures = new ConcurrentHashMap<>();
	private ExecutorService executorService = Executors.newFixedThreadPool( 10);
	
	

	private Map<String, Map<CapabilityKeys, Boolean>> capabilites;
	{
		capabilites = new HashMap<>();
		
		// terminal
		Map<CapabilityKeys, Boolean> capabilitesForTerminalView = new HashMap<>();
		capabilitesForTerminalView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForTerminalView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForTerminalView.put( CapabilityKeys.shortNotation, true);
		capabilitesForTerminalView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForTerminalView.put( CapabilityKeys.filter, false);
		capabilitesForTerminalView.put( CapabilityKeys.dependencies, true);
		capabilitesForTerminalView.put( CapabilityKeys.parents, true);
		capabilitesForTerminalView.put( CapabilityKeys.parentDependers, true);
		capabilitesForTerminalView.put( CapabilityKeys.imports, true);
		capabilitesForTerminalView.put( CapabilityKeys.importDependers, true);
		capabilitesForTerminalView.put( CapabilityKeys.dependers, true);				
		capabilitesForTerminalView.put( CapabilityKeys.search, false);
		capabilitesForTerminalView.put( CapabilityKeys.detail, true);
		capabilitesForTerminalView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForTerminalView.put( CapabilityKeys.parts, false);
		capabilitesForTerminalView.put( CapabilityKeys.open, true);
		capabilitesForTerminalView.put( CapabilityKeys.copy, true);
		capabilitesForTerminalView.put( CapabilityKeys.saveResolution, true);
		capabilites.put( Transposer.CONTEXT_TERMINAL, capabilitesForTerminalView);
		
		// solutions
		Map<CapabilityKeys, Boolean> capabilitesForSolutionsView = new HashMap<>();
		capabilitesForSolutionsView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForSolutionsView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForSolutionsView.put( CapabilityKeys.shortNotation, true);
		capabilitesForSolutionsView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForSolutionsView.put( CapabilityKeys.filter, true);
		capabilitesForSolutionsView.put( CapabilityKeys.dependencies, true);
		capabilitesForSolutionsView.put( CapabilityKeys.dependers, true);
		capabilitesForSolutionsView.put( CapabilityKeys.parents, true);
		capabilitesForSolutionsView.put( CapabilityKeys.parentDependers, true);
		capabilitesForSolutionsView.put( CapabilityKeys.imports, true);
		capabilitesForSolutionsView.put( CapabilityKeys.importDependers, true);
		capabilitesForSolutionsView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForSolutionsView.put( CapabilityKeys.search, false);
		capabilitesForSolutionsView.put( CapabilityKeys.detail, true);
		capabilitesForSolutionsView.put( CapabilityKeys.parts, true);
		capabilitesForSolutionsView.put( CapabilityKeys.open, true);
		capabilitesForSolutionsView.put( CapabilityKeys.saveResolution, true);
		capabilitesForSolutionsView.put( CapabilityKeys.purge, true);
		capabilitesForSolutionsView.put( CapabilityKeys.copy, true);
		capabilites.put( Transposer.CONTEXT_SOLUTIONS, capabilitesForSolutionsView);
		
		// projects
		Map<CapabilityKeys, Boolean> capabilitesForProjectsView = new HashMap<>();
		capabilitesForProjectsView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForProjectsView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForProjectsView.put( CapabilityKeys.shortNotation, true);
		capabilitesForProjectsView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForProjectsView.put( CapabilityKeys.filter, true);
		capabilitesForProjectsView.put( CapabilityKeys.dependencies, true);
		capabilitesForProjectsView.put( CapabilityKeys.dependers, true);
		capabilitesForProjectsView.put( CapabilityKeys.parents,true);
		capabilitesForProjectsView.put( CapabilityKeys.parentDependers, true);
		capabilitesForProjectsView.put( CapabilityKeys.imports, true);
		capabilitesForProjectsView.put( CapabilityKeys.importDependers, true);
		capabilitesForProjectsView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForProjectsView.put( CapabilityKeys.search, false);
		capabilitesForProjectsView.put( CapabilityKeys.detail, true);
		capabilitesForProjectsView.put( CapabilityKeys.parts, false);
		capabilitesForProjectsView.put( CapabilityKeys.open, true);
		capabilitesForProjectsView.put( CapabilityKeys.saveResolution, true);
		capabilitesForProjectsView.put( CapabilityKeys.copy, true);
		capabilites.put( Transposer.CONTEXT_PROJECTS, capabilitesForProjectsView);
				
		
		// all
		Map<CapabilityKeys, Boolean> capabilitesForAllView = new HashMap<>();
		capabilitesForAllView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForAllView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForAllView.put( CapabilityKeys.shortNotation, true);
		capabilitesForAllView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForAllView.put( CapabilityKeys.filter, true);
		capabilitesForAllView.put( CapabilityKeys.dependencies, true);
		capabilitesForAllView.put( CapabilityKeys.dependers, true);		
		capabilitesForAllView.put( CapabilityKeys.parents, true);
		capabilitesForAllView.put( CapabilityKeys.parentDependers, true);
		capabilitesForAllView.put( CapabilityKeys.imports, true);
		capabilitesForAllView.put( CapabilityKeys.importDependers, true);
		capabilitesForAllView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForAllView.put( CapabilityKeys.search, false);
		capabilitesForAllView.put( CapabilityKeys.detail, true);
		capabilitesForAllView.put( CapabilityKeys.parts, true);
		capabilitesForAllView.put( CapabilityKeys.open, true);
		capabilitesForAllView.put( CapabilityKeys.saveResolution, true);
		capabilitesForAllView.put( CapabilityKeys.purge, true);
		capabilitesForAllView.put( CapabilityKeys.copy, true);
		capabilites.put( Transposer.CONTEXT_ALL, capabilitesForAllView);
		
		// parents
		Map<CapabilityKeys, Boolean> capabilitesForParentsView = new HashMap<>();
		capabilitesForParentsView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForParentsView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForParentsView.put( CapabilityKeys.shortNotation, true);
		capabilitesForParentsView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForParentsView.put( CapabilityKeys.filter, true);
		capabilitesForParentsView.put( CapabilityKeys.dependencies, false);
		capabilitesForParentsView.put( CapabilityKeys.dependers, false);		
		capabilitesForParentsView.put( CapabilityKeys.parents, true);
		capabilitesForParentsView.put( CapabilityKeys.parentDependers, true);
		capabilitesForParentsView.put( CapabilityKeys.imports, true);
		capabilitesForParentsView.put( CapabilityKeys.importDependers, true);
		capabilitesForParentsView.put( CapabilityKeys.visibleDependencies, false);
		capabilitesForParentsView.put( CapabilityKeys.search, false);
		capabilitesForParentsView.put( CapabilityKeys.detail, true);
		capabilitesForParentsView.put( CapabilityKeys.parts, false);
		capabilitesForParentsView.put( CapabilityKeys.open, true);
		capabilitesForParentsView.put( CapabilityKeys.saveResolution, true);
		capabilitesForParentsView.put( CapabilityKeys.purge, true);
		capabilitesForParentsView.put( CapabilityKeys.copy, true);
		capabilites.put( Transposer.CONTEXT_PARENTS, capabilitesForParentsView);
		
		
		// filtered
		Map<CapabilityKeys, Boolean> capabilitesForFilteredView = new HashMap<>();
		capabilitesForFilteredView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForFilteredView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForFilteredView.put( CapabilityKeys.shortNotation, true);
		capabilitesForFilteredView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForFilteredView.put( CapabilityKeys.filter, true);
		capabilitesForFilteredView.put( CapabilityKeys.dependencies, false);
		capabilitesForFilteredView.put( CapabilityKeys.dependers, true);
		capabilitesForFilteredView.put( CapabilityKeys.parents, false);
		capabilitesForFilteredView.put( CapabilityKeys.parentDependers, false);
		capabilitesForFilteredView.put( CapabilityKeys.imports, false);
		capabilitesForFilteredView.put( CapabilityKeys.importDependers, false);
		capabilitesForFilteredView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForFilteredView.put( CapabilityKeys.search, false);
		capabilitesForFilteredView.put( CapabilityKeys.detail, true);
		capabilitesForFilteredView.put( CapabilityKeys.parts, false);
		capabilitesForFilteredView.put( CapabilityKeys.coalesce, true);
		capabilitesForFilteredView.put( CapabilityKeys.open, false);
		capabilitesForFilteredView.put( CapabilityKeys.copy, true);
		capabilitesForFilteredView.put( CapabilityKeys.saveResolution, true);
		capabilites.put( Transposer.CONTEXT_FILTERED, capabilitesForFilteredView);
		
		// incomplete
		Map<CapabilityKeys, Boolean> capabilitesForIncompleteView = new HashMap<>();
		capabilitesForIncompleteView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForIncompleteView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForIncompleteView.put( CapabilityKeys.shortNotation, true);
		capabilitesForIncompleteView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForIncompleteView.put( CapabilityKeys.filter, true);
		capabilitesForIncompleteView.put( CapabilityKeys.dependencies, true);
		capabilitesForIncompleteView.put( CapabilityKeys.parents, true);
		capabilitesForIncompleteView.put( CapabilityKeys.parentDependers, true);
		capabilitesForIncompleteView.put( CapabilityKeys.imports, true);
		capabilitesForIncompleteView.put( CapabilityKeys.importDependers, true);
		capabilitesForIncompleteView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForIncompleteView.put( CapabilityKeys.dependers, true);				
		capabilitesForIncompleteView.put( CapabilityKeys.search, false);
		capabilitesForIncompleteView.put( CapabilityKeys.detail, true);
		capabilitesForIncompleteView.put( CapabilityKeys.parts, false);
		capabilitesForIncompleteView.put( CapabilityKeys.open, true);
		capabilitesForIncompleteView.put( CapabilityKeys.copy, true);
		capabilitesForIncompleteView.put( CapabilityKeys.saveResolution, true);
		capabilites.put( Transposer.CONTEXT_INCOMPLETE, capabilitesForIncompleteView);
		
		// unresolved
		Map<CapabilityKeys, Boolean> capabilitesForUnresolvedView = new HashMap<>();
		capabilitesForUnresolvedView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForUnresolvedView.put( CapabilityKeys.shortNotation, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.filter, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.dependencies, false);
		capabilitesForUnresolvedView.put( CapabilityKeys.dependers, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.parents, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.parentDependers, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.imports, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.importDependers, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.search, false);
		capabilitesForUnresolvedView.put( CapabilityKeys.detail, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.parts, false);
		capabilitesForUnresolvedView.put( CapabilityKeys.open, false);
		capabilitesForUnresolvedView.put( CapabilityKeys.copy, true);
		capabilitesForUnresolvedView.put( CapabilityKeys.saveResolution, true);
		capabilites.put( Transposer.CONTEXT_UNRESOLVED, capabilitesForUnresolvedView);

		// clashes
		Map<CapabilityKeys, Boolean> capabilitesForClashesView = new HashMap<>();
		capabilitesForClashesView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForClashesView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForClashesView.put( CapabilityKeys.shortNotation, true);
		capabilitesForClashesView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForClashesView.put( CapabilityKeys.filter, false);
		capabilitesForClashesView.put( CapabilityKeys.search, false);		
		capabilitesForClashesView.put( CapabilityKeys.dependencies, false);
		capabilitesForClashesView.put( CapabilityKeys.dependers, true);
		capabilitesForClashesView.put( CapabilityKeys.parents, false);
		capabilitesForClashesView.put( CapabilityKeys.parentDependers, false);
		capabilitesForClashesView.put( CapabilityKeys.imports, false);
		capabilitesForClashesView.put( CapabilityKeys.importDependers, false);
		capabilitesForClashesView.put( CapabilityKeys.detail, false);
		capabilitesForClashesView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForClashesView.put( CapabilityKeys.parts, false);
		capabilitesForClashesView.put( CapabilityKeys.open, false);
		capabilitesForClashesView.put( CapabilityKeys.copy, false);
		capabilitesForClashesView.put( CapabilityKeys.saveResolution, true);
		capabilites.put( Transposer.CONTEXT_CLASHES, capabilitesForClashesView);
		
		// detail view
		Map<CapabilityKeys, Boolean> capabilitesForDetailView = new HashMap<>();
		capabilitesForDetailView.put( CapabilityKeys.identifyProjects, true);
		capabilitesForDetailView.put( CapabilityKeys.visibleArtifactNature, false);
		capabilitesForDetailView.put( CapabilityKeys.shortNotation, true);
		capabilitesForDetailView.put( CapabilityKeys.visibleGroups, true);
		capabilitesForDetailView.put( CapabilityKeys.filter, false);
		capabilitesForDetailView.put( CapabilityKeys.dependencies, true);
		capabilitesForDetailView.put( CapabilityKeys.parents, true);
		capabilitesForDetailView.put( CapabilityKeys.parentDependers, true);
		capabilitesForDetailView.put( CapabilityKeys.imports, true);
		capabilitesForDetailView.put( CapabilityKeys.importDependers, true);
		capabilitesForDetailView.put( CapabilityKeys.visibleDependencies, true);
		capabilitesForDetailView.put( CapabilityKeys.dependers, true);				
		capabilitesForDetailView.put( CapabilityKeys.search, false);
		capabilitesForDetailView.put( CapabilityKeys.detail, true);
		capabilitesForDetailView.put( CapabilityKeys.parts, true);
		capabilitesForDetailView.put( CapabilityKeys.open, true);
		capabilitesForDetailView.put( CapabilityKeys.copy, true);
		capabilites.put( Transposer.CONTEXT_DETAIL, capabilitesForDetailView);
		
		
	}
	@Configurable @Required
	public void setTransposer(Transposer transposer) {
		this.transposer = transposer;
	}
	
	public Future<RepositoryReflection> preemptiveRepositoryReflectionRetrieval() {
		Future<RepositoryReflection> future = executorService.submit( () -> supplyRepositoryReflection());
		return future;
	}
	
	private RepositoryReflection supplyRepositoryReflection() {
		Maybe<RepositoryReflection> maybe = DevrockPlugin.instance().reflectRepositoryConfiguration();
		if (!maybe.isSatisfied()) {
			String msg = "Failed to retrieve repository-reflection:" + maybe.whyUnsatisfied().stringify();
			IStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.instance().log(status);
			return null;
		}
		RepositoryReflection repositoryReflection = maybe.get();
		return repositoryReflection;
	}

	public void preemptiveDataRetrieval( List<String> keys) {
		for (String key : keys) {
			TranspositionContext transpositionContext = ResolutionViewerContextStorage.loadTranspositionContextFromStorage( key);
			transpositionContext.setAssignedKey( key);
			Future<List<Node>> future = executorService.submit( () -> supplyNodes( key, transpositionContext, true));
			futures.put( key, future);
		}		
	}
	
	public List<Node> getInitialData(String key) {		
		Future<List<Node>> future = futures.get(key);
		if (future != null) {
			try {
				return future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return Collections.emptyList();
	}
	
	/**
	 * returns the proper {@link Node}s for the viewer with the key and context
	 * @param key - the {@link String} that identifies the viewer 
	 * @param context - the {@link TranspositionContext}
	 * @return 
	 */
	public List<Node> supplyNodes( String key, TranspositionContext context, boolean newTransposerInstance) {
		long before = System.nanoTime();
		List<Node> nodes;
		if (newTransposerInstance) {				
			nodes = supplyNodes(key, transposer.from(), context, null);
		} else {
			nodes = supplyNodes(key, context, null);
		}
		long after = System.nanoTime();
		System.out.println("transposing with key [" + key + "](" + context.getKey() + "/" + context.getAssignedKey() + ") [" + ((after-before)/1E6) + "] ms");
		return nodes;
	}
	
	public List<Node> supplyNodes( String key, Transposer transposer, TranspositionContext context, Node node) {
		switch (key) {
			case Transposer.CONTEXT_TERMINAL:
				return transposer.transposeTerminals(context);
			case Transposer.CONTEXT_ALL:
				return transposer.transposeAllArtifacts(context);
			case Transposer.CONTEXT_SOLUTIONS:
				return transposer.transposeSolutions(context);
			case Transposer.CONTEXT_CLASHES:
				return transposer.transposeClashes(context);
			case Transposer.CONTEXT_FILTERED:
				return transposer.transposeFiltered(context);
			case Transposer.CONTEXT_INCOMPLETE:
				return transposer.transposeIncomplete(context);
			case Transposer.CONTEXT_UNRESOLVED:
				return transposer.transposeUnresolved(context);
			case Transposer.CONTEXT_DETAIL: {
				if (node != null) {
					return Collections.singletonList( transposer.transposeDetailNode(context, node));
				}
			}
			case Transposer.CONTEXT_PARENTS: {
				return transposer.transposeParents(context);
			}
			case Transposer.CONTEXT_PROJECTS : {				
				return transposer.transposeProjectDependencies( context);
			}
		}
		return Collections.emptyList();
	}
		
	
	public List<Node> supplyNodes( String key, TranspositionContext context, Node node) {
		switch (key) {
			case Transposer.CONTEXT_TERMINAL:
				return transposer.transposeTerminals(context);
			case Transposer.CONTEXT_ALL:
				return transposer.transposeAllArtifacts(context);
			case Transposer.CONTEXT_SOLUTIONS:				
				return transposer.transposeSolutions(context);		
								
			case Transposer.CONTEXT_CLASHES:
				return transposer.transposeClashes(context);
			case Transposer.CONTEXT_FILTERED:
				return transposer.transposeFiltered(context);
			case Transposer.CONTEXT_INCOMPLETE:
				return transposer.transposeIncomplete(context);
			case Transposer.CONTEXT_UNRESOLVED:
				return transposer.transposeUnresolved(context);
			case Transposer.CONTEXT_DETAIL: {
				if (node != null) {
					return Collections.singletonList( transposer.transposeDetailNode(context, node));
				}
			}
			case Transposer.CONTEXT_PARENTS: {
				return  transposer.transposeParents(context);
			}
			case Transposer.CONTEXT_PROJECTS : {				
				return transposer.transposeProjectDependencies( context);
			}
		}
		return Collections.emptyList();
	}
	
	

	/**
	 * @param key - the identification of the viewer
	 * @param capKey - the {@link CapabilityKeys} that determines the requestes capability
	 * @return - true if capability is allowed/supported, false otherwise
	 */
	public Boolean supplyCapability(String key, CapabilityKeys capKey) {
		Map<CapabilityKeys, Boolean> cps = capabilites.get( key);
		if (cps == null) {
			// default for 'focused' new tabs (selected artifacts) 
			cps = capabilites.get( Transposer.CONTEXT_SOLUTIONS);
			if (cps == null)
				return false;
		}
		Boolean value = cps.get( capKey);
		if (value == null)
			return false;		
		return value;
	}
	
}
