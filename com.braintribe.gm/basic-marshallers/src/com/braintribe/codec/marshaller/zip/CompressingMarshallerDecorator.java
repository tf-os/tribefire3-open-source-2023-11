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
package com.braintribe.codec.marshaller.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.utils.Base64;
import com.braintribe.utils.IOTools;

public class CompressingMarshallerDecorator implements CharacterMarshaller {

	protected CharacterMarshaller embeddedMarshaller;

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		this.marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.embeddedMarshaller.marshall(baos, value, options);
			String base64Encoded = Base64.encodeBytes(baos.toByteArray(), 0, baos.size(), Base64.GZIP);
			out.write(base64Encoded.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new MarshallException("Could not create GZIPOutputStream around the output stream.", e);
		}
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return this.unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}
	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOTools.pump(in, baos);
			String decodedString = new String(Base64.decode(baos.toString("UTF-8")), "UTF-8");
			return this.embeddedMarshaller.unmarshall(new ByteArrayInputStream(decodedString.getBytes()), options);
		} catch (IOException e) {
			throw new MarshallException("Could not create GZIPInputStream around the input stream.", e);
		}
	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			StringWriter buffer = new StringWriter();
			this.embeddedMarshaller.marshall(buffer, value, options);
			String marshalledRawString = buffer.toString();
			String base64Encoded = Base64.encodeBytes(marshalledRawString.getBytes("UTF-8"), 0, marshalledRawString.length(), Base64.GZIP);
			writer.write(base64Encoded);
		} catch (IOException e) {
			throw new MarshallException("Could not create GZIPOutputStream around the output stream.", e);
		}
	}
	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		try {
			String encodedString = IOTools.slurp(reader);
			String decodedString = new String(Base64.decode(encodedString), "UTF-8");
			return this.embeddedMarshaller.unmarshall(new StringReader(decodedString), options);
		} catch (IOException e) {
			throw new MarshallException("Could not create GZIPInputStream around the input stream.", e);
		}
	}

	public CharacterMarshaller getEmbeddedMarshaller() {
		return embeddedMarshaller;
	}
	@Required
	@Configurable
	public void setEmbeddedMarshaller(CharacterMarshaller embeddedMarshaller) {
		this.embeddedMarshaller = embeddedMarshaller;
	}
}
