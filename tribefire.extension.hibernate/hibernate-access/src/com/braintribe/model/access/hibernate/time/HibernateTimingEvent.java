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
package com.braintribe.model.access.hibernate.time;

import java.time.Duration;
import java.time.Instant;

import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

public class HibernateTimingEvent {

	private Duration duration;
	private String context;
	private Instant recordingTime;
	
	public HibernateTimingEvent(Duration duration, String context) {
		super();
		this.duration = duration;
		this.context = context;
		this.recordingTime = NanoClock.INSTANCE.instant();
	}
	
	@Override
	public String toString() {
		String recTime = DateTools.encode(recordingTime, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
		String durString = StringTools.prettyPrintDuration(duration, true, null);
		StringBuilder sb = new StringBuilder(recTime);
		sb.append(": ");
		sb.append(context);
		sb.append(" (duration: ");
		sb.append(durString);
		sb.append(")");
		return sb.toString();
	}
	
}
