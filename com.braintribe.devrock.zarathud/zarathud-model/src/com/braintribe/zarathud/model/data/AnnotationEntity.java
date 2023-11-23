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
package com.braintribe.zarathud.model.data;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents an annotation - actually a container 
 * @author pit
 *
 */
public interface AnnotationEntity extends ZedEntity {
	
	final EntityType<AnnotationEntity> T = EntityTypes.T(AnnotationEntity.class);
	
	String declaringInterface = "declaringInterface";
	String members = "members";
	String owner = "owner";

	/**
	 * @return - the actual defining interface of the annotation
	 */
	TypeReferenceEntity getDeclaringInterface();
	void setDeclaringInterface( TypeReferenceEntity interfaceEntity);
	
	/**
	 * access to the annotation's declarations
	 * @return - map of name to {@link AnnotationValueContainer}
	 */
	Map<String, AnnotationValueContainer> getMembers();
	void setMembers( Map<String, AnnotationValueContainer> values);
	
	GenericEntity getOwner();
	void setOwner(GenericEntity value);

}
