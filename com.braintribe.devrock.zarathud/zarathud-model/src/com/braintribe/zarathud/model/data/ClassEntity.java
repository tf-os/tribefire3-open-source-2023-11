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
import com.braintribe.zarathud.model.data.natures.HasAbstractNature;
import com.braintribe.zarathud.model.data.natures.HasAccessModifierNature;
import com.braintribe.zarathud.model.data.natures.HasStaticNature;
import com.braintribe.zarathud.model.data.natures.HasSynchronizedNature;


/**
 * represents a class
 * @author pit
 *
 */
public interface ClassEntity extends ClassOrInterfaceEntity, HasAccessModifierNature, HasStaticNature, HasSynchronizedNature, HasAbstractNature {
	
	final EntityType<ClassEntity> T = EntityTypes.T(ClassEntity.class);
	String superType = "superType";
	String subTypes = "subTypes";
	String implementedInterfaces = "implementedInterfaces";

	/**
	 * @return - single super type, a {@link ClassEntity} of course
	 */
	TypeReferenceEntity getSuperType();
	void setSuperType( TypeReferenceEntity supertype);
	
	/**
	 * @return - all deriving types as a {@link Set} of {@link ClassEntity}
	 */
	Set<ClassEntity> getSubTypes();
	void setSubTypes( Set<ClassEntity> subTypes);
	
	/**
	 * @return - a {@link Set} all {@link InterfaceEntity} the class implements
	 */
	Set<TypeReferenceEntity> getImplementedInterfaces();
	void setImplementedInterfaces( Set<TypeReferenceEntity> entries);
		
	
	 
}
