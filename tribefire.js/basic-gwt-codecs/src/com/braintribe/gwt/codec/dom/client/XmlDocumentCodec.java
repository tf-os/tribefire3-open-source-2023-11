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
package com.braintribe.gwt.codec.dom.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

public class XmlDocumentCodec<T> implements Codec<T, String> {
	private Codec<T, Document> domCodec;
	private static boolean hasProcessingInstructionBug = hasProcessingInstructionBug();
	
	private static boolean hasProcessingInstructionBug() {
		Document document = XMLParser.createDocument();
		document.appendChild(document.createProcessingInstruction("pi", ""));
		document.appendChild(document.createElement("e"));
		String xml = document.toString();
		return !xml.contains("<?pi");
	}

	
	public XmlDocumentCodec(Codec<T, Document> domCodec) {
		super();
		setDomCodec(domCodec);
	}
	
	public XmlDocumentCodec() {
	}
	
	@Configurable
	public void setDomCodec(Codec<T, Document> domCodec) {
		this.domCodec = domCodec;
	}
	
	public Codec<T, Document> getDomCodec() {
		return domCodec;
	}

	@Override
	public String encode(T value) throws CodecException {
		Document document = domCodec.encode(value);

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
	public T decode(String encodedValue) throws CodecException {
		try {
			Document document = XMLParser.parse(encodedValue);
			return domCodec.decode(document);
		}
		catch (Exception e) {
			throw new CodecException("error while parsing xml", e);
		}
	}
	
	@Override
	public Class<T> getValueClass() {
		return domCodec.getValueClass();
	}
}
