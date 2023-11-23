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
package com.braintribe.model.processing.securityservice.basic.test.common;

public class TestConfig {
	
	private Boolean generatingUserSessionAccesses = true;
	private Boolean enableLongRunning = true;
	private Boolean enableExpiration = true;

	public Boolean getGeneratingUserSessionAccesses() {
		return generatingUserSessionAccesses;
	}
	
	public void setGeneratingUserSessionAccesses(Boolean generatingUserSessionAccesses) {
		this.generatingUserSessionAccesses = generatingUserSessionAccesses;
	}

	public Boolean getEnableLongRunning() {
		return enableLongRunning;
	}

	public void setEnableLongRunning(Boolean enableLongRunning) {
		this.enableLongRunning = enableLongRunning;
	}
	
	public Boolean getEnableExpiration() {
		return enableExpiration;
	}

	public void setEnableExpiration(Boolean enableExpiration) {
		this.enableExpiration = enableExpiration;
	}

}
