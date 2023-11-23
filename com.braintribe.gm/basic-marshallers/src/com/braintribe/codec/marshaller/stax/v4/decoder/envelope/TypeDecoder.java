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
package com.braintribe.codec.marshaller.stax.v4.decoder.envelope;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.DecodingContext;
import com.braintribe.codec.marshaller.stax.TypeInfo4Read;
import com.braintribe.codec.marshaller.stax.v4.decoder.scalar.ScalarValueDecoder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;

public class TypeDecoder extends ScalarValueDecoder {
	private TypeInfo4Read typeInfo;
	private boolean isEntityType;
	
	@Override
	public void begin(Attributes attributes) throws MarshallException {
		super.begin(attributes);
		typeInfo = new TypeInfo4Read();
		
		
		String alias = attributes.getValue("alias");
		String as = attributes.getValue("as");
		
		typeInfo.alias = alias;
		typeInfo.as = (as != null && !as.isEmpty())? as: alias; 
		 
		String encodedNum = attributes.getValue("num");
		if (encodedNum != null) {
			typeInfo.setCount(Integer.parseInt(encodedNum));
			isEntityType = true;
		}
	}
	
	@Override
	protected Object decode(DecodingContext context, String text)
			throws MarshallException {
		typeInfo.typeSignature = text;
		if (isEntityType) {
			EntityType<?> entityType = context.findType(text);
			
			if (entityType != null) {
				typeInfo.type = entityType;
			}
			else if (!context.getDecodingLenience().isTypeLenient()) {
				throw new MarshallException("unable to decode unkown type: " + text);
			}
		}
		else {
			EnumType enumType = context.findType(text);
			
			if (enumType != null) {
				typeInfo.type = enumType;
			}
			else if (!context.getDecodingLenience().isTypeLenient()) {
				throw new MarshallException("unable to decode unkown type: " + text);
			}
		}
		
		return typeInfo;
	}
}
