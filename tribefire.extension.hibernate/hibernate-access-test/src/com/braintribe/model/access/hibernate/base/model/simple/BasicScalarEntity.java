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
package com.braintribe.model.access.hibernate.base.model.simple;

import java.math.BigDecimal;
import java.util.Date;

import com.braintribe.model.access.hibernate.base.model.HibernateAccessEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface BasicScalarEntity extends HibernateAccessEntity {

	EntityType<BasicScalarEntity> T = EntityTypes.T(BasicScalarEntity.class);

	String stringValue = "stringValue";
	String integerValue = "integerValue";
	String longValue = "longValue";
	String floatValue = "floatValue";
	String doubleValue = "doubleValue";
	String booleanValue = "booleanValue";
	String dateValue = "dateValue";
	String decimalValue = "decimalValue";
	String color = "color";

	String getStringValue();
	void setStringValue(String stringValue);

	Integer getIntegerValue();
	void setIntegerValue(Integer integerValue);

	Long getLongValue();
	void setLongValue(Long longValue);

	Float getFloatValue();
	void setFloatValue(Float floatValue);

	Double getDoubleValue();
	void setDoubleValue(Double doubleValue);

	Boolean getBooleanValue();
	void setBooleanValue(Boolean booleanValue);

	Date getDateValue();
	void setDateValue(Date dateValue);

	BigDecimal getDecimalValue();
	void setDecimalValue(BigDecimal decimalValue);

	BasicColor getColor();
	void setColor(BasicColor color);

}
