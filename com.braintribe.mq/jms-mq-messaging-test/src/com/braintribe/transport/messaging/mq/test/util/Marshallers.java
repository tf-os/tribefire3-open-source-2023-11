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
package com.braintribe.transport.messaging.mq.test.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.braintribe.codec.json.genericmodel.GenericModelJsonCodec;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.codec.marshaller.bin.BinMarshaller;
import com.braintribe.codec.marshaller.json.JsonMarshaller;
import com.braintribe.codec.marshaller.sax.SaxMarshaller;
import com.braintribe.codec.marshaller.xml.XmlMarshaller;
import com.braintribe.model.generic.GMF;

public class Marshallers {
	
	public static MarshallerRegistry registry;

	public static final String XML = "application/xml";
	public static final String XML_STREAM = "application/stream+xml";
	public static final String BIN = "application/gm";
	public static final String JSON = "application/json";

	private static final Map<String, MarshallerRegistryEntry> marshallerEntries = new HashMap<String, MarshallerRegistryEntry>(10);
			
	static {
		registerMarshaller(XML , new XmlMarshaller());
		registerMarshaller(XML_STREAM , new SaxMarshaller<Object>());
		registerMarshaller(BIN , new BinMarshaller());
		registerMarshaller(JSON, createJsonMarshaller());
		createMarshallerRegistry();
	}
	
	public static Set<String> getMappedMimeTypes() {
		return marshallerEntries.keySet();
	}
	
	private static Marshaller createJsonMarshaller() {
		JsonMarshaller jsonMarshaller = new JsonMarshaller();
		GenericModelJsonCodec<Object> jsonDelegateCodec = new GenericModelJsonCodec<Object>();
		jsonDelegateCodec.setType(GMF.getTypeReflection().getBaseType());
		jsonDelegateCodec.setGenericModelTypeReflection(GMF.getTypeReflection());
		jsonMarshaller.setJsonCodec(jsonDelegateCodec);
		return jsonMarshaller;
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
