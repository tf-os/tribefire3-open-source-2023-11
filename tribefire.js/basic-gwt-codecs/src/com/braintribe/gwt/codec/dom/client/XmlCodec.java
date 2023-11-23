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
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public class XmlCodec<T> implements Codec<T, String> {
	private Codec<T, Element> domCodec;
	
	public XmlCodec(Codec<T, Element> domCodec) {
		super();
		setDomCodec(domCodec);
	}
	
	public XmlCodec() {
	}
	
	@Configurable
	public void setDomCodec(Codec<T, Element> domCodec) {
		this.domCodec = domCodec;
	}
	
	public Codec<T, Element> getDomCodec() {
		return domCodec;
	}

	@Override
	public String encode(T value) throws CodecException {
		Element element = domCodec.encode(value);
		String xml = element.toString();
		return xml;
	}
	
	@Override
	public T decode(String encodedValue) throws CodecException {
		try {
			Document document = XMLParser.parse(encodedValue);
			return domCodec.decode(document.getDocumentElement());
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
