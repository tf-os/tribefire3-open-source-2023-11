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
package com.braintribe.model.processing.service.commons;

import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.service.api.ServiceRequest;

public class NoOpServiceRequestSummaryLogger implements ServiceRequestSummaryLogger {
	
	public static final ServiceRequestSummaryLogger INSTANCE = new NoOpServiceRequestSummaryLogger();
	
	private NoOpServiceRequestSummaryLogger() {
	}

	@Override
	public void startTimer(String partialDescription) {
		// no-op
	}

	@Override
	public void stopTimer(String partialDescription) {
		// no-op
	}

	@Override
	public void stopTimer() {
		// no-op
	}

	@Override
	public String oneLineSummary(ServiceRequest request) {
		return null;
	}

	@Override
	public String summary(Object caller, ServiceRequest request) {
		return null;
	}

	@Override
	public void logOneLine(String prefix, ServiceRequest request) {
		// no-op
	}

	@Override
	public void log(Object caller, ServiceRequest request) {
		// no-op
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

}
