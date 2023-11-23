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
package com.braintribe.codec.marshaller.api;

import java.io.StringReader;
import java.io.StringWriter;

import com.braintribe.codec.CodecException;

public abstract class AbstractCharacterMarshaller implements CharacterMarshaller, HasStringCodec, GmCodec<Object, String> {

	@Override
	public Object decode(String encodedValue) throws CodecException {
		return decode(encodedValue, GmDeserializationOptions.deriveDefaults().build());
	}

	@Override
	public Object decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
		try {
			StringReader reader = new StringReader(encodedValue);
			return unmarshall(reader, options);
		} catch (MarshallException e) {
			throw new CodecException("error while unmarshalling", e);
		}
	}

	@Override
	public String encode(Object value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public String encode(Object value, GmSerializationOptions options) throws CodecException {
		StringWriter writer = new StringWriter();
		try {
			marshall(writer, value, options);
			return writer.toString();
		} catch (MarshallException e) {
			throw new CodecException("error while marshalling", e);
		}
	}

	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}

	@Override
	public GmCodec<Object, String> getStringCodec() {
		return this;
	}
}
