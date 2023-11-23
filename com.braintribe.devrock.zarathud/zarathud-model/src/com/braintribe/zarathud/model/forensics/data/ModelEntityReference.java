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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * represents a generic entity within the analysis
 * @author pit
 *
 */
public interface ModelEntityReference extends GenericEntity {
	
	EntityType<ModelEntityReference> T = EntityTypes.T(ModelEntityReference.class);
	
	String type = "type";
	String modelPropertyReference = "modelPropertyReference";
	
	Artifact getArtifact();
	void setArtifact( Artifact artifact);
	
	ZedEntity getType();
	void setType(ZedEntity type);
	
	List<ModelPropertyReference> getPropertyReferences();
	void setPropertyReferences( List<ModelPropertyReference> propertyReferences);
	
	List<MethodEntity> getNonConformOtherMethods();
	void setNonConformOtherMethods( List<MethodEntity> nonConformMethods);
	
	List<MethodEntity> getConformOtherMethods();
	void setConformOtherMethods( List<MethodEntity> conformMethods);
	
	FieldEntity getEntityTypesDeclaration();
	void setEntityTypesDeclaration(FieldEntity value);
}