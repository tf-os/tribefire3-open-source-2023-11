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
import com.braintribe.codec.marshaller.stax.EntityRegistrationListener;
import com.braintribe.codec.marshaller.stax.PropertyAbsenceHelper;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;

public class PropertyDecoder extends Decoder {
	public Object value;
	public boolean absent;
	private GenericEntity entity;
	private PropertyAbsenceHelper propertyAbsenceHelper;
	private PropertyDecoderPreparation preparation;
	private Decoder valueDecoder;
	
	public PropertyDecoder(GenericEntity entity, PropertyAbsenceHelper propertyAbsenceHelper, PropertyDecoderPreparation preparation) {
		this.entity = entity;
		this.propertyAbsenceHelper = propertyAbsenceHelper;
		this.preparation = preparation;
	}
	
	public Decoder getValueDecoder() {
		if (valueDecoder == null) {
			
		}
		return valueDecoder;
	}
	
	@Override
	public Decoder newDecoder(DecoderFactoryContext context, String elementName, Attributes attributes) throws MarshallException {
		return preparation.valueDecoderFactory.newDecoder(context, elementName, attributes);
	}
	
	@Override
	public void begin(Attributes attributes)
			throws MarshallException {
		absent = Boolean.parseBoolean(attributes.getValue("absent"));
		propertyAbsenceHelper.addPresent(preparation.property);
	}
	
	@Override
	public void notifyForwardEntity(Decoder origin, String referenceId) {
		decodingContext.addEntityRegistrationListener(referenceId, new EntityRegistrationListener() {
			@Override
			public void onEntityRegistered(GenericEntity entity) throws MarshallException {
				notifyValue(null, entity);
			}
		});
	}
	
	@Override
	public void notifyValue(Decoder origin, Object value) {
		if (absent) {
			preparation.property.setAbsenceInformation(entity, (AbsenceInformation)value);
		}
		else {
			try {
				preparation.property.setDirectUnsafe(entity, value);
			}
			catch (ClassCastException e) {
				if (!decodingContext.getDecodingLenience().isPropertyClassCastExceptionLenient())
					throw e;
			}
		}
	}
}
