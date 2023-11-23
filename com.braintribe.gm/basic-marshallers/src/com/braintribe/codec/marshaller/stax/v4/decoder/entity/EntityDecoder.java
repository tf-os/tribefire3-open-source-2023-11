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
package com.braintribe.codec.marshaller.stax.v4.decoder.entity;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stax.EntityRegistration;
import com.braintribe.codec.marshaller.stax.PropertyAbsenceHelper;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.codec.marshaller.stax.v4.decoder.ValueHostingDecoder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

public class EntityDecoder extends ValueHostingDecoder {
	private GenericEntity entity;
	private PropertyAbsenceHelper propertyAbsenceHelper;
	private EntityType<?> entityType;
	
	public EntityDecoder() {
		this.propertyDecorated = true;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String _elementName, Attributes attributes) throws MarshallException {
		return super.newDecoder(context, _elementName, attributes);
	}
	
	@Override
	public void notifyValue(Decoder origin, Object value) throws MarshallException {
		if (entity == null)
			return;
		
		Property property = entityType.findProperty(origin.propertyName);
		
		if (property != null) {
			if (origin.elementName.charAt(0) == 'a') {
				property.setAbsenceInformation(entity, (AbsenceInformation)value);
			}
			else {
				try {
					property.setDirectUnsafe(entity, value);
				}
				catch (ClassCastException e) {
					if (!decodingContext.getDecodingLenience().isPropertyClassCastExceptionLenient())
						throw e;
				}
			}
		}
		else if (!decodingContext.getDecodingLenience().isPropertyLenient())
			throw new MarshallException("unknown property: " + entityType.getTypeSignature() + "." + propertyName);
	}
	
	@Override
	public void begin(Attributes attributes) throws MarshallException {
		try {
			String id = attributes.getValue("id");
			EntityRegistration registration = decodingContext.acquireEntity(id);
			if (registration.typeInfo != null) {
				entity = registration.entity;
				entityType = (EntityType<?>)registration.typeInfo.type;
				propertyAbsenceHelper = decodingContext.providePropertyAbsenceHelper();
			}
		}
		catch (Exception e) {
			throw new MarshallException("error while decoding entity", e);
		}
	}
	
	@Override
	public void end() throws MarshallException {
		if (propertyAbsenceHelper != null)
			propertyAbsenceHelper.ensureAbsenceInformation(entityType, entity);
		
		parent.notifyValue(this, entity);
	}

	
}
