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
package com.braintribe.model.example;

import java.util.Date;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${longProperty} ${stringProperty}")

public interface Omega extends StandardIdentifiable {

	EntityType<Omega> T = EntityTypes.T(Omega.class);

	String longProperty = "longProperty";
	String stringProperty = "stringProperty";
	String integerProperty = "integerProperty";
	String doubleProperty = "doubleProperty";
	String floatProperty = "floatProperty";
	String dateProperty = "dateProperty";
	String booleanProperty = "booleanProperty";
	String enumProperty = "enumProperty";
	String entityProperty = "entityProperty";

	String setOfLongProperty = "setOfLongProperty";
	String setOfStringProperty = "setOfStringProperty";
	String setOfIntegerProperty = "setOfIntegerProperty";
	String setOfDoubleProperty = "setOfDoubleProperty";
	String setOfFloatProperty = "setOfFloatProperty";
	String setOfDateProperty = "setOfDateProperty";
	String setOfBooleanProperty = "setOfBooleanProperty";
	String setOfEnumProperty = "setOfEnumProperty";
	String setOfEntityProperty = "setOfEntityProperty";

	Long getLongProperty();
	void setLongProperty(Long longProperty);

	Set<Long> getSetOfLongProperty();
	void setSetOfLongProperty(Set<Long> setOfLongProperty);

	String getStringProperty();
	void setStringProperty(String stringProperty);

	Set<String> getSetOfStringProperty();
	void setSetOfStringProperty(Set<String> setOfStringProperty);

	Integer getIntegerProperty();
	void setIntegerProperty(Integer integerProperty);

	Set<Integer> getSetOfIntegerProperty();
	void setSetOfIntegerProperty(Set<Integer> setOfIntegerProperty);

	Double getDoubleProperty();
	void setDoubleProperty(Double doubleProperty);

	Set<Double> getSetOfDoubleProperty();
	void setSetOfDoubleProperty(Set<Double> setOfDoubleProperty);

	Float getFloatProperty();
	void setFloatProperty(Float floatProperty);

	Set<Float> getSetOfFloatProperty();
	void setSetOfFloatProperty(Set<Float> setOfFloatProperty);

	Date getDateProperty();
	void setDateProperty(Date dateProperty);

	Set<Date> getSetOfDateProperty();
	void setSetOfDateProperty(Set<Date> setOfDateProperty);

	Boolean getBooleanProperty();
	void setBooleanProperty(Boolean booleanProperty);

	Set<Boolean> getSetOfBooleanProperty();
	void setSetOfBooleanProperty(Set<Boolean> setOfBooleanProperty);

	OmegaEnum getEnumProperty();
	void setEnumProperty(OmegaEnum enumProperty);

	Set<OmegaEnum> getSetOfEnumProperty();
	void setSetOfEnumProperty(Set<OmegaEnum> setOfEnumProperty);

	Alpha getEntityProperty();
	void setEntityProperty(Alpha entityProperty);

	Set<Alpha> getSetOfEntityProperty();
	void setSetOfEntityProperty(Set<Alpha> setOfEntityProperty);

}
