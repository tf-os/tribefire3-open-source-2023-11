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
package com.braintribe.gwt.genericmodel.client.codec.dom4;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.GmAsyncCodec;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.api.GmEncodingContext;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

public class GmXmlCodec<T> implements Codec<T, String>, GmAsyncCodec<T, String> {
	private GmDomCodec<T> domCodec;
	
	private static boolean hasProcessingInstructionBug = hasProcessingInstructionBug();
	
	private static boolean hasProcessingInstructionBug() {
		Document document = XMLParser.createDocument();
		document.appendChild(document.createProcessingInstruction("pi", ""));
		document.appendChild(document.createElement("e"));
		String xml = document.toString();
		return !xml.contains("<?pi");
	}
	
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
	public Class<T> getValueClass() {
		return domCodec.getValueClass();
	}

	@Override
	public T decode(String encodedValue) throws CodecException {
		Document document = null;
		try {
			document = XMLParser.parse(encodedValue);
		} catch (Exception e) {
			throw new CodecException("error while decoding xml into DOM", e);
		}
		
		return domCodec.decode(document);
	}
	
	@Override
	public String encode(T value) throws CodecException {
		return encode(value, null);
	}

	@Override
	public String encode(T value, GmEncodingContext context) throws CodecException {
		Document document = domCodec.encode(value, context);
		
		String xml = null;
		if (hasProcessingInstructionBug) {
			StringBuilder builder = new StringBuilder();
			
			Node node = document.getFirstChild();
			while (node != null) {
				builder.append(node.toString());
				if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
					builder.append("\n");
				}
				node = node.getNextSibling();
			}
			xml = builder.toString();
		}
		else {
			xml = document.toString();
		}
		return xml;
	}

	@Override
	public <T1 extends T> T1 decode(String encodedValue, GmDecodingContext context) throws CodecException {
		Document document = null;
		try {
			document = XMLParser.parse(encodedValue);
		} catch (Exception e) {
			throw new CodecException("error while decoding xml into DOM", e);
		}
		
		return domCodec.decode(document, context);
	}
	
	@Override
	public <T1 extends T> Future<T1> decodeAsync(String encodedValue, GmDecodingContext context) {
		Document document = null;
		try {
			document = XMLParser.parse(encodedValue);
		} catch (Exception e) {
			Future<T1> future = new Future<T1>();
			future.onFailure(new CodecException("error while decoding xml into DOM", e));
			return future;
		}
		
		return domCodec.decodeAsync(document, context);
	}
	
	@Override
	public Future<String> encodeAsync(T value, GmEncodingContext context) {
		Future<String> future = new Future<>();
		domCodec.encodeAsync(value, context) //
				.andThen(document -> {
					String xml = null;
					if (!hasProcessingInstructionBug)
						xml = document.toString();
					else {
						StringBuilder builder = new StringBuilder();

						Node node = document.getFirstChild();
						while (node != null) {
							builder.append(node.toString());
							if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
								builder.append("\n");
							}
							node = node.getNextSibling();
						}
						xml = builder.toString();
					}

					future.onSuccess(xml);
				}).onError(future::onFailure);
		
		return future;
	}
	
	
}
