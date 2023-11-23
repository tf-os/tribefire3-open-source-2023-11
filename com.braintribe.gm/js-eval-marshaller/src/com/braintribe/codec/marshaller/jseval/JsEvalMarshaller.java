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
package com.braintribe.codec.marshaller.jseval;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.jseval.genericmodel.GenericModelJsEvalCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;

public class JsEvalMarshaller implements Marshaller {
	private static final Logger logger = Logger.getLogger(JsEvalMarshaller.class);
	private GenericModelJsEvalCodec<Object> codec;
	
	@Configurable @Required
	public void setCodec(GenericModelJsEvalCodec<Object> codec) {
		this.codec = codec;
	}
	
	@Override
	public void marshall(OutputStream out, Object value)
			throws MarshallException {
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(out, "UTF-8");
			String jsEvalCode = codec.encode(value);
			writer.write(jsEvalCode);
			writer.flush();
		} catch (Exception e) {
			throw new MarshallException("error while encoding value", e);
		}
		finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("error while closing writer", e);
				}
		}
	}
	
	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		throw new UnsupportedOperationException("no unmarshall supported");
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		marshall(out, value);
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(in);
	}
	
}
