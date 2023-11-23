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
package com.braintribe.model.processing.web.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.processing.web.rest.UrlPathCodec;

public class UrlPathCodecImpl<E extends GenericEntity> implements UrlPathCodec<E> {

	public static final String IGNORE_SEGMENT_PREFIX = "-";

	private final List<String> segments = new ArrayList<>();

	private int optionalSegment = -1;
	
	@Override
	public UrlPathCodec<E> constantSegment(String segment) {
		segments.add(IGNORE_SEGMENT_PREFIX + segment);
		return this;
	}

	@Override
	public UrlPathCodec<E> mappedSegment(String propertyName) {
		return mappedSegment(propertyName, false);
	}

	@Override
	public UrlPathCodec<E> mappedSegment(String propertyName, boolean optional) {
		segments.add(propertyName);
		if (optional) {
			if (optionalSegment >= 0) {
				HttpExceptions.internalServerError("Only one optional segment allowed.");
			}
			optionalSegment = segments.size() - 1;
		}
		return this;
	}
	
	@Override
	public String encode(E entity) throws CodecException {
		StringBuilder urlPathBuilder = new StringBuilder();
		EntityType<GenericEntity> entityType = entity.entityType();
		
		for(String segment : segments) {
			if(segment.startsWith(IGNORE_SEGMENT_PREFIX)) {
				urlPathBuilder.append("/").append(segment.substring(IGNORE_SEGMENT_PREFIX.length()));
			} else {
				Property property = entityType.getProperty(segment);
				Object value = property.get(entity);
				if(value != null) {
					urlPathBuilder.append("/").append(value.toString());
				}
			}
		}
		
		return urlPathBuilder.substring(1).toString();
	}

	@Override
	public E decode(Supplier<E> entitySupplier, String urlPath) throws CodecException {
		E entity = entitySupplier.get();
		
		if (urlPath.startsWith("/")) {
			urlPath = urlPath.substring(1);
		}
		
		String[] path = urlPath.split("/");
		EntityType<GenericEntity> entityType = entity.entityType();
		
		int pathSize = path.length;
		int segmentsSize = segments.size();
		
		int pathIndex = 0;
		int segmentsIndex = 0;
		while (pathIndex < pathSize && segmentsIndex < segmentsSize) {
			String segment = segments.get(segmentsIndex);
			if (optionalSegment == segmentsIndex && pathSize < segmentsSize) {
				// skip this segment
				segmentsIndex++;
				continue;
			} else if(!segment.startsWith(IGNORE_SEGMENT_PREFIX)) {
				Property property = entityType.getProperty(segment);
				property.set(entity, path[pathIndex]);
			}
			pathIndex++;
			segmentsIndex++;
		}
		
		return entity;
	}

}
