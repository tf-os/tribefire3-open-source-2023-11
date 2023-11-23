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
package com.braintribe.codec.marshaller.dom;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.reflection.GenericModelType;

public class GmXmlCodec<T> implements GmCodec<T, String> {
	private GmDomCodec<T> domCodec;
	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory poxTranformerFactory = TransformerFactory.newInstance();

	public GmXmlCodec(Class<T> valueClass) {
		domCodec = new GmDomCodec<T>(valueClass);
	}

	public GmXmlCodec(GenericModelType type) {
		domCodec = new GmDomCodec<T>(type);
	}

	public GmXmlCodec() {
		domCodec = new GmDomCodec<T>();
	}

	public GmDomCodec<T> getDomCodec() {
		return domCodec;
	}

	@Override
	public String encode(T value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public T decode(String encodedValue) throws CodecException {
		return decode(encodedValue, GmDeserializationOptions.deriveDefaults().build());
	}

	@Override
	public Class<T> getValueClass() {
		return domCodec.getValueClass();
	}

	@Override
	public T decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
		StringReader reader = new StringReader(encodedValue);
		Document document = null;
		try {
			document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(reader));
		} catch (Exception e) {
			throw new CodecException("error while decoding xml into DOM", e);
		}

		return domCodec.decode(document);
	}

	@Override
	public String encode(T value, GmSerializationOptions options) throws CodecException {
		try {

			Document document = domCodec.encode(value, options);

			StringWriter writer = new StringWriter();

			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(writer);

			Transformer serializer;
			synchronized (poxTranformerFactory) {
				poxTranformerFactory.setAttribute("indent-number", 2);
				serializer = poxTranformerFactory.newTransformer();
			}

			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");

			serializer.transform(domSource, streamResult);

			return writer.toString();
		} catch (CodecException e) {
			throw e;
		} catch (Exception e) {
			throw new CodecException("error while encoding value", e);
		}
	}

	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		domCodec.setRequiredTypesReceiver(requiredTypesReceiver);
	}

}
