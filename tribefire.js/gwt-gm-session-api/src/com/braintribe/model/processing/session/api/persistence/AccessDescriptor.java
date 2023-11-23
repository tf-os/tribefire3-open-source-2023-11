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
package com.braintribe.model.processing.session.api.persistence;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.meta.GmMetaModel;

import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(namespace = GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public class AccessDescriptor {

	private final String accessId;
	private final GmMetaModel dataModel;
	private final String accessDenotationType;

	public AccessDescriptor(String accessId, GmMetaModel dataModel, String accessDenotationType) {
		this.accessId = accessId;
		this.dataModel = dataModel;
		this.accessDenotationType = accessDenotationType;
	}

	public String accessId() {
		return accessId;
	}

	public GmMetaModel dataModel() {
		return dataModel;
	}

	public String accessDenotationType() {
		return accessDenotationType;
	}

}
