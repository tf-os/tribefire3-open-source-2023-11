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
package com.braintribe.gwt.gme.notification.client;

import com.braintribe.model.command.Command;
import com.braintribe.model.generic.reflection.ScalarType;

public class CommandModel {
	
	private String id;
	private String name;
	
	public CommandModel(Command command) {
		Object idObject = command.getId();
		
		if (idObject == null) {
			idObject = (long) command.hashCode();
			command.setId(idObject);
		}
		
		ScalarType type = (ScalarType) command.entityType().getIdProperty().getType().getActualType(idObject);
		id = type.instanceToString(idObject);
		
		name = command.getName();
		if (name == null)
			name = command.entityType().getShortName();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
