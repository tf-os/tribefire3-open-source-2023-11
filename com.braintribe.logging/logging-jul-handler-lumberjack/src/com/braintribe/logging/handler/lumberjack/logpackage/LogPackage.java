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
package com.braintribe.logging.handler.lumberjack.logpackage;

import java.util.Map;

public class LogPackage {

	protected int sequenceNumber = 1;
	protected String line = null;
	protected Map<String, String> properties = null;

	public LogPackage(int sequenceNumber, String line, Map<String, String> properties) {
		this.sequenceNumber = sequenceNumber;
		this.line = line;
		this.properties = properties;
	}

	public String getLine() {
		return line;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void addProperty(String key, String value) {
		if (key != null && value != null) {
			this.properties.put(key, value);
		}
	}
}
