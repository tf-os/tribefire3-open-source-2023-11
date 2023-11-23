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
package com.braintribe.codec.dom.genericmodel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.dom.DomEncodingContext;
import com.braintribe.utils.xml.XmlTools;

public class ValueDomCodec<T> implements Codec<T, Element> {
	private Codec<T, String> delegate;
	private String elementName = "value";
	
	@Configurable 
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	
	@Configurable @Required
	public void setDelegate(Codec<T, String> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public T decode(Element element) throws CodecException {
		String encodedValue = XmlTools.getFirstTextAsString(element);
		return delegate.decode(encodedValue);
	}
	
	@Override
	public Element encode(T value) throws CodecException {
		DomEncodingContext ctx = EncodingContext.get();
		Document document = ctx.getDocument();
		Element element = document.createElement(elementName);
		Text text = document.createTextNode(delegate.encode(value));
		element.appendChild(text);
		return element;
	}
	
	@Override
	public Class<T> getValueClass() {
		return delegate.getValueClass();
	}

}
