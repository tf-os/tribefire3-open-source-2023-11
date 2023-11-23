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
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

public class VirtualCollectionExpert<T> extends VirtualComplexEntityExpert {
	private String collection;
	
	public VirtualCollectionExpert(String collection, String property, Map<String, Codec<GenericEntity, String>> codecs) {		
		this.collection = collection;
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public void attach(ContentExpert child) {
		T value = (T) child.getPayload();
		Property collectionProperty = type.getProperty( collection);
		Collection<T> collectionToAddTo = collectionProperty.get(getInstance());
		collectionToAddTo.add( value);		
	}

	@Override
	public Object getPayload() {		
		return getInstance();
	}	
	

}
