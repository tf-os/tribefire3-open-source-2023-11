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
package com.braintribe.codec.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.utils.xml.XmlTools;

public class XmlCodec<T> implements Codec<T, String> {

	protected DomCodec<T> xmlCodec;
	protected String path;
	
	public XmlCodec(DomCodec<T> xmlCodec) {
		this(xmlCodec, null);
	}
	
	public XmlCodec(DomCodec<T> xmlCodec, String path) {
		this.xmlCodec= xmlCodec;
		this.path = path;
	}
	
	@Override
	public T decode(String s) throws CodecException {
		if (s == null || s.trim().length() == 0)
			return null;
		
		Element e;
		try {
			Document doc = XmlTools.parseXML(s);
			
			e = doc.getDocumentElement();
			if (path!=null) e = XmlTools.getElement(e, path);
		} catch (Exception ex) {
			throw new CodecException("error while decoding xml", ex);
		}
		
		return xmlCodec.decode(e);
	}

	@Override
	public String encode(T obj) throws CodecException {
		if (obj == null) return ""; //TODO: really? or ask codec?
		
		try {
			Document doc = XmlTools.createDocument();
			
			Element rootElement = xmlCodec.encode(doc, obj);
			if (rootElement == null) return "";
			
			doc.appendChild(rootElement);
			
			return XmlTools.toString(doc);
		} catch (Exception e) {
			throw new CodecException("error while encoding dom", e);
		}
	}
	
	@Override
	public Class<T> getValueClass() {
	    return xmlCodec.getValueClass();
	}

}
