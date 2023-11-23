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
package com.braintribe.web.servlet.deploymentreflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.utils.xml.XmlTools;

public class DeploymentReflectionVelocityTools {
	
	private Set<String> nodeIds;
	private Set<String> cartridgeIds;
	private LiveInstances liveInstances;
	private Map<String,List<String>> nodeFamiliesToCartridges;

	public DeploymentReflectionVelocityTools(LiveInstances liveInstances) {
		this.liveInstances=liveInstances;
		
		nodeFamiliesToCartridges = getNodeFamiliesToCartridges();
	}

	public void setNodeIds(Set<String> nodeIds) {
		this.nodeIds = nodeIds;
	}

	public Set<String> getNodeIds() {
		return nodeIds;
	}
	
	public void setCartridgeIds(Set<String> cartridgeIds) {
		this.cartridgeIds = cartridgeIds;
	}
	
	public Set<String> getCartridgeIds() {
		return cartridgeIds;
	}

	public boolean isHardwired(Deployable d) {
		return (d instanceof HardwiredDeployable);
	}

	public String getSimpleName(String typeSignature) {
		String[] parts = typeSignature.split("\\.");
		return parts[parts.length - 1];
	}

	public String getPackageName(String typeSignature) {
		String[] parts = typeSignature.split("\\.");
		String[] reduced = Arrays.copyOf(parts, parts.length - 1);
		return String.join(".", reduced);
	}

	public String escape(String text) {
		return XmlTools.escape(text);
	}

	public boolean cartridgeDeployedOnNode(String nodeId, String cartridgeId) {
		List<String> cartridgeIds = nodeFamiliesToCartridges.get(nodeId);
		return cartridgeIds != null && cartridgeIds.contains(cartridgeId);
	}
	
	/**
	 * Filters {@link LiveInstances} to classify nodes on its node type and maps the occuring cartridges to it.<br/>
	 * A node type bundles nodes which follow a certain name pattern. Example:
	 * <ul>
	 * <li>NodeA#1</li>
	 * <li>NodeA#2</li>
	 * <li>NodeB#1</li>
	 * </ul>
	 * defines two node types: NodeA and NodeB.
	 * 
	 * @return A {@link Map} bundling node types to cartridges
	 */
	public Map<String,List<String>> getNodeFamiliesToCartridges() {
		Set<String> instances = liveInstances.liveInstances();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		instances.stream().forEach(i -> {
			String[] split = i.split("@");
			String nodeType = split[1];
			
			List<String> cartridgeIds = map.get(nodeType);
			if (cartridgeIds == null) {
				cartridgeIds = new ArrayList<String>();
				map.put(nodeType, cartridgeIds);
			}
			cartridgeIds.add(split[0]);
		});
		
		return map;
	}
}
