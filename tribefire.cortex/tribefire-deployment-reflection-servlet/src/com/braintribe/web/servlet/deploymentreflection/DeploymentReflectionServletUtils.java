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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.model.deploymentreflection.request.WireKind;
import com.braintribe.utils.StringTools;

/**
 * Class providing utility methods used by the {@link DeploymentReflectionServlet}.
 * @author christina.wilpernig
 */
public class DeploymentReflectionServletUtils {

	protected LiveInstances liveInstances;

	public DeploymentReflectionServletUtils(LiveInstances liveInstances) {
		super();
		this.liveInstances = liveInstances;
	}

	public Set<String> getCartridgeIds() {
		Set<String> cartridgeIds = new TreeSet<>();
		liveInstances.liveInstances().stream().forEach(instance -> {
			String[] split = instance.split("@");
			cartridgeIds.add(split[0]);
		});
		return cartridgeIds;
	}

	public Set<String> getNodeIds() {
		Set<String> nodeIds = new TreeSet<>();
		liveInstances.liveInstances().stream().forEach(instance -> {
			String[] split = instance.split("@");
			nodeIds.add(split[1]);
		});
		return nodeIds;
	}

	public List<String> getWireKinds() {
		List<String> wireKinds = new ArrayList<String>();
		for (WireKind k : WireKind.values()) {
			wireKinds.add(k.toString());
		}
		return wireKinds;
	}

	public static Boolean getSingleParameterAsBoolean(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return Boolean.parseBoolean(values[0]);
	}

	public static String getSingleParameterAsString(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	
	public static String getParameterMapAsString(HttpServletRequest req) {
		StringBuilder result = new StringBuilder();
		Map<String, String[]> parameterMap = req.getParameterMap();
		if (parameterMap != null) {
			boolean first = true;
			for (Map.Entry<String,String[]> entry : parameterMap.entrySet()) {
				String key = entry.getKey();
				String[] values = entry.getValue();
				String valuesAsString = StringTools.createStringFromArray(values);
				if (!first) {
					result.append(", ");
				} else {
					first = false;
				}
				result.append(key != null ? key : "null");
				result.append("=");
				result.append(valuesAsString != null ? valuesAsString : "null");
			}
		}
		return result.toString();
	}

}
