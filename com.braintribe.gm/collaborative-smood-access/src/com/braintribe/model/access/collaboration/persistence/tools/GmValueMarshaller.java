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
package com.braintribe.model.access.collaboration.persistence.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.generic.tools.GmValueCodec;

/**
 * @author peter.gazdik
 */
public class GmValueMarshaller implements Marshaller {

	public static final GmValueMarshaller INSTANCE = new GmValueMarshaller();

	private GmValueMarshaller() {
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		marshall(out, value);
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(in);
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		String valueAsGmString = GmValueCodec.objectToGmString(value);

		try {
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write(valueAsGmString);
			writer.flush();

		} catch (IOException e) {
			throw new MarshallException("Error while marshalling value: " + valueAsGmString, e);
		}
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		try {
			InputStreamReader isReader = new InputStreamReader(in, "UTF-8");
			BufferedReader reader = new BufferedReader(isReader);
			String valueAsGmString = reader.readLine();

			return GmValueCodec.objectFromGmString(valueAsGmString);

		} catch (IOException e) {
			throw new MarshallException("Error while unmarshalling GM value from input stream", e);
		}
	}

}
