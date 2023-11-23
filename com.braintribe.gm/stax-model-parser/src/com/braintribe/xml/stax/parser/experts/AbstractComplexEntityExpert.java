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
package com.braintribe.xml.stax.parser.experts;

import java.util.Collection;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

public abstract class AbstractComplexEntityExpert extends AbstractContentExpert {

	private static Logger log = Logger.getLogger(AbstractComplexEntityExpert.class);
	
	protected GenericEntity instance;
	protected EntityType<GenericEntity> type;
	protected Map<String, Codec<GenericEntity,String>> codecs;
	
	@Override
	public GenericEntity getInstance() {
		if (instance == null) {
			instance = type.create();
		}			
		return instance;
	}
	public void setInstance(GenericEntity instance) {
		this.instance = instance;
	}
	
	@Override
	public EntityType<GenericEntity> getType() {		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void attach(ContentExpert child) {
		Property property = type.getProperty( child.getProperty());
		if (property == null) {
			// honk
		}		
		Object value = child.getPayload();
		GenericModelType propertyType = property.getType();
		
		switch (propertyType.getTypeCode()) {
			// just to try, not used yet - currently handled by the real expert (as there's real element in the XML)
			case listType:
			case setType:								
				Collection collectionToAddTo = property.get(getInstance());
				GenericModelType elementType = ((CollectionType) propertyType).getParameterization()[0]; 
				collectionToAddTo.add( convertSingleValue(elementType, value));		
				break;
			default:
				property.setDirect(getInstance(), convertSingleValue(propertyType, value));
				break;
		}
					
	}

	@Override
	public Object getPayload() {
		return instance;
	}
	
	
	private Object convertSingleValue( GenericModelType propertyType, Object value) {
		switch (propertyType.getTypeCode()) {
		case booleanType:				
			value = Boolean.parseBoolean( (String) value);
			break;
		case integerType:
			value = Integer.parseInt( (String) value);
			break;
		case longType:
			value = Long.parseLong( (String) value);
			break;
		case doubleType:
			value = Double.parseDouble( (String) value);
			break;
		case floatType:
			value = Float.parseFloat( (String) value);
			break;			
		case dateType:
			try {
				value = new DateInterpreter().parseDate( (String) value);
			} catch (CodecException e) {
				log.warn("cannot interpret date from [" + value + "]");
				e.printStackTrace();
			}
			break;			
		case stringType:
			break;
		default:
			if (codecs != null) {
				Codec<GenericEntity,String> codec = codecs.get(propertyType.getTypeSignature());
				if (codec != null) {
					try {
						return codec.decode((String) value);
					} catch (CodecException e) {
						log.warn("codec for [" + propertyType.getTypeSignature() +"] cannot convert type passed",e);
					}
				}
			}
			/*
			// i don't think that this is relevant information unless it crashes.. then remove the comment again :-)
			if (log.isDebugEnabled()) {
				log.debug("complex type unhandled by codec passed [" + propertyType.getTypeSignature() +"], actual value returned");
			}
			*/
			break;
		}
		return value;
	}
}
