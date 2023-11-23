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
package com.braintribe.model.processing.itw.analysis.protomodel;

import com.braintribe.model.generic.pseudo.GenericEntity_pseudo;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.restriction.ProtoGmTypeRestriction;

/**
 * Pseudo-implementation of {@link GmProperty}
 * 
 * @see GenericEntity_pseudo
 * 
 * @author peter.gazdik
 */
public class ProtoGmPropertyImpl extends ProtoGmPropertyInfoImpl implements ProtoGmProperty {

	private ProtoGmType type;
	private ProtoGmTypeRestriction typeRestriction;
	private String name;
	private boolean nullable = true; // Later this should be replaced with default-value annotation (once supported)
	private ProtoGmEntityType entityType;

	@Override
	public ProtoGmType getType() {
		return type;
	}

	public void setType(ProtoGmType type) {
		this.type = type;
	}

	@Override
	public ProtoGmTypeRestriction getTypeRestriction() {
		return typeRestriction;
	}

	public void setTypeRestriction(ProtoGmTypeRestriction typeRestriction) {
		this.typeRestriction = typeRestriction;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean getNullable() {
		return nullable;
	}

	@Override
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	@Override
	public ProtoGmEntityType getDeclaringType() {
		return entityType;
	}

	public void setDeclaringType(ProtoGmEntityType entityType) {
		this.entityType = entityType;
	}

}
