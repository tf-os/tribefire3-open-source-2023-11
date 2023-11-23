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

import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.manipulation.PropertyManipulation;

public class ResultEntry {
	
	public String name;
	public String desc;
	public String propertyName;
	public String message;
	public String note;
	public int size;
	private List<Manipulation> listManipulation = new ArrayList<>();
	
	public ResultEntry(String name, String desc, String message, String note, String propertyName, int size) {
		this.name = name;
		this.desc = desc;
		this.message = message;
		this.note = note;
		this.propertyName = propertyName;
		this.size = size;
	}

	public List<Manipulation> getListManipulation() {
		return listManipulation;
	}
	
	public String getOriginalValue() {
		String originalValue = null;
		
		for (Manipulation manipulation : listManipulation) {
			if (manipulation == null)
				continue;
			
			if (!(manipulation instanceof PropertyManipulation))
				continue;
			
			Owner owner = ((PropertyManipulation) manipulation).getOwner();
			
			if (owner == null)
				continue;
			
			if (!owner.getPropertyName().equals(propertyName))
				continue;
			
			Manipulation inverseManipulation = manipulation.getInverseManipulation(); 
			if (inverseManipulation != null && inverseManipulation instanceof ChangeValueManipulation) {
				originalValue = String.valueOf(((ChangeValueManipulation) inverseManipulation).getNewValue());				
				break;
			}
		}
		
		return originalValue;
	}
}
