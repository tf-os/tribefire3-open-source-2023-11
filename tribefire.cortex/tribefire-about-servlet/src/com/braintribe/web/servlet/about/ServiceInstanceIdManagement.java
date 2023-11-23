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
package com.braintribe.web.servlet.about;

import static com.braintribe.web.servlet.about.ParameterTools.getSingleParameterAsString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools2;

public class ServiceInstanceIdManagement {

	protected InstanceId localInstanceId;
	protected LiveInstances liveInstances;

	public ServiceInstanceIdManagement(InstanceId localInstanceId, LiveInstances liveInstances) {
		super();
		this.localInstanceId = localInstanceId;
		this.liveInstances = liveInstances;
	}

	public InstanceId getSelectedServiceInstance(HttpServletRequest req) throws ServletException {
		String param = getSingleParameterAsString(req, "node");
		if (StringTools.isBlank(param) || param.equals(AboutServlet.KEY_ALL_NODES)) {
			InstanceId iid = InstanceId.T.create();
			return iid;
		} else {
			param = param.trim();

			InstanceId candidate = null;
			for (String availableInstanceId : liveInstances.liveInstances()) {
				int i = availableInstanceId.indexOf('@');
				if (i != -1 && i > 0 && i < availableInstanceId.length()-1) {
					String availAppId = availableInstanceId.substring(0, i);
					String availNodeId = availableInstanceId.substring(i+1);
					if (availNodeId.equals(param)) {
						if (candidate == null || isMasterInstanceId(availableInstanceId)) {
							candidate = InstanceId.T.create();
							candidate.setNodeId(availNodeId);
							candidate.setApplicationId(availAppId);
						}
					}
				}
			}

			if (candidate != null) {
				return candidate;
			} else {
				throw new ServletException("Could not parse the node id: "+param);
			}
		}
	}


	public Collection<InstanceId> getSelectedApplicationInstanceIds(HttpServletRequest req) {
		String param = getSingleParameterAsString(req, "node");
		if (StringTools.isBlank(param) || param.equals(AboutServlet.KEY_ALL_NODES)) {

			Set<String> liveInstancesSet = liveInstances.liveInstances();
			List<InstanceId> instanceIds = new ArrayList<>(liveInstancesSet.size());

			Set<String> visitedNodes = new HashSet<>();

			for (String availableInstanceId : liveInstancesSet) {
				int i = availableInstanceId.indexOf('@');
				if (i != -1 && i > 0 && i < availableInstanceId.length()-1) {
					String availAppId = availableInstanceId.substring(0, i);
					String availNodeId = availableInstanceId.substring(i+1);

					if (!visitedNodes.contains(availNodeId)) {
						InstanceId candidate = InstanceId.T.create();
						candidate.setNodeId(availNodeId);
						candidate.setApplicationId(availAppId);

						instanceIds.add(candidate);

						visitedNodes.add(availNodeId);
					}
				}
			}

			return instanceIds;
		} else {
			param = param.trim();

			List<InstanceId> instanceIds = new ArrayList<>();

			InstanceId candidate = InstanceId.T.create();
			candidate.setNodeId(param);
			
			instanceIds.add(candidate);
			
			return instanceIds;
		}
	}


	public Collection<InstanceId> getSelectedServiceInstances(HttpServletRequest req) throws ServletException {
		String param = getSingleParameterAsString(req, "node");
		if (StringTools.isBlank(param) || param.equals(AboutServlet.KEY_ALL_NODES)) {

			Map<String,InstanceId> instancesPerNode = new TreeMap<>();
			for (String availableInstanceId : liveInstances.liveInstances()) {
				int i = availableInstanceId.indexOf('@');
				if (i != -1 && i > 0 && i < availableInstanceId.length()-1) {
					String availAppId = availableInstanceId.substring(0, i);
					String availNodeId = availableInstanceId.substring(i+1);

					if (!instancesPerNode.containsKey(availNodeId) || isMasterInstanceId(availableInstanceId)) {
						InstanceId candidate = InstanceId.T.create();
						candidate.setNodeId(availNodeId);
						candidate.setApplicationId(availAppId);

						instancesPerNode.put(availNodeId, candidate);
					}
				}
			}

			return instancesPerNode.values();
		} else {
			param = param.trim();

			InstanceId candidate = null;
			for (String availableInstanceId : liveInstances.liveInstances()) {
				int i = availableInstanceId.indexOf('@');
				if (i != -1 && i > 0 && i < availableInstanceId.length()-1) {
					String availAppId = availableInstanceId.substring(0, i);
					String availNodeId = availableInstanceId.substring(i+1);
					if (availNodeId.equals(param)) {
						if (candidate == null || isMasterInstanceId(availableInstanceId)) {
							candidate = InstanceId.T.create();
							candidate.setNodeId(availNodeId);
							candidate.setApplicationId(availAppId);
						}
					}
				}
			}

			if (candidate != null) {
				return CollectionTools2.asSet(candidate);
			} else {
				throw new ServletException("Could not parse the node id: "+param);
			}
		}
	}

	public boolean isMasterInstanceId(String instance) {
		if (StringTools.isBlank(instance)) {
			return false;
		}
		return instance.startsWith("master@");
	}

	public Set<String> getNodes() {
		Set<String> result = new TreeSet<>();
		for (String instance : liveInstances.liveInstances()) {
			int i = instance.indexOf('@');
			if (i != -1 && i > 0 && i < instance.length()-1) {
				String nodeId = instance.substring(i+1);
				result.add(nodeId);
			}
		}
		return result;
	}

	public String getNodeFromInstanceId(InstanceId instanceId) {
		if (instanceId == null) {
			return null;
		}
		String instance = instanceId.toString();
		int i = instance.indexOf('@');
		if (i != -1 && i > 0 && i < instance.length()-1) {
			String nodeId = instance.substring(i+1);
			return nodeId;
		}
		return null;
	}

	public boolean isLocalServerInstance(InstanceId id) {
		if (id == null) {
			return false;
		}
		if (localInstanceId.getApplicationId().equals(id.getApplicationId()) &&
				localInstanceId.getNodeId().equals(id.getNodeId())) {
			return true;
		}
		return false;
	}

	public Set<String> getServiceInstances() {
		Set<String> result = new TreeSet<>();
		String servicesAppId = localInstanceId.getApplicationId();
		Set<String> allInstances = liveInstances.liveInstances();
		for (String instance : allInstances) {
			if (instance.startsWith(servicesAppId+"@")) {
				result.add(instance);
			}
		}
		return result;
	}

	public Set<String> getAllInstances() {
		return liveInstances.liveInstances();
	}
}
