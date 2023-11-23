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
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmMetaModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GmDecodingContext {
	GenericModelType resolveType(String type);
	void ensureTypes(Set<String> types, AsyncCallback<Void> callback);
	void ensureTypes(Set<String> types) throws CodecException;
	boolean isLenientDecode();
	ProxyContext getProxyContext();
	Function<Object, GmMetaModel> getIntrinsicModelExtractor();
	GenericEntity create(EntityType<?> entityType);
}
