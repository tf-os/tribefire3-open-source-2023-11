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
package com.braintribe.xml.parser.experts;

import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.xml.parser.registry.ContentExpertFactory;

public class CollectionExpertFactory<T> extends ComplexEntityExpertFactory implements ContentExpertFactory{
	private String collection;

	/*
	public CollectionExpertFactory(EntityType<GenericEntity> type, String collection, String property) {
		super( type, property);
		this.collection = collection;
	}
   */
	public CollectionExpertFactory(String signature, String collection, String property, Map<String, Codec<GenericEntity, String>> codecs) {
		super( signature, property, codecs);
		this.collection = collection;
	}
	public CollectionExpertFactory(String signature, String collection, String property) {
		super( signature, property, null);
		this.collection = collection;
	}

	@Override
	public ContentExpert newInstance() {
		if (type != null) {
			return new CollectionExpert<T>(type, collection, property, codecs);
		}
		else {
			return new CollectionExpert<T>(signature, collection, property, codecs);
		}
	}
	
	
	

}
