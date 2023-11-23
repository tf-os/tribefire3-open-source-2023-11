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
package com.braintribe.zarathud.model.forensics.data;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * represents an enum within a model, aka a proper transportable element of a model
 * @author pit
 *
 */
public interface ModelEnumReference extends GenericEntity {
	
	EntityType<ModelEnumReference> T = EntityTypes.T(ModelEnumReference.class);
	
	String artifact = "artifact";
	String type = "type";
	String enumEntity = "enumEntity";
	String enumTypesDeclaration = "enumTypesDeclaration";

	/**
	 * @return
	 */
	Artifact getArtifact();
	void setArtifact( Artifact artifact);
	
	/**
	 * @return
	 */
	ZedEntity getType();
	void setType(ZedEntity type);
	
	/**
	 * @return
	 */
	EnumEntity getEnumEntity();
	void setEnumEntity(EnumEntity value);
 	
	/**
	 * @return
	 */
	FieldEntity getEnumTypesDeclaration();
	void setEnumTypesDeclaration(FieldEntity value);
}
