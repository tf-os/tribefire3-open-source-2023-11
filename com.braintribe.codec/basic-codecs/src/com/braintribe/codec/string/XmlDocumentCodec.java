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
package com.braintribe.codec.string;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.utils.xml.XmlTools;

public class XmlDocumentCodec<T> implements Codec<T, String> {
	private Codec<T, Document> domCodec;
	
	public XmlDocumentCodec(Codec<T, Document> domCodec) {
		super();
		setDomCodec(domCodec);
	}
	
	public XmlDocumentCodec() {
	}
	
	public void setDomCodec(Codec<T, Document> domCodec) {
		this.domCodec = domCodec;
	}
	
	public Codec<T, Document> getDomCodec() {
		return domCodec;
	}

	@Override
	public String encode(T value) throws CodecException {
		Document document = domCodec.encode(value);
		try {
			//Let the GC remove the possibly large object
			value = null;
			String xml = XmlTools.toString(document);
			return xml;
		} catch (TransformerException e) {
			throw new CodecException("error while parsing xml", e);
		}
	}
	
	@Override
	public T decode(String encodedValue) throws CodecException {
		if (encodedValue == null || encodedValue.length() == 0)
			return null;
		
		try {
			Document document = XmlTools.parseXML(encodedValue);
			//Let the GC remove the possibly large string
			encodedValue = null;
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
