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
package com.braintribe.model.modellergraph.graphics;

import java.util.Map;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface ModelGraphState extends GraphElement {

	final EntityType<ModelGraphState> T = EntityTypes.T(ModelGraphState.class);

	// @formatter:off
	Map<String, Edge> getEdges();
	void setEdges(Map<String, Edge> edges);

	Map<String, Node> getNodes();
	void setNodes(Map<String, Node> nodes);
	
	boolean getHasMore();
	void setHasMore(boolean hasMore);
	
	boolean getHasLess();
	void setHasLess(boolean hasLess);

	// List<Node> getNodes();
	// void setNodes(List<Node> nodes);
	//
	// List<Edge> getEdges();
	// void setEdges(List<Edge> edges);
	// @formatter:on

}
