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
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;

public class GenericModelObjectDomCodec implements Codec<Object, Element> {
	private GenericModelDomCodecRegistry codecRegistry;
	
	public void setCodecRegistry(GenericModelDomCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public Object decode(Element encodedValue) throws CodecException {
		String tagName = encodedValue.getTagName();
		Codec<?, Element> codec = codecRegistry.getCodecByTagName(tagName);
		return codec.decode(encodedValue);
	}
	
	@Override
	public Element encode(Object value) throws CodecException {
		GenericModelType actualType = GMF.getTypeReflection().getType(value);
		return codecRegistry.getCodec(actualType).encode(value);
	}
	
	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}
}
