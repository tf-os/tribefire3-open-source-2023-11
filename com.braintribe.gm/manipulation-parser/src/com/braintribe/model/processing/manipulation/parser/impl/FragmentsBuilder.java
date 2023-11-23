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
package com.braintribe.model.processing.manipulation.parser.impl;

import com.braintribe.model.time.CalendarOffset;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.model.time.TimeZoneOffset;

/**
 * A convenient class that contains all the instantiations any Query related model
 */
public interface FragmentsBuilder {

	public static CalendarOffset dateOffset(Integer value, DateOffsetUnit unit) {
		DateOffset dateOffset = DateOffset.T.create();
		dateOffset.setOffset(unit);
		dateOffset.setValue(value);
		return dateOffset;
	}

	public static CalendarOffset timeZoneOffset(int minutes) {
		TimeZoneOffset timeZoneOffset = TimeZoneOffset.T.create();
		timeZoneOffset.setMinutes(minutes);
		return timeZoneOffset;
	}

}
