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
package com.braintribe.devrock.artifactcontainer.control.walk.scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.logging.Logger;

/**
 * a registry for "expectations", i.e. when a run over multiple projects is run.
 *  
 * @author pit
 *
 */
public class ContainerProcessingExpectationRegistry {
	private static Logger log = Logger.getLogger(ContainerProcessingExpectationRegistry.class);
	private Map<String, Map<String, ContainerProcessingExpectation>> scopes = new LinkedHashMap<String, Map<String, ContainerProcessingExpectation>>();
	private Stack<String> autoBuildBlockingProcessingScopes = new Stack<>();
	private boolean initialValue;

	/**
	 * creates a new processing scope 	
	 */
	public String createNewProcessingScope(){
		Map<String, ContainerProcessingExpectation> scopeMap = new HashMap<String, ContainerProcessingExpectation>();
		String id = UUID.randomUUID().toString();
		scopes.put(id, scopeMap);
		//log.debug("initiation processing scope [" + id + "]");
		return id;
	}
	
	/**
	 * add a container expectation to the scope
	 */
	public void addProcessorExpectationToScope( String id, ArtifactContainer container){
		ContainerProcessingExpectation expectation = new ContainerProcessingExpectation();
		expectation.setContainer(container);
		expectation.setId( container.getId());
		Map<String, ContainerProcessingExpectation> scopeMap = scopes.get(id);
		if (scopeMap == null) {
			return;
		}
		scopeMap.put( container.getId(), expectation);
		
	}
	/**
	 * mark a container as being successfully processed	
	 */
	public void markProcessorExpectationAsResolved( String id, String containerId){
		Map<String, ContainerProcessingExpectation> scopeMap = scopes.get(id);
		if (scopeMap == null) {
			return;
		}
		ContainerProcessingExpectation expectation = scopeMap.get(containerId);
		if (expectation == null) {
			return;
		}
		expectation.setProcessed(true);
	}
	
	/**
	 * checks if a scope has been processed completely 
	 */
	public boolean isProcessed( String id) {
		Map<String, ContainerProcessingExpectation> scopeMap = scopes.get(id);
		if (scopeMap == null) {
			return false;			
		}
		 
		for (Entry<String, ContainerProcessingExpectation> entry : scopeMap.entrySet()) {
			if (entry.getValue().isProcessed() == false)
				return false;
		}
		return true;
	}

	/**
	 * release a scope 
	 */
	public void release( String id) {
		scopes.remove(id);
		//log.debug("releasing processing scope [" + id + "]");
	}
	
	/**
	 * get all containers of a scope 
	 */
	public List<ArtifactContainer> getContainers(String id) {
		List<ArtifactContainer> result = new ArrayList<ArtifactContainer>();
		Map<String, ContainerProcessingExpectation> scopeMap = scopes.get(id);
		if (scopeMap == null)
			return result;
		for (ContainerProcessingExpectation expectation : scopeMap.values()) {
			result.add( expectation.getContainer());
		}
		return result;
	}
	
	/**
	 * check if a certain container is in an active set 	
	 */
	public boolean isBeingProcessed( String containerId) {
		for (Entry<String,Map<String,ContainerProcessingExpectation>> entry : scopes.entrySet()) {
			Map<String,ContainerProcessingExpectation> map = entry.getValue();
			for (ContainerProcessingExpectation expectation : map.values()) {
				if (expectation.getId().equalsIgnoreCase(containerId))
					return true;
			}
		}
		return false;
	}
	
	private String collate(Stack<String> s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.size(); i++) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append( s.get(i));
		}
		return sb.toString();
	}

	/**
	 * block auto build if this is the first scope in the registry 
	 * @param scopeId - the id of the scope
	 */
	public synchronized void inhibitAutoBuild(String scopeId) {	
		if (autoBuildBlockingProcessingScopes.isEmpty()) {			
			log.debug( "deactivated auto-build for processing scope [" + scopeId + "], stored flag [" + initialValue + "]");
			initialValue = switchJdtAutoBuild(false);
		}
		else {
			log.debug( "already deactivated auto-build, no action for processing scope [" + scopeId + "], active are [" + collate(autoBuildBlockingProcessingScopes)  + "]");
			switchJdtAutoBuild(false);
		}
		autoBuildBlockingProcessingScopes.push(scopeId);
	}


	/**
	 * restore auto build if this is the last scope in the registry 
	 * @param scopeId - the id of the scope
	 */
	public synchronized void releaseAutoBuild(String scopeId) {
		autoBuildBlockingProcessingScopes.pop();

		if (autoBuildBlockingProcessingScopes.isEmpty()) {
			switchJdtAutoBuild( initialValue);
			log.debug( "restoring auto-build for processing scope [" + scopeId + "], restored flag [" + initialValue + "]");
		}
		else {
			log.debug( "still a processing scope requires deactivated auto-build, no action for processing scope [" + scopeId + "], active are [" + collate(autoBuildBlockingProcessingScopes)  + "]");						
		}
	}
		
	private boolean switchJdtAutoBuild( boolean enable) {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IWorkspaceDescription desc= workspace.getDescription();
		boolean isAutoBuilding= desc.isAutoBuilding();
		if (isAutoBuilding != enable) {
			desc.setAutoBuilding(enable);
			try {
				workspace.setDescription(desc);
			} catch (CoreException e) {
				String emsg="cannot set auto build feature";								
				ArtifactContainerStatus status = new ArtifactContainerStatus(emsg, e);
				ArtifactContainerPlugin.getInstance().log(status);
			}
		}
		return isAutoBuilding;
	}
}
