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
package com.braintribe.model.zarathud.data;

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface MethodEntity extends GenericEntity{
	
	final EntityType<MethodEntity> T = EntityTypes.T(MethodEntity.class);

	String getName();
	void setName( String name);
	
	String getSignature();
	void setSignature( String signature);
	
	String getDesc();
	void setDesc( String desc);
	
	Set<ClassEntity> getExceptions();
	void setExceptions( Set<ClassEntity> exceptions);
	
	AccessModifier getAccessModifier();
	void setAccessModifier( AccessModifier modifier);
	
	boolean getAbstractNature();
	void setAbstractNature( boolean value);
	
	boolean getStaticNature();
	void setStaticNature( boolean value);
	
	boolean getSynchronizedNature();
	void setSynchronizedNature( boolean value);
	
	Set<AnnotationEntity> getAnnotations();
	void setAnnotations( Set<AnnotationEntity> value);
	
	AbstractClassEntity getOwner();
	void setOwner( AbstractClassEntity owner);
	
	AbstractEntity getReturnType();
	void setReturnType( AbstractEntity desc);	
	
	List<AbstractEntity> getArgumentTypes();
	void setArgumentTypes( List<AbstractEntity> types);
			
}
