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
package com.braintribe.model.meta;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.GmfException;
import com.braintribe.processing.async.api.AsyncCallback;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Marker interface for types which can be deployed (or woven using the Instant Type Weaving). For now we only support deployment of ProtoGmMetaModel.
 */
@JsType(namespace = GmCoreApiInteropNamespaces.metadata)
public interface Weavable {

	default void deploy() {
		try {
			GMF.getTypeReflection().deploy(this);

		} catch (GmfException e) {
			throw new RuntimeException("Error while deploying model!", e);
		}
	}

	@JsMethod(name = "deployAsync")
	default void deploy(AsyncCallback<Void> asyncCallack) {
		GMF.getTypeReflection().deploy(this, asyncCallack);
	}

}
