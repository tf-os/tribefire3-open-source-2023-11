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
package com.braintribe.codec.marshaller.stax.v4.decoder.scalar;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.DecodingContext;
import com.braintribe.codec.marshaller.stax.TypeInfo4Read;
import com.braintribe.model.generic.reflection.EnumType;

public class EnumDecoder extends ScalarValueDecoder {

	@Override
	protected Object decode(DecodingContext context, String text) throws MarshallException {
		int index = text.lastIndexOf('.');
		
		String typeKey = text.substring(0, index); 
		TypeInfo4Read typeInfo = context.getTypeInfoByKey(typeKey);
		
		EnumType enumType = (EnumType) typeInfo.type;
		
		if (enumType == null) {
			if (context.getDecodingLenience().isTypeLenient()) {
				return null;
			}
			else 
				throw new MarshallException("unkown enum type: " + typeInfo.typeSignature);
		}
		
		String constantName = text.substring(index + 1);
		
		try {
			return enumType.getInstance(constantName);
		} catch (Exception e) {
			if (context.getDecodingLenience().isEnumConstantLenient()) {
				return null;
			}
			else 
				throw new MarshallException("unkown enum constant: " + enumType.getTypeSignature() + "." + constantName);
		}
	}
}
