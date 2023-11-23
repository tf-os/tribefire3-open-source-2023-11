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
package com.braintribe.model.processing.session.api.managed;

import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.processing.async.api.AsyncCallback;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface MergeBuilder {

	MergeBuilder adoptUnexposed(boolean adopt);
	MergeBuilder keepEnvelope(boolean keepEvelope);
	MergeBuilder envelopeFactory(Function<GenericEntity, GenericEntity> envelopeFactory);
	MergeBuilder suspendHistory(boolean suspend);
	MergeBuilder transferTransientProperties(boolean transferTransientProperties);
	
	@JsMethod (name = "doForSync")
	<T> T doFor(T data) throws GmSessionException;
	<T> void doFor(T data, AsyncCallback<T> asyncCallback) throws GmSessionException;

}
