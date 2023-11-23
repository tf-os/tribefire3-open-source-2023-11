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
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;

/**
 * what the {@link ModelForensicsResult} contains about the properties of a model
 *  
 * @author pit
 *
 */
public interface ModelPropertyReference extends GenericEntity {
	
	EntityType<ModelPropertyReference> T = EntityTypes.T(ModelPropertyReference.class);
	
	String forensicsRating = "forensicsRating";
	String owner = "owner";
	String name = "name";
	String setter = "setter";
	String getter = "getter";
	String type = "type";
	String annotations = "annotations";
	String transientNature = "transientNature";
	String nameMember = "nameMember";

	
	/**
	 * @return - the owning generic entity (or {@link InterfaceEntity} here)
	 */
	ModelEntityReference getOwner();
	void setOwner(ModelEntityReference owner);
	
	/**
	 * @return - name of the property
	 */
	String getName();
	void setName(String name);
	
	/**
	 * @return - the string declared as property name, if any 
	 */
	FieldEntity getNameMember();
	void setNameMember(FieldEntity nameMember);
	
	/**
	 * @return - the method being the getter 
	 */
	MethodEntity getGetter();
	void setGetter(MethodEntity value);
	
	/**
	 * @return - the method being the setter 
	 */
	MethodEntity getSetter();
	void setSetter(MethodEntity value);
	
	/**
	 * @return - the {@link ZedEntity} standing in as type of the property
	 */
	ZedEntity getType();
	void setType( ZedEntity type);
		
	/**
	 * @return - the {@link AnnotationEntity} attached to the property (mostly getters)
	 */
	List<AnnotationEntity> getAnnotations();
	void setAnnotations( List<AnnotationEntity> annotations);
	
	/**
	 * @return - true if it has transient nature (i.e. the annotations contain a reference to the {@link Transient} annotation 
	 */
	boolean getTransientNature();
	void setTransientNature(boolean transientNature);
}
