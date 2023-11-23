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

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class NullCodec<T> implements Codec<T, Element> {
	@Override
	public T decode(Element element) throws CodecException {
		return null;
	}
	
	@Override
	public Element encode(T value) throws CodecException {
		EncodingContext ctx = EncodingContext.get();
		return ctx.getDocument().createElement("null");
	}
	
	@Override
	public Class<T> getValueClass() {
		return null;
	}
}
