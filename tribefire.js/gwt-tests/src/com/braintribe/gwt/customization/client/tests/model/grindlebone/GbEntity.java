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
package com.braintribe.gwt.customization.client.tests.model.grindlebone;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface GbEntity extends GenericEntity {
	
	final EntityType<GbEntity> T = EntityTypes.T(GbEntity.class);
	
	Integer getIntegerWrapper();
	void setIntegerWrapper(Integer value);
	
	Long getLongWrapper();
	void setLongWrapper(Long value);
	
	Double getDoubleWrapper();
	void setDoubleWrapper(Double value);
	
	Float getFloatWrapper();
	void setFloatWrapper(Float value);
	
	Boolean getBooleanWrapper();
	void setBooleanWrapper(Boolean value);
	
	boolean getBooleanValue();
	void setBooleanValue(boolean value);
	
	int getIntegerValue();
	void setIntegerValue(int value);
	
	long getLongValue();
	void setLongValue(long value);
	
	double getDoubleValue();
	void setDoubleValue(double value);
	
	float getFloatValue();
	void setFloatValue(float value);
	
	String getStringValue();
	void setStringValue(String value);
}
