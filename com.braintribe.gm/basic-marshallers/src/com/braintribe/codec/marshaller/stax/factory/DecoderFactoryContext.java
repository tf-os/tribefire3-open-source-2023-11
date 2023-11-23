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
package com.braintribe.codec.marshaller.stax.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.DecodingContext;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.decoder.entity.EntityDecoder;
import com.braintribe.codec.marshaller.stax.decoder.entity.EntityDecoderPreparation;
import com.braintribe.codec.marshaller.stax.decoder.entity.LenientDecoder;
import com.braintribe.model.generic.reflection.EntityType;

public class DecoderFactoryContext {
	public Map<String, EntityDecoderPreparation> entityDecoderPreparations = new ConcurrentHashMap<String, EntityDecoderPreparation>();

	public Decoder newEntityDecoder(DecodingContext decodingContext, Attributes attributes) throws MarshallException {
		String typeSignature = attributes.getValue("type");
		
		if (typeSignature != null) {
			EntityDecoderPreparation preparation = entityDecoderPreparations.get(typeSignature);
			
			if (preparation == null) {
				EntityType<?> entityType = decodingContext.findType(typeSignature);

				if (entityType == null) { 
					if (decodingContext.getDecodingLenience().isTypeLenient())
						return new LenientDecoder();
					else
						throw new MarshallException("unable to decode unkown type: " + typeSignature);
				}
				else {
					preparation = new EntityDecoderPreparation(entityType);
					entityDecoderPreparations.put(typeSignature, preparation);
				}
			}
			
			return new EntityDecoder(preparation);
		}
		else 
			throw new MarshallException("entity elements in the pool element need a typeSignature attribute");
	}
}
