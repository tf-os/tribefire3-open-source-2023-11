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
package com.braintribe.codec.marshaller.sax;

import org.xml.sax.Attributes;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

class EntityDecoder extends ValueDecoder {
	private GenericEntity entity;
	private EntityType<GenericEntity> entityType;
	private PropertyAbsenceHelper propertyAbsenceHelper;
	private String ref;
	
	@Override
	public void end(DecodingContext context) throws MarshallException {
		if (propertyAbsenceHelper != null)
			propertyAbsenceHelper.ensureAbsenceInformation(entityType, entity);
	}

	@Override
	public void appendCharacters(char[] characters, int s, int l) {
		
	}
	
	@Override
	public Object getValue(DecodingContext context) {
		if (ref != null) {
			return context.lookupEntity(ref);
		}
		else {
			return entity;
		}
	}
	
	@Override
	public void onDescendantEnd(DecodingContext context, Decoder decoder)
			throws MarshallException {
		PropertyDecoder propertyDecoder = (PropertyDecoder)decoder;
		
		Property property = entityType.findProperty(propertyDecoder.propertyName);
		
		if (property != null) {
			propertyAbsenceHelper.addPresent(property);
			if (propertyDecoder.absent) {
				property.setAbsenceInformation(entity, (AbsenceInformation)propertyDecoder.value);
			}
			else {
				property.set(entity, propertyDecoder.value);
			}
		}
	}
	
	@Override
	public void begin(DecodingContext context, Attributes attributes)
			throws MarshallException {
		
		
		String ref = attributes.getValue("ref");
		if (ref != null && ref.length() > 0) {
			this.ref = ref;
		}
		else {
			try {
				String typeName = attributes.getValue("type");

				if (typeName == null || typeName.length() == 0)
					throw new MarshallException("missing type attribute");
				
				entityType = GMF.getTypeReflection().getEntityType(typeName);

				entity = context.createRaw(entityType);
				propertyAbsenceHelper = context.providePropertyAbsenceHelper();

				String idString = attributes.getValue("id");

				if (idString != null && idString.length() > 0) {
					context.register(entity, idString);
				}
			}
			catch (Exception e) {
				throw new MarshallException("error while decoding entity", e);
			}
		}

	}
}
