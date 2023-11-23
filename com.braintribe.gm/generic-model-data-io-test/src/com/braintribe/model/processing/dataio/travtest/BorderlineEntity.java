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
package com.braintribe.model.processing.dataio.travtest;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface BorderlineEntity extends SubBorderlineEntity {

	final EntityType<BorderlineEntity> T = EntityTypes.T(BorderlineEntity.class);

	String getPropertyX();
	void setPropertyX(String propertyX);

	String getPropertyY();
	void setPropertyY(String propertyY);

	Set<String> getSetProperty1();
	void setSetProperty1(Set<String> setProperty1);

	Set<String> getSetProperty2();
	void setSetProperty2(Set<String> setProperty2);
	
	String getAbsentProperty1();
	void setAbsentProperty1(String absentProperty1);

	String getAbsentProperty2();
	void setAbsentProperty2(String absentProperty2);

}
