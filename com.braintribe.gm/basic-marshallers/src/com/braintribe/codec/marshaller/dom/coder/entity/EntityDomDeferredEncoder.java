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
package com.braintribe.codec.marshaller.dom.coder.entity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.DomEncodingContext;
import com.braintribe.codec.marshaller.dom.TypeInfo;
import com.braintribe.codec.marshaller.dom.coder.DeferredEncoder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.Property;

public class EntityDomDeferredEncoder extends DeferredEncoder {
	private EntityDomCodingPreparation preparation;
	public GenericEntity entity;
	public String refId;
	private TypeInfo typeInfo;


	public EntityDomDeferredEncoder(EntityDomCodingPreparation preparation, TypeInfo typeInfo, GenericEntity entity, String refId) {
		this.preparation = preparation;
		this.entity = entity;
		this.refId = refId;
		this.typeInfo = typeInfo;
	}
	
	@Override
	public boolean continueEncode(DomEncodingContext context) throws CodecException {
		Document document = context.getDocument();
		Element entityElement = document.createElement("E");
		
		entityElement.setAttribute("t", typeInfo.as);
		entityElement.setAttribute("id", refId);
		
		GenericEntity _entity = this.entity;

		for (PropertyDomCodingPreparation propertyPreparation: preparation.propertyPreparations) {
			Property property = propertyPreparation.property;
			
			Object value = property.get(_entity);
			
			Element propertyElement = null;
			
			if (value == null) {
				AbsenceInformation absenceInformation = property.getAbsenceInformation(_entity);
				if (absenceInformation != null && context.shouldWriteAbsenceInformation()) {
					propertyElement = encodeAbsenceInformation(context, absenceInformation);
				} 
			}
			else {
				propertyElement = propertyPreparation.valueCoder.encode(context, value);
			}
			
			if (propertyElement != null) {
				propertyElement.setAttribute("p", property.getName());
				entityElement.appendChild(propertyElement);
			}
		}
		
		context.appendToPool(entityElement);
		
		return false;
	}
	
	
	private static Element encodeAbsenceInformation(DomEncodingContext context, AbsenceInformation absenceInformation) throws CodecException {
		Element element = context.getDocument().createElement("a");
		if (!context.isSimpleAbsenceInformation(absenceInformation)) {
			String text = context.lookupQualifiedId(absenceInformation);
			element.setTextContent(text);
		}
		return element;
	}

}
