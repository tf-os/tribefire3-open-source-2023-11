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
package com.braintribe.transport.messaging.api.test.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;

public class Marshallers {

	public static MarshallerRegistry registry;

	private static final Marshaller XML_MARSHALLER = new StaxMarshaller();
	private static final Marshaller BIN_MARSHALLER = new Bin2Marshaller();
	private static final Marshaller JSON_MARSHALLER = new JsonStreamMarshaller();

	public static final String XML = "application/xml";
	public static final String XML_STREAM = "application/stream+xml";
	public static final String BIN = "application/gm";
	public static final String JSON = "application/json";

	private static final Map<String, MarshallerRegistryEntry> marshallerEntries = new HashMap<String, MarshallerRegistryEntry>(10);

	static {
		registerMarshaller(XML, XML_MARSHALLER);
		registerMarshaller(XML_STREAM, XML_MARSHALLER);
		registerMarshaller(BIN, BIN_MARSHALLER);
		registerMarshaller(JSON, JSON_MARSHALLER);
		createMarshallerRegistry();
	}

	public static Set<String> getMappedMimeTypes() {
		return marshallerEntries.keySet();
	}

	public static Marshaller get(String mimeType) {
		return registry.getMarshaller(mimeType);
	}

	private static void registerMarshaller(final String mimeType, final Marshaller marshaller) {
		marshallerEntries.put(mimeType, new MarshallerRegistryEntry() {

			@Override
			public String getMimeType() {
				return mimeType;
			}

			@Override
			public Marshaller getMarshaller() {
				return marshaller;
			}

		});
	}

	private static void createMarshallerRegistry() {
		registry = new MarshallerRegistry() {

			@Override
			public Marshaller getMarshaller(String mimeType) {
				return marshallerEntries.get(mimeType) != null ? marshallerEntries.get(mimeType).getMarshaller() : null;
			}

			@Override
			public MarshallerRegistryEntry getMarshallerRegistryEntry(String mimeType) {
				return marshallerEntries.get(mimeType);
			}

		};
	}

}
