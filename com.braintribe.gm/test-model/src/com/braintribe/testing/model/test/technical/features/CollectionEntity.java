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
package com.braintribe.testing.model.test.technical.features;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An entity with various collection type properties.
 *
 * @author michael.lafite
 */

public interface CollectionEntity extends GenericEntity {

	EntityType<CollectionEntity> T = EntityTypes.T(CollectionEntity.class);

	List<String> getStringList();
	void setStringList(List<String> stringList);

	List<Integer> getIntegerList();
	void setIntegerList(List<Integer> integerList);

	List<BigDecimal> getDecimalList();
	void setDecimalList(List<BigDecimal> decimalList);

	List<SimpleEntity> getSimpleEntityList();
	void setSimpleEntityList(List<SimpleEntity> simpleEntityList);

	Set<String> getStringSet();
	void setStringSet(Set<String> stringSet);

	Set<Integer> getIntegerSet();
	void setIntegerSet(Set<Integer> integerSet);

	Set<BigDecimal> getDecimalSet();
	void setDecimalSet(Set<BigDecimal> decimalSet);

	Set<SimpleEntity> getSimpleEntitySet();
	void setSimpleEntitySet(Set<SimpleEntity> simpleEntitySet);

	Map<String, String> getStringToStringMap();
	void setStringToStringMap(Map<String, String> stringToStringMap);

	Map<Integer, Integer> getIntegerToIntegerMap();
	void setIntegerToIntegerMap(Map<Integer, Integer> integerToIntegerMap);

	Map<String, SimpleEntity> getStringToSimpleEntityMap();
	void setStringToSimpleEntityMap(Map<String, SimpleEntity> stringToSimpleEntityMap);

	Map<SimpleEntity, String> getSimpleEntityToStringMap();
	void setSimpleEntityToStringMap(Map<SimpleEntity, String> simpleEntityToStringMap);

	Map<SimpleEntity, ComplexEntity> getSimpleEntityToComplexEntityMap();
	void setSimpleEntityToComplexEntityMap(Map<SimpleEntity, ComplexEntity> simpleEntityToComplexEntityMap);

}
