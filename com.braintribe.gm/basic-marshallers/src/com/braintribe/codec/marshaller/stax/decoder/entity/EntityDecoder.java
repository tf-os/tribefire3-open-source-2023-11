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
package com.braintribe.codec.marshaller.stax.decoder.entity;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.PropertyAbsenceHelper;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class EntityDecoder extends Decoder {
	private GenericEntity entity;
	private PropertyAbsenceHelper propertyAbsenceHelper;
	private EntityDecoderPreparation preparation;
	private EntityType<?> entityType;
	
	public EntityDecoder(EntityDecoderPreparation preparation) {
		this.preparation = preparation;
		this.entityType = preparation.entityType;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String elementName, Attributes attributes) throws MarshallException{
		String name = attributes.getValue("name");
		return preparation.newPropertyDecoder(decodingContext, entity, propertyAbsenceHelper, name);
	}
	
	@Override
	public void end() throws MarshallException {
		if (propertyAbsenceHelper != null)
			propertyAbsenceHelper.ensureAbsenceInformation(entityType, entity);
		
		parent.notifyValue(this, entity);
	}

	@Override
	public void begin(Attributes attributes)
			throws MarshallException {
		try {
			entity = decodingContext.isEnhanced()? entityType.create(): entityType.createPlain();
			propertyAbsenceHelper = decodingContext.providePropertyAbsenceHelper();

			String idString = attributes.getValue("id");
			
			decodingContext.register(entity, idString);
		}
		catch (Exception e) {
			throw new MarshallException("error while decoding entity", e);
		}
	}
}
