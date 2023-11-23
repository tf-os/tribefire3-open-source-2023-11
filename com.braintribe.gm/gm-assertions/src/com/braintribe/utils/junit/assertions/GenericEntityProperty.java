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
package com.braintribe.utils.junit.assertions;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.genericmodel.GMCoreTools;

/**
 * Helper class that specifies an entity property by {@link GenericEntity} instance and property name.
 * 
 * @author michael.lafite
 */
public class GenericEntityProperty {
	private final GenericEntity entity;
	private final String name;
	private Object value;
	private boolean valueRetrieved;
	private boolean wasAbsent;

	public GenericEntityProperty(final GenericEntity entity, final String name) {
		this.entity = entity;
		this.name = name;
	}

	public GenericEntity getEntity() {
		return this.entity;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		if (!this.valueRetrieved) {
			this.wasAbsent = isAbsent();
			this.value = GMCoreTools.getPropertyValue(this.entity, this.name);
			this.valueRetrieved = true;
		}
		return this.value;
	}

	public boolean isAbsent() {
		return GMCoreTools.isAbsent(this.entity, this.name);
	}

	public boolean wasAbsent() {
		return this.wasAbsent;
	}

}
