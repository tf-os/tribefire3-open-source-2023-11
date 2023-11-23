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
package com.braintribe.gwt.validationui.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;

public class EntityEntry {
	
	private static Long lastId = 0l;
	
	public GenericEntity entity;
	public boolean persistent;
	public String entityTypeShortName;
	public String entitySelectiveInformation;
	public List<ResultEntry> results;
	public Long id;
	private List<Manipulation> listManipulation = new ArrayList<>();
	
	public EntityEntry(GenericEntity entity, boolean persistent, String entityTypeShortName, String entitySelectiveInformation, List<ResultEntry> results) {
		this.entity = entity;
		this.persistent = persistent;
		this.entityTypeShortName = entityTypeShortName;
		this.entitySelectiveInformation = entitySelectiveInformation;
		this.results = results;
		id = lastId++;
	}

	public List<Manipulation> getListManipulation() {
		return listManipulation;
	}
}
