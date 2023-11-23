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
package com.braintribe.model.processing.ddra.endpoints.interceptors;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.model.processing.service.api.HttpRequestSupplierAspect;
import com.braintribe.model.processing.service.api.ServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resourceapi.stream.HasStreamCondition;
import com.braintribe.model.resourceapi.stream.HasStreamRange;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.lcd.StringTools;

public class HttpStreamingPreProcessor implements ServicePreProcessor<ServiceRequest>{

	@Override
	public ServiceRequest process(ServiceRequestContext requestContext, ServiceRequest serviceRequest) {
		requestContext.findAttribute(HttpRequestSupplierAspect.class) //
			.flatMap(s -> s.getFor(serviceRequest))
			.ifPresent(httpRequest -> processStreamHeaders(httpRequest, serviceRequest));
		
		return serviceRequest;
	}

	private void processStreamHeaders(HttpServletRequest httpRequest, ServiceRequest serviceRequest) {
		if (serviceRequest instanceof HasStreamCondition) {
			HasStreamCondition hasStreamCondition = (HasStreamCondition) serviceRequest;
			
			String ifNonMatchHeader = httpRequest.getHeader("If-None-Match");
			long ifModifiedSinceHeader = httpRequest.getDateHeader("If-Modified-Since");
			
			if (ifNonMatchHeader != null) {
				FingerprintMismatch streamCondition = FingerprintMismatch.T.create();
				streamCondition.setFingerprint(ifNonMatchHeader);
				hasStreamCondition.setCondition(streamCondition);
			}
			else if (ifModifiedSinceHeader != -1) {
				ModifiedSince streamCondition = ModifiedSince.T.create();
				streamCondition.setDate(new Date(ifModifiedSinceHeader));
				hasStreamCondition.setCondition(streamCondition);
			}
			
		}
		
		if (serviceRequest instanceof HasStreamRange) {
			HasStreamRange hasStreamRange = (HasStreamRange) serviceRequest;
			
			parseRangeHeader(httpRequest, hasStreamRange);
		}
	}
	
	private void parseRangeHeader(HttpServletRequest httpRequest, HasStreamRange hasStreamRange) {
		
		String rangeHeader = httpRequest.getHeader("Range");
		if (StringTools.isBlank(rangeHeader)) {
			return;
		}
		
		try {
			
			int index = rangeHeader.indexOf('=');
			if (index == -1) {
				throw new IllegalStateException("There is no '=' sign.");
			}
			String unit = rangeHeader.substring(0, index).trim();
			if (StringTools.isBlank(unit) || !unit.equalsIgnoreCase("bytes")) {
				throw new IllegalStateException("Only unit 'bytes' is supported.");
			}
			String rangeSpec = rangeHeader.substring(index+1).trim();
			index = rangeSpec.indexOf('-');
			if (index == -1) {
				throw new IllegalStateException("The range value "+rangeSpec+" does not contain '-'");
			}
			String start = rangeSpec.substring(0, index).trim();
			String end = null;
			if (index < rangeSpec.length()-1) {
				end = rangeSpec.substring(index+1).trim();
			}
			long startLong = Long.parseLong(start);
			long endLong = -1;
			if (!StringTools.isBlank(end)) {
				endLong = Long.parseLong(end);
			}
			
			StreamRange streamRange = StreamRange.T.create();
			
			streamRange.setStart(startLong);
			streamRange.setEnd(endLong);
			
			hasStreamRange.setRange(streamRange);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to parse Range header \""+rangeHeader+"\".", e);
		}
		
	}
}
