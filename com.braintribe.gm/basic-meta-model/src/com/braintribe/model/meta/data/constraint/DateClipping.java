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
package com.braintribe.model.meta.data.constraint;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.PropertyMetaData;
import com.braintribe.model.time.DateOffsetUnit;

public interface DateClipping extends PropertyMetaData {

	EntityType<DateClipping> T = EntityTypes.T(DateClipping.class);

	// @formatter:off
	void setLower(DateOffsetUnit lower); 
	DateOffsetUnit getLower();

	void setUpper(DateOffsetUnit upper); 
	DateOffsetUnit getUpper();
	
	void setDateSeparator(String dateSeparator);
	String getDateSeparator();
	
	void setTimeSeparator(String timeSeparator);
	String getTimeSeparator();
	// @formatter:on
	
}
