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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.DomDecodingContext;
import com.braintribe.codec.marshaller.dom.EntityRegistration;
import com.braintribe.codec.marshaller.dom.TypeInfo;
import com.braintribe.codec.marshaller.dom.coder.DeferredDecoder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;

public class EntityDomDeferredDecoder extends DeferredDecoder {

	private EntityType<?> entityType;
	private EntityDomCodingPreparation preparation;
	public GenericEntity entity;
	public String refId;
	public TypeInfo typeInfo;
	public Element element;
	
	public EntityDomDeferredDecoder(EntityType<?> entityType, EntityDomCodingPreparation preparation, Element element) {
		this.entityType = entityType;
		this.preparation = preparation;
		this.element = element;
	}
	
	@Override
	public boolean continueDecode(DomDecodingContext context) throws CodecException {
		String id = element.getAttribute("id");
		EntityRegistration registration = context.acquireEntity(id);
		GenericEntity _entity = registration.entity;
		
		PropertyAbsenceHelper propertyAbsenceHelper = context.providePropertyAbsenceHelper();
		
		Node node = element.getFirstChild();
		
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element propertyElement = (Element)node;
				String propertyName = propertyElement.getAttribute("p");
				PropertyDomCodingPreparation propertyPreparation = preparation.getPropertyCoderByName(propertyName);
				if (preparation != null) {
					propertyAbsenceHelper.addPresent(propertyPreparation.property);
					if (propertyElement.getTagName().equals("a")) {
						AbsenceInformation ai = decodeAbsenceInforamtion(context, propertyElement);
						if (ai != null) {
							// PGA: I am adding the null check because of incompatible changes. I do not know if 'ai' can every be null
							propertyPreparation.property.setAbsenceInformation(_entity, ai);
						}
					}
					else {
						Object value = propertyPreparation.valueCoder.decode(context, propertyElement);
						propertyPreparation.property.setDirectUnsafe(_entity, value);
					}
				}
				else 
					throw new CodecException("unkown property " + propertyName + " for type " + entityType);
			}
			node = node.getNextSibling();
		}
		
		propertyAbsenceHelper.ensureAbsenceInformation(entityType, _entity);
		return false;
	}
	
	private static AbsenceInformation decodeAbsenceInforamtion(DomDecodingContext context, Element element) throws CodecException {
		String text = element.getTextContent();
		if (text.isEmpty())
			return context.getAbsenceInformationForMissingProperties();
		else
			return (AbsenceInformation)context.acquireEntity(text).entity;
	}
	
}
