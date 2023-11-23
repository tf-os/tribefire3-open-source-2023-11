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
package com.braintribe.logging.juli.filters.logger;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class EnabledLogLevels {

	private int from = Level.FINEST.intValue();
	private int to = Level.SEVERE.intValue();

	public void setFrom(int from) {
		this.from = from;
	}
	public void setTo(int to) {
		this.to = to;
	}

	public void setFrom(String from) {
		if (from == null || from.trim().length() == 0) {
			return;
		}
		Level parsedLevel = Level.parse(from);
		this.from = parsedLevel.intValue();
	}
	public void setTo(String to) {
		if (to == null || to.trim().length() == 0) {
			return;
		}
		Level parsedLevel = Level.parse(to);
		this.to = parsedLevel.intValue();
	}

	public boolean enabled(LogRecord logRecord) {
		int level = logRecord.getLevel().intValue();
		return (this.from <= level && level <= this.to);
	}
}
