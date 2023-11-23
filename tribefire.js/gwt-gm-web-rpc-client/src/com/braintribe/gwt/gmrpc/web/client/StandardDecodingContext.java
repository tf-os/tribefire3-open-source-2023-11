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

import java.util.Set;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.genericmodel.client.codec.api.DefaultDecodingContext;
import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcException;
import com.braintribe.gwt.gmrpc.api.client.itw.TypeEnsurer;
import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpert;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.resource.source.TransientSource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StandardDecodingContext extends DefaultDecodingContext {
	private final TypeEnsurer typeEnsurer;
	private boolean lenientDecode;
	private GmSession session;
	private ResponseInfo responseInfo;
	 
	public StandardDecodingContext(TypeEnsurer typeEnsurer) {
		super();
		this.typeEnsurer = typeEnsurer;
	}
	
	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}
	
	public void setSession(GmSession session) {
		this.session = session;
	}
	
	public void setEmbeddedRequiredTypesExpert(final EmbeddedRequiredTypesExpert embeddedRequiredTypesExpert) {
		setIntrinsicModelExtractor(assembly -> embeddedRequiredTypesExpert.getModelFromAssembly(assembly));
	}


	@Override
	public GenericModelType resolveType(String type) {
		return typeReflection.getType(type);
	}

	@Override
	public void ensureTypes(Set<String> types, AsyncCallback<Void> callback) {
		if (typeEnsurer != null)
			typeEnsurer.ensureTypes(types, callback);
		else
			callback.onSuccess(null);
	}

	@Override
	public void ensureTypes(Set<String> types) throws CodecException {
		if (typeEnsurer != null) {
			try {
				typeEnsurer.ensureTypes(types);
			} catch (GmRpcException e) {
				throw new CodecException("error while ensuring types", e);
			}
		}
	}
	
	public void setLenientDecode(boolean lenientDecode) {
		this.lenientDecode = lenientDecode;
	}

	@Override
	public boolean isLenientDecode() {
		return lenientDecode;
	}
	
	@Override
	public GenericEntity create(EntityType<?> entityType) {
		GenericEntity entity = session != null ? session.createRaw(entityType) : entityType.createRaw();
				
		if (responseInfo != null) {
			if (entity.type() == TransientSource.T) {
				TransientSource transientSource = (TransientSource)entity;
				responseInfo.sources.add(transientSource);				
			}
		}
		
		return entity;
	}
}
