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

import java.io.Reader;
import java.io.Writer;

import com.braintribe.gm.model.reason.Maybe;

public interface CharacterMarshaller extends Marshaller {

	void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException;

	default void marshall(Writer writer, Object value) throws MarshallException {
		marshall(writer, value, GmSerializationOptions.deriveDefaults().build());
	}

	Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException;

	default Object unmarshall(Reader reader) throws MarshallException {
		return unmarshall(reader, GmDeserializationOptions.deriveDefaults().build());
	}

	default Maybe<Object> unmarshallReasoned(Reader reader, GmDeserializationOptions options) {
		return Maybe.complete(unmarshall(reader, options));
	}

	default Maybe<Object> unmarshallReasoned(Reader reader) throws MarshallException {
		return unmarshallReasoned(reader, GmDeserializationOptions.defaultOptions);
	}
}
