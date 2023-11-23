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

import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class NullWrapperCodec<T> implements Codec<T, Element> {
	private Codec<T, Element> valueCodec;
	@Configurable @Required
	public void setValueCodec(Codec<T, Element> valueCodec) {
		this.valueCodec = valueCodec;
	}
	
	@Override
	public T decode(Element element) throws CodecException {
		if (element.getTagName().equals("null"))
			return null;
		
		return valueCodec.decode(element);
	}
	
	@Override
	public Element encode(T value) throws CodecException {
		EncodingContext ctx = EncodingContext.get();
		if (value == null) {
			return ctx.getDocument().createElement("null");
		}
		else {
			return valueCodec.encode(value);
		}
	}
	
	@Override
	public Class<T> getValueClass() {
		return valueCodec.getValueClass();
	}
}
