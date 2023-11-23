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
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.utils.xml.XmlTools;

public class LegacyAnyTypeDomCodec implements Codec<Object, Element> {
	private GenericModelDomCodecRegistry codecRegistry;
	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	public void setCodecRegistry(GenericModelDomCodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	@Override
	public Element encode(Object value) throws CodecException {
		throw new UnsupportedOperationException("Any Types are not supported. Just decoding of older encoded xmls could still be read.");
	}
	
	@Override
	public Object decode(Element encodedValue) throws CodecException {
		String typeSignature = encodedValue.getAttribute("type");
		
		if (typeSignature == null || typeSignature.length() == 0)
			throw new CodecException("AnyType must have a type attribute to be decoded from a DOM Element");
		
		Element childElement = XmlTools.getFirstElement(encodedValue, null);
		GenericModelType type = typeReflection.getType(typeSignature);
		Codec<Object, Element> codec = codecRegistry.getCodec(type);
		
		Object value = codec.decode(childElement);
		return value;
	}

	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}
}
