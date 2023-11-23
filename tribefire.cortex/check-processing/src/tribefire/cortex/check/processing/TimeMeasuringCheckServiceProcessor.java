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
package tribefire.cortex.check.processing;

import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

/**
 * Measures the time a processor takes to execute a {@link CheckRequest}.
 * 
 * @author christina.wilpernig
 */
public class TimeMeasuringCheckServiceProcessor implements ServiceProcessor<CheckRequest, CheckResult> {

	private CheckProcessor checkProcessor;

	public TimeMeasuringCheckServiceProcessor(CheckProcessor checkProcessor) {
		this.checkProcessor = checkProcessor;
	}
	
	@Override
	public CheckResult process(ServiceRequestContext requestContext, CheckRequest request) {
		
		long t0 = System.nanoTime();
		
		CheckResult checkResult = checkProcessor.check(requestContext, request);
		
		checkResult.setElapsedTimeInMs((System.nanoTime() - t0)/1_000_000.0);
		
		return checkResult;
	}

}
