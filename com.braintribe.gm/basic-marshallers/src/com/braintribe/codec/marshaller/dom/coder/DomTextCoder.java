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
package com.braintribe.codec.marshaller.dom.coder;

import org.w3c.dom.Element;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.DomDecodingContext;
import com.braintribe.codec.marshaller.dom.DomEncodingContext;

public abstract class DomTextCoder<T> implements DomCoder<T> {
	private String elementName;
	
	public DomTextCoder(String elementName) {
		super();
		this.elementName = elementName;
	}

	@Override
	public T decode(DomDecodingContext context, Element element) throws CodecException {
		if (element.getTagName().equals("n"))
			return null;
		
		String text = element.getTextContent();
		return decodeText(context, text);
	}

	protected abstract T decodeText(DomDecodingContext context, String text) throws CodecException;

	@Override
	public Element encode(DomEncodingContext context, T value) throws CodecException {
		if (value == null)
			context.getDocument().createElement("n");
		
		String text = encodeText(context, value);
		
		Element element = context.getDocument().createElement(elementName);
		
		if (text != null)
			element.setTextContent(text);
		
		return element;
	}

	/**
	 * @param context
	 * @throws CodecException
	 */
	protected String encodeText(DomEncodingContext context, T value) throws CodecException {
		return value.toString();
	}
	
}
