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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

import com.braintribe.model.meta.data.constraint.DateClipping;

public class DateZoneMapper extends DateClipper implements Function<Date, Date> {
	public DateZoneMapper(String zoneId, DateClipping dateClipping) {
		super(dateClipping, ZoneId.of(zoneId));
	}
	
	@Override
	public Date apply(Date d) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
		return clip(Date.from(localDateTime.atZone(getZoneId()).toInstant()));
	}
}
