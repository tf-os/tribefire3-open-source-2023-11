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
import java.util.Optional;

import com.braintribe.model.meta.data.constraint.DateClipping;
import com.braintribe.model.time.DateOffsetUnit;

public class DateClipper {
	
	private DateClipping dateClipping;
	private ZoneId zoneId;
	
	public DateClipper(DateClipping dateClipping, ZoneId zoneId) {
		this.dateClipping = dateClipping;
		this.zoneId = zoneId;
	}
	
	public ZoneId getZoneId() {
		return zoneId;
	}
	
	public Date clip(Date date) {
		if (dateClipping == null)
			return date;
		
		class Clipper {
			DateOffsetUnit upperBound = Optional.ofNullable(dateClipping.getUpper()).orElse(DateOffsetUnit.year);
			DateOffsetUnit lowerBound = Optional.ofNullable(dateClipping.getLower()).orElse(DateOffsetUnit.millisecond);
			
			int clip(DateOffsetUnit unit, int value, int clippedValue) {
				return (unit.ordinal() >= lowerBound.ordinal() && unit.ordinal() <= upperBound.ordinal())? value: clippedValue;
			}
		}
		
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), zoneId);
		
		Clipper clipper = new Clipper();
		
		LocalDateTime clippedLocalDateTime = LocalDateTime.of(
			clipper.clip(DateOffsetUnit.year, localDateTime.getYear(), 0),
			clipper.clip(DateOffsetUnit.month, localDateTime.getMonthValue(), 1),
			clipper.clip(DateOffsetUnit.day, localDateTime.getDayOfMonth(), 1),
			clipper.clip(DateOffsetUnit.hour, localDateTime.getHour(), 0),
			clipper.clip(DateOffsetUnit.minute, localDateTime.getMinute(), 0),
			clipper.clip(DateOffsetUnit.second, localDateTime.getSecond(), 0),
			clipper.clip(DateOffsetUnit.millisecond, localDateTime.getNano() / 1_000_000, 0) * 1_000_000
		);

		return Date.from(clippedLocalDateTime.atZone(zoneId).toInstant());
	}
}
