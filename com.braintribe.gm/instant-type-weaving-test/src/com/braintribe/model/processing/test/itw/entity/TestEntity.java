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
package com.braintribe.model.processing.test.itw.entity;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


@SelectiveInformation("Hi ${#type} ${double} ${#id} ${#runtimeId} ${N/A} Low2")
@ToStringInformation("Hi ${#type_short} ${double}  ${N/A} Low2")
public interface TestEntity extends GenericEntity {

	EntityType<TestEntity> T = EntityTypes.T(TestEntity.class);

	int getAge();
	void setAge(int s);
	
	double getDouble();
	void setDouble(double d);
	
	Integer getAgeO();
	void setAgeO(Integer s);
	
	AnotherTestEntity getAnotherEntity();
	void setAnotherEntity(AnotherTestEntity ate);
	
	Set<String> getStrings();
	void setStrings(Set<String> s);

}
