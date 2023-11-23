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
package com.braintribe.model.elasticsearchreflection.nodeinformation;

import java.util.Map;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface DiscoveryNode extends StandardIdentifiable {

	final EntityType<DiscoveryNode> T = EntityTypes.T(DiscoveryNode.class);

	TransportAddress getAddress();
	void setAddress(TransportAddress address);

	Map<String, String> getAttributes();
	void setAttributes(Map<String, String> attributes);

	String getHostAddress();
	void setHostAddress(String hostAddress);

	String getHostName();
	void setHostName(String hostName);

	String getDiscoveryNodeId();
	void setDiscoveryNodeId(String discoveryNodeId);

	String getName();
	void setName(String name);

	String getVersion();
	void setVersion(String version);

}
