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
package com.braintribe.model.graphlayout;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.descriptive.HasLocalizedDescription;
import com.braintribe.model.descriptive.HasLocalizedName;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface GraphLayout extends GenericEntity, HasLocalizedName, HasLocalizedDescription{

	EntityType<GraphLayout> T = EntityTypes.T(GraphLayout.class);
	
	public void setElements(Set<GraphLayoutElement> elements);
	public Set<GraphLayoutElement> getElements();
	
	public void setIdentification(String identification);
	public String getIdentification();
	
	public void setDenotation(GenericEntity denotation);
	public GenericEntity getDenotation();
	
	default Stream<GraphLayoutElement> elements() {
		return (Stream<GraphLayoutElement>) (Object) getElements().stream().filter((element) -> {return element instanceof GraphLayoutElement;});
	}
	
	default Stream<Node> nodes() {
		return (Stream<Node>) (Object) getElements().stream().filter((element) -> {return element instanceof Node;});
	}
	
	default Set<Node> nodesSet() {
		return nodes().collect(Collectors.toSet());
	}
	
	default Stream<Edge> edges() {
		return (Stream<Edge>) (Object) getElements().stream().filter((element) -> {return element instanceof Edge;});
	}
	
	default Set<Edge> edgesSet() {
		return edges().collect(Collectors.toSet());
	}

}
