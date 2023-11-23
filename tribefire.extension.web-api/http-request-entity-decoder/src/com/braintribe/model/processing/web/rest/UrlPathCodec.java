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
package com.braintribe.model.processing.web.rest;

import java.util.function.Supplier;

import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.web.rest.impl.UrlPathCodecImpl;

public interface UrlPathCodec<E extends GenericEntity> {

	static <E extends GenericEntity> UrlPathCodec<E> create() {
		return new UrlPathCodecImpl<>();
	}
	
	static <E extends GenericEntity> UrlPathCodec<E> create(EntityType<E> type) {
		return create();
	}
	
	UrlPathCodec<E> constantSegment(String segment);

	UrlPathCodec<E> mappedSegment(String propertyName);

	UrlPathCodec<E> mappedSegment(String propertyName, boolean optional);

	String encode(E value) throws CodecException;
	
	E decode(Supplier<E> entitySupplier, String encodedValue) throws CodecException;

}
