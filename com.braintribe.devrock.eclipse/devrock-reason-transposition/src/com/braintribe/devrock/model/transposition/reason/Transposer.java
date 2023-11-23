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
package com.braintribe.devrock.model.transposition.reason;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.devrock.eclipse.model.resolution.nodes.ReasonNode;
import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.devrock.model.mc.reason.McReason;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

public class Transposer {

	public ReasonNode transpose( Origination origination) {
		return transpose(origination, true);
	}
	
	public ReasonNode transpose( Reason reason) {
		return transpose(reason, false);
	}

	private ReasonNode transpose( Reason reason, boolean asOrigination) {
		ReasonNode node = ReasonNode.T.create();
		
		node.setBackingReason(reason);
		EntityType<GenericEntity> entityType = reason.entityType();
		node.setType( entityType.getShortName());
		node.setText( reason.getText());
		
		Set<String> standardReasonProperties = null;
		if (asOrigination) {
			standardReasonProperties = Origination.T.getProperties().stream().map( p -> p.getName()).collect(Collectors.toSet());
		}
		else {
			standardReasonProperties = McReason.T.getProperties().stream().map( p -> p.getName()).collect(Collectors.toSet());	
		}
		
		// properties
		List<Property> properties = entityType.getProperties();
		for (Property property : properties) {
			String name = property.getName();
			
			// filter any irrelevant - any standard properties of the base class is ignored
			if (standardReasonProperties.contains( name)) {
				continue;
			}
			// 
			Object value = property.get(reason);
			// transpose property value?? how?? toString()?					
		}
				
		// children 
		for (Reason child : reason.getReasons()) {
			node.getChildren().add( transpose(child, asOrigination));
		}
	
		
		return node;
	}
}
