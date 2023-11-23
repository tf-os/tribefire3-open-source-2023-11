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
package com.braintribe.model.processing.query.smart.test2.basic.model.smart;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This type is mapped, but has a property that is not mapped - which should be treated as if it was always null.
 * 
 * @author peter.gazdik
 */
public interface SmartSimplePropertiesEntityA extends GenericEntity {

	String smartString = "smartString";
	String smartDate = "smartDate";
	
	EntityType<SmartSimplePropertiesEntityA> T = EntityTypes.T(SmartSimplePropertiesEntityA.class);

	String getSmartString();
	void setSmartString(String smartString);

	Date getSmartDate();
	void setSmartDate(Date smartDate);

}
