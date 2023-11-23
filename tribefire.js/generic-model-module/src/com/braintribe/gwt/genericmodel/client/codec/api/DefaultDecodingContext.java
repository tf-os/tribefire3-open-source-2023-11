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
package com.braintribe.gwt.genericmodel.client.codec.api;

import java.util.Set;
import java.util.function.Function;

import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DefaultDecodingContext implements GmDecodingContext {
	protected static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private ProxyContext proxyContext;
	private Function<Object, GmMetaModel> intrinsicModelExtractor;
	
	public DefaultDecodingContext() {
		super();
	}
	
	@Override
	public GenericModelType resolveType(String type) {
		return typeReflection.getType(type);
	}

	@Override
	public void ensureTypes(Set<String> types, AsyncCallback<Void> callback) {
		callback.onSuccess(null);
	}

	@Override
	public void ensureTypes(Set<String> types) throws CodecException {
		// NOOP
	}

	@Override
	public boolean isLenientDecode() {
		return false;
	}
	
	public void setProxyContext(ProxyContext proxyContext) {
		this.proxyContext = proxyContext;
	}
	
	@Override
	public ProxyContext getProxyContext() {
		return proxyContext;
	}

	public void setIntrinsicModelExtractor(Function<Object, GmMetaModel> intrinsicModelExtractor) {
		this.intrinsicModelExtractor = intrinsicModelExtractor;
	}

	@Override
	public Function<Object, GmMetaModel> getIntrinsicModelExtractor() {
		return intrinsicModelExtractor;
	}
	
	@Override
	public GenericEntity create(EntityType<?> entityType) {
		return entityType.createRaw();
	}
}
