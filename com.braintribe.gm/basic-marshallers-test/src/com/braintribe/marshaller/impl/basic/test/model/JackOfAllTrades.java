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
package com.braintribe.marshaller.impl.basic.test.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface JackOfAllTrades extends StandardIdentifiable {

	EntityType<JackOfAllTrades> T = EntityTypes.T(JackOfAllTrades.class);

	List<Object> getObjectList();
	void setObjectList(List<Object> objectList);

	Object getObject();
	void setObject(Object object);

	// test Object property with empty List value
	Object getObjectL();
	void setObjectL(Object objectL);
	
	// test Object property with empty Set value
	Object getObjectS();
	void setObjectS(Object objectS);
	
	// test Object property with empty Map value
	Object getObjectM();
	void setObjectM(Object objectM);
	
	Date getDateValue();
	void setDateValue(Date dateValue);

	Mode getMode();
	void setMode(Mode mode);

	JackOfAllTrades getOther();
	void setOther(JackOfAllTrades otherExample);

	String getStringValue();
	void setStringValue(String stringValue);

	Boolean getBooleanValue();
	void setBooleanValue(Boolean booleanValue);

	Integer getIntegerValue();
	void setIntegerValue(Integer integerValue);

	Long getLongValue();
	void setLongValue(Long longValue);

	Float getFloatValue();
	void setFloatValue(Float floatValue);

	Double getDoubleValue();
	void setDoubleValue(Double doubleValue);

	boolean getPrimitiveBooleanValue();
	void setPrimitiveBooleanValue(boolean primitiveBooleanValue);

	int getPrimitiveIntegerValue();
	void setPrimitiveIntegerValue(int primitiveIntegerValue);

	long getPrimitiveLongValue();
	void setPrimitiveLongValue(long primitiveLongValue);

	float getPrimitiveFloatValue();
	void setPrimitiveFloatValue(float primitiveFloatValue);

	double getPrimitiveDoubleValue();
	void setPrimitiveDoubleValue(double primitiveDoubleValue);

	BigDecimal getDecimalValue();
	void setDecimalValue(BigDecimal decimalValue);

	List<String> getStringList();
	void setStringList(List<String> stringList);

	List<JackOfAllTrades> getEntityList();
	void setEntityList(List<JackOfAllTrades> entityList);

	Set<String> getStringSet();
	void setStringSet(Set<String> stringSet);

	Set<JackOfAllTrades> getEntitySet();
	void setEntitySet(Set<JackOfAllTrades> entitySet);

	Map<String, String> getStringStringMap();
	void setStringStringMap(Map<String, String> stringStringMap);

	Map<JackOfAllTrades, JackOfAllTrades> getEntityEntityMap();
	void setEntityEntityMap(Map<JackOfAllTrades, JackOfAllTrades> entityEntityMap);

}
