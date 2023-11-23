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
package com.braintribe.gwt.gmrpc.web.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.genericmodel.client.codec.api.DefaultDecodingContext;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpert;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.service.api.result.ServiceResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EmbeddedTypesDecodingContext extends DefaultDecodingContext {

	private Map<String, GenericModelType> types;
	
	public EmbeddedTypesDecodingContext(EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert) {
		super();
		types = new HashMap<String, GenericModelType>();
		types.put(ServiceResult.T.getTypeSignature(), ServiceResult.T);
		for (GenericModelType type: embeddedRequiredTypesExpert.getMinimalTypes()) {
			types.put(type.getTypeSignature(), type);
		}
	}

	@Override
	public GenericModelType resolveType(String type) {
		return types.get(type);
	}

	@Override
	public void ensureTypes(Set<String> types, AsyncCallback<Void> callback) {
		// do nothing here
		callback.onSuccess(null);
	}

	@Override
	public boolean isLenientDecode() {
		return true;
	}
	
}
