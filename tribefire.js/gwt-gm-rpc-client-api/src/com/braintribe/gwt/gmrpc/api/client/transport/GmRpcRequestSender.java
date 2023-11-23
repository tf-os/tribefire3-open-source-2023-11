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
package com.braintribe.gwt.gmrpc.api.client.transport;

import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcException;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpert;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ServiceResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GmRpcRequestSender {
	public ServiceResult sendRequest(ServiceRequest request, EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, boolean reasoned) throws GmRpcException;
	public void sendRequest(ServiceRequest request, EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert, AsyncCallback<ServiceResult> asyncCallback, boolean reasoned);
}
