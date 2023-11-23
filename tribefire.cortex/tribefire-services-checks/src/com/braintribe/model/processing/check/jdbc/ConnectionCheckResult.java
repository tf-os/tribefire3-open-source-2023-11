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
package com.braintribe.model.processing.check.jdbc;

public class ConnectionCheckResult {

	private boolean connectionValid = false;
	private String details;
	private long elapsedTime = -1;
	
	
	public ConnectionCheckResult(boolean connectionValid, long elpasedTime) {
		this.connectionValid = connectionValid;
		this.elapsedTime = elpasedTime;
	}

	public ConnectionCheckResult(boolean connectionValid, long elapsedTime, String details) {
		this(connectionValid, elapsedTime);
		this.details = details;
	}

	
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	public boolean isConnectionValid() {
		return connectionValid;
	}
	
	public String getDetails() {
		return details;
	}
	
	
}
