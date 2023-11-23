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

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.natures.HasGenericNature;


/**
 * represents an interface 
 * @author pit
 *
 */
public interface InterfaceEntity extends ClassOrInterfaceEntity, HasGenericNature {
	
	final EntityType<InterfaceEntity> T = EntityTypes.T(InterfaceEntity.class);
	String superInterfaces = "superInterfaces";
	String subInterfaces = "subInterfaces";
	String implementingClasses = "implementingClasses";

	/**
	 * @return - a {@link Set} of {@link InterfaceEntity} it extends 
	 */
	Set<TypeReferenceEntity> getSuperInterfaces();
	void setSuperInterfaces( Set<TypeReferenceEntity> entries);
	
	/**
	 * @return - a {@link Set} of {@link InterfaceEntity} that derive from it
	 */
	Set<InterfaceEntity> getSubInterfaces();
	void setSubInterfaces( Set<InterfaceEntity> subInterfaces);
	
	/**
	 * @return - a {@link Set} of {@link ClassEntity} that implement it
	 */
	Set<ClassEntity> getImplementingClasses();
	void setImplementingClasses( Set<ClassEntity> implementingClasses);
}
