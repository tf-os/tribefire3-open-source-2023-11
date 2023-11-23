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
package com.braintribe.codec.marshaller.dom.coder.scalar;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.DomDecodingContext;
import com.braintribe.codec.marshaller.dom.DomEncodingContext;
import com.braintribe.codec.marshaller.dom.TypeInfo;
import com.braintribe.codec.marshaller.dom.coder.DomTextCoder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

public class EnumDomCoder<T extends Enum<T>> extends DomTextCoder<T> {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private EnumType enumType;
	
	
	public EnumDomCoder(EnumType enumType) {
		super("e");
		this.enumType = enumType;
	}
	
	public EnumDomCoder() {
		super("e");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T decodeText(DomDecodingContext context, String text) throws CodecException {
		int index = text.lastIndexOf('.');
		
		if (enumType == null) {
			String typeKey = text.substring(0, index); 
			enumType = (EnumType) context.getTypeInfoByKey(typeKey).type;
		}
		
		String constantName = text.substring(index + 1);
		return (T) enumType.getInstance(constantName);
	}
	
	@Override
	protected String encodeText(DomEncodingContext context, T value) throws CodecException {
		final EnumType type;
		if (enumType != null) {
			type = enumType;
		} else {
			type = typeReflection.getType(value);
		}

		final TypeInfo typeInfo = context.registerRequiredType(type);
		return typeInfo.as + "." + value;
	}
}
