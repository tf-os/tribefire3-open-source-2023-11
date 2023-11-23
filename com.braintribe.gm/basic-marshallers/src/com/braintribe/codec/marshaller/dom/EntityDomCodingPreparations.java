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
package com.braintribe.codec.marshaller.dom;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.coder.DomCoder;
import com.braintribe.codec.marshaller.dom.coder.DomCoders;
import com.braintribe.codec.marshaller.dom.coder.collection.ListDomCoder;
import com.braintribe.codec.marshaller.dom.coder.collection.MapDomCoder;
import com.braintribe.codec.marshaller.dom.coder.collection.SetDomCoder;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.entity.PropertyDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.scalar.EnumDomCoder;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public class EntityDomCodingPreparations {
	private Map<EntityType<?>, EntityDomCodingPreparation> preparations = new ConcurrentHashMap<EntityType<?>, EntityDomCodingPreparation>();
	
	
	public EntityDomCodingPreparation acquireEntityDomCodingPreparation(EntityType<?> entityType) throws CodecException {
		EntityDomCodingPreparation preparation = preparations.get(entityType);
		
		if (preparation == null) {
			List<Property> properties = entityType.getProperties();
			
			PropertyDomCodingPreparation propertyPreparations[] = new PropertyDomCodingPreparation[properties.size()];
			int index = 0;
			for (Property property: properties) {
				@SuppressWarnings("unchecked")
				DomCoder<Object> domCoder = (DomCoder<Object>) acquireDomCoder(property.getType());
				PropertyDomCodingPreparation propertyPreparation = new PropertyDomCodingPreparation();
				propertyPreparation.property = property;
				propertyPreparation.valueCoder = domCoder; 
				propertyPreparations[index++] = propertyPreparation;
			}
			preparation = new EntityDomCodingPreparation(propertyPreparations);
			preparations.put(entityType, preparation);
		}
		
		return preparation;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private DomCoder<?> acquireDomCoder(GenericModelType type) throws CodecException {
		switch (type.getTypeCode()) {
		// simple scalar types
		case stringType: return DomCoders.stringCoder;
		case booleanType: return DomCoders.booleanCoder; 
		case dateType: return DomCoders.dateCoder;
		case decimalType: return DomCoders.decimalCoder;
		case doubleType: return DomCoders.doubleCoder;
		case floatType: return DomCoders.floatCoder;
		case integerType: return DomCoders.integerCoder;
		case longType: return DomCoders.longCoder;
		
		// custom types
		case enumType: return new EnumDomCoder((EnumType)type);
		case entityType: return DomCoders.entityReferenceCoder;

		// object type
		case objectType: return DomCoders.objectCoder;
		
		// collection types
		case mapType: 
			GenericModelType[] parameterization = ((CollectionType)type).getParameterization();
			
			return new MapDomCoder<Object, Object>(
				(DomCoder<Object>)acquireDomCoder(parameterization[0]),
				(DomCoder<Object>)acquireDomCoder(parameterization[1]), true);
			
		case setType:			
			return new SetDomCoder<Object>(
				(DomCoder<Object>)acquireDomCoder(((CollectionType)type).getCollectionElementType()), true);
		case listType: 
			return new ListDomCoder<Object>(
					(DomCoder<Object>)acquireDomCoder(((CollectionType)type).getCollectionElementType()), true);
		default:
			throw new CodecException("unsupported GenericModelType " + type);
		} 
	}


}
