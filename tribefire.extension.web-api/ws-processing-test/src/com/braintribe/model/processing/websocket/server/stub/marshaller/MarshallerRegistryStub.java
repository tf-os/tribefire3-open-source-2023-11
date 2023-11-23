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
package com.braintribe.model.processing.websocket.server.stub.marshaller;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.common.BasicMarshallerRegistryEntry;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;

public class MarshallerRegistryStub extends BasicConfigurableMarshallerRegistry {

	@Override
	public Marshaller getMarshaller(String mimeType) {
		switch (mimeType) {
			case "gm/json":
				return new JsonStreamMarshaller();
			case "application/json":
				return new JsonStreamMarshaller();
			case "application/xml":
				return new StaxMarshaller();
			default:
				return null;
		}
	}

	@Override
	public MarshallerRegistryEntry getMarshallerRegistryEntry(String mimeType) {
		switch (mimeType) {
			case "gm/json":
				return new BasicMarshallerRegistryEntry("gm/json", new JsonStreamMarshaller());
			case "application/json":
				return new BasicMarshallerRegistryEntry("application/json", new JsonStreamMarshaller());
			case "application/xml":
				return new BasicMarshallerRegistryEntry("application/xml", new StaxMarshaller());
			default:
				return null;
		}
	}

}
