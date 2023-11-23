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
package com.braintribe.codec.marshaller.stax.decoder.scalar;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.DecodingContext;
import com.braintribe.model.generic.reflection.EnumType;

public class EnumDecoder extends ScalarValueDecoder {
	private String type;
	
	@Override
	public void begin(Attributes attributes)
			throws MarshallException {
		this.type = attributes.getValue("type");
	}
	@Override
	protected Object decode(DecodingContext context, String text) throws MarshallException {
		EnumType enumType = context.findType(type);
		
		if (enumType != null) {
			try {
				return enumType.getInstance(text);
			} catch (Exception e) {
				if (context.getDecodingLenience().isEnumConstantLenient())
					return null;
				else
					throw new MarshallException("unkown enum constant: " + type + "." + text);
			}
		}
		else {
			if (context.getDecodingLenience().isTypeLenient())
				return null;
			else
				throw new MarshallException("unkown enum type: " + type);
		}
	}
}
