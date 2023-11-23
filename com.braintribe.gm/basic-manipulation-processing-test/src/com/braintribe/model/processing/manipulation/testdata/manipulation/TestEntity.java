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
package com.braintribe.model.processing.manipulation.testdata.manipulation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.StandardIntegerIdentifiable;

import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ToStringInformation(value = "${name}")
public interface TestEntity extends StandardIntegerIdentifiable, HasName {

	EntityType<TestEntity> T = EntityTypes.T(TestEntity.class);

	String getProperty1();
	void setProperty1(String property1);

	String getProperty2();
	void setProperty2(String property2);

	TestEntity getParentEntity();
	void setParentEntity(TestEntity parentEntity);

	Object getObjectProperty();
	void setObjectProperty(Object objectProperty);

	// ###################################################
	// ## . . . . . . Collection properties . . . . . . ##
	// ###################################################

	Set<TestEntity> getSomeSet();
	void setSomeSet(Set<TestEntity> someSet);

	List<TestEntity> getSomeList();
	void setSomeList(List<TestEntity> someList);

	Map<TestEntity, TestEntity> getSomeMap();
	void setSomeMap(Map<TestEntity, TestEntity> someMap);

	Set<Integer> getIntSet();
	void setIntSet(Set<Integer> intSet);

	List<Integer> getIntList();
	void setIntList(List<Integer> intList);

	Map<Integer, Integer> getIntMap();
	void setIntMap(Map<Integer, Integer> intMap);

}
