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
package com.braintribe.codec.marshaller.json.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface TestEntity extends GenericEntity {

static EntityType<TestEntity> T = EntityTypes.T(TestEntity.class);
	
	// primitive data types
	boolean getPrimitiveBooleanValue();
	void setPrimitiveBooleanValue(boolean booleanValue);
	
	int getIntValue();
	void setIntValue(int intValue);
	
	long getPrimitiveLongValue();
	void setPrimitiveLongValue(long longValue);
	
	float getPrimitiveFloatValue();
	void setPrimitiveFloatValue(float floatValue);
	
	double getPrimitiveDoubleValue();
	void setPrimitiveDoubleValue(double doubleValue);
	
	// object types
	Boolean getBooleanValue();
	void setBooleanValue(Boolean booleanValue);
	
	Integer getIntegerValue();
	void setIntegerValue(Integer intValue);
	
	Long getLongValue();
	void setLongValue(Long longValue);
	
	Float getFloatValue();
	void setFloatValue(Float floatValue);
	
	Double getDoubleValue();
	void setDoubleValue(Double doubleValue);
	
	String getStringValue();
	void setStringValue(String stringValue);
	
	
	// maps
	Map<String, String> getStringMap();
	void setStringMap(Map<String, String> stringMap);
	
	Map<String, Integer> getStringIntegerMap();
	void setStringIntegerMap(Map<String, Integer> stringIntegerMap);
	
	Map<String, Long> getStringLongMap();
	void setStringLongMap(Map<String, Long> stringLongMap);
	
	Map<String, Double> getStringDoubleMap();
	void setStringDoubleMap(Map<String, Double> stringDoubleMap);
	
	Map<String, Float> getStringFloatMap();
	void setStringFloatMap(Map<String, Float> stringFloatMap);
	
	Map<String, Object> getStringObjectMap();
	void setStringObjectMap(Map<String, Object> stringObjectMap);
	
	Map<Object, String> getObjectStringMap();
	void setObjectStringMap(Map<Object, String> objectStringMap);
	
	Map<Object, Object> getObjectObjectMap();
	void setObjectObjectMap(Map<Object, Object> objectObjectMap);
	
	Map<TestEnum, String> getEnumMap();
	void setEnumMap(Map<TestEnum, String> enumMap);

	// sets
	Set<String> getStringSet();
	void setStringSet(Set<String> stringSet);
	
	Set<Integer> getIntegerSet();
	void setIntegerSet(Set<Integer> integerSet);
	
	Set<Long> getLongSet();
	void setLongSet(Set<Long> longSet);
	
	Set<Double> getDoubleSet();
	void setDoubleSet(Set<Double> doubleSet);
	
	Set<Float> getFloatSet();
	void setFloatSet(Set<Float> floatSet);
	
	Set<Boolean> getBooleanSet();
	void setBooleanSet(Set<Boolean> booleanSet);
	
	Set<Object> getObjectSet();
	void setObjectSet(Set<Object> objectSet);
	
	//lists
	List<String> getStringList();
	void setStringList(List<String> stringList);

	List<Integer> getIntegerList();
	void setIntegerList(List<Integer> integerList);

	List<Long> getLongList();
	void setLongList(List<Long> longList);

	List<Double> getDoubleList();
	void setDoubleList(List<Double> doubleList);

	List<Float> getFloatList();
	void setFloatList(List<Float> floatList);

	List<Boolean> getBooleanList();
	void setBooleanList(List<Boolean> booleanList);

	List<Object> getObjectList();
	void setObjectList(List<Object> objectList);
	
	// numbers
	BigDecimal getDecimalValue();
	void setDecimalValue(BigDecimal decimalValue);
	
	
	// date 
	Date getDateValue();
	void setDateValue(Date stringValue);
	
	Set<Date> getDateSet();
	void setDateSet(Set<Date> dateSet);
	
	List<Date> getDateList();
	void setDateList(List<Date> dateList);
	
	Map<String, Date> getStringDateMap();
	void setStringDateMap(Map<String, Date> stringDateMap);
	
	// complex object
	TestEntity getEntityValue();
	void setEntityValue(TestEntity entity);
}
