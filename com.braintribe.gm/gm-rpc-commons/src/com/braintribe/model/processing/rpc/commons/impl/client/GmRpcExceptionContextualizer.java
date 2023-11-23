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
package com.braintribe.model.processing.rpc.commons.impl.client;

import java.util.Deque;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.ndc.mbean.NestedDiagnosticContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.utils.StringTools;

public class GmRpcExceptionContextualizer {

	public static void enhanceMessage(Failure message, AttributeContext attributeContext, ServiceRequest request) {

		StringBuilder sb = new StringBuilder(message.getMessage() != null ? message.getMessage() : "(no message)");
		sb.append(" [");
		if (attributeContext != null) {
			sb.append("Context:");
			sb.append(stringify(attributeContext));
		}
		if (request != null) {
			if (sb.length() > 0) sb.append(", ");
			sb.append("Request:");
			sb.append(request.stringify());
		}
		String threadName = Thread.currentThread().getName();
		if (sb.length() > 0) sb.append(", ");
		sb.append("Threadname:");
		sb.append(threadName);
		sb.append(']');
		String enhancedMessage = sb.toString();
		
		message.setMessage(enhancedMessage);
	}
	
	public static <E extends Throwable> E enhanceException(E t, AttributeContext attributeContext, GenericEntity request) {
		String message = t.getMessage();
		StringBuilder sb = new StringBuilder(message != null ? message : "(no message)");
		sb.append(" [");
		if (attributeContext != null) {
			sb.append("Context:");
			sb.append(stringify(attributeContext));
		}
		if (request != null) {
			if (sb.length() > 0) sb.append(", ");
			sb.append("Request:");
			sb.append(request.stringify());
		}
		String threadName = Thread.currentThread().getName();
		if (sb.length() > 0) sb.append(", ");
		sb.append("Threadname:");
		sb.append(threadName);
		
		Deque<String> ndcCollection = NestedDiagnosticContext.getNdc();
		if (ndcCollection != null && !ndcCollection.isEmpty()) {
			String ndc = StringTools.createStringFromCollection(ndcCollection, ",");
			sb.append(", ndc: ");
			sb.append(ndc);
		}
		sb.append(']');
		String enhancedMessage = sb.toString();
		
		return Exceptions.contextualize(t, enhancedMessage);
	}
	
	public static String stringify(AttributeContext attributeContext) {
		StringBuilder sb = new StringBuilder(attributeContext.getClass().getSimpleName());
		sb.append('[');
		attributeContext.findAttribute(RequestedEndpointAspect.class).ifPresent(requestedEndpoint -> {
			sb.append("requested endpoint: ");
			sb.append(requestedEndpoint);
		});

		attributeContext.findAttribute(RequestorAddressAspect.class).ifPresent( requestorAddress -> {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("requestor address: ");
			sb.append(requestorAddress);
		});
		
		attributeContext.findAttribute(RequestorUserNameAspect.class).ifPresent( username -> {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("requestor user: ");
			sb.append(username);
		});
		
		sb.append(']');
		return sb.toString();
	}

}
