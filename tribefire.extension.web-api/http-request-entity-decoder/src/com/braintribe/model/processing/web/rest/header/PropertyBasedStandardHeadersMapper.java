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
package com.braintribe.model.processing.web.rest.header;

import static com.braintribe.model.processing.web.rest.HttpExceptions.badRequest;
import static com.braintribe.model.processing.web.rest.impl.HttpRequestEntityDecoderUtils.illegalArgument;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.web.rest.StandardHeadersMapper;

/**
 * Implementation of {@link StandardHeadersMapper} that takes a collection or properties, 
 * or a Map<String, Property> from header name to property.
 * 
 *
 * @param <T> the entity type.
 */
public class PropertyBasedStandardHeadersMapper<T extends GenericEntity> extends AbstractStandardHeadersMapper<T> {

	@FunctionalInterface
	private interface Setter {
		
		void set(HttpServletRequest request, String headerName, GenericEntity target);
	}

	private final Map<String, Setter> setters = new HashMap<>();

	/**
	 * Instantiate a new PropertyBasedStandardHeadersMapper with the given property collection.
	 * Checks property names and types during instantiation.
	 * 
	 * @param properties the properties to map, must not be {@code null}
	 */
	public PropertyBasedStandardHeadersMapper(Collection<Property> properties) {
		validateProperties(properties.stream().collect(Collectors.toMap(Property::getName, Function.identity())));
	}

	/**
	 * Instantiate a new PropertyBasedStandardHeadersMapper with the given property map.
	 * Checks the map's keys and properties' types during instantiation.
	 * 
	 * @param properties the properties to map, must not be {@code null}
	 */
	public PropertyBasedStandardHeadersMapper(Map<String, Property> properties) {
		validateProperties(properties);
	}

	@Override
	public final void assign(HttpServletRequest request, String headerName, T target) {
		Setter setter = setters.get(headerName);
		if(setter == null) {
			String propertyName = getPropertyNameFromStandardHeaderName(headerName);
			setter = setters.get(propertyName);
			if(setter != null) {
				setters.put(headerName, setter);
			}
		}
		
		if(setter == null) {
			return;
		}
		
		setter.set(request, headerName, target);
	}
	
	private void validateProperties(Map<String, Property> properties) {
		for(Map.Entry<String, Property> entry : properties.entrySet()) {
			Setter setter = getSetterFor(entry.getValue(), entry.getKey());
			
			setters.put(entry.getKey(), setter);
		}
	}

	private Setter getSetterFor(Property property, String headerOrPropertyName) {
		HeaderValueType type = HeaderValueTypes.WELL_KNOWN_HEADERS.getOrDefault(headerOrPropertyName, HeaderValueType.UNKOWN);

		switch(type) {
			case DATE:
				return getDateSetterFor(property);
			case INT:
				return getIntSetterFor(property);
			case STRING:
				return getStringSetterFor(property);
			case STRING_LIST:
				return getStringListSetterFor(property);
			default:
				throw new IllegalArgumentException("The property " + property.getName() + " does not correspond to a standard header.");
		}
	}

	private Setter getDateSetterFor(Property property) {
		if(property.getType().getTypeCode() != TypeCode.dateType) {
			illegalArgument("Property %s of entity %s should be of type date but is of type %s.", 
					property.getName(), property.getDeclaringType().getTypeName(), property.getType().getTypeName());
		}

		return (HttpServletRequest request, String headerName, GenericEntity target) -> {
			try {
				Date date = new Date(request.getDateHeader(headerName));
				property.set(target, date);
			} catch (IllegalArgumentException e) {
				badRequest("Cannot parse header parameter %s as date, value: %s", headerName, request.getHeader(headerName));
			}
		};
	}
	
	private Setter getIntSetterFor(Property property) {
		if(property.getType().getTypeCode() != TypeCode.integerType) {
			illegalArgument("Property %s of entity %s should be of type integer but is of type %s.", 
					property.getName(), property.getDeclaringType().getTypeName(), property.getType().getTypeName());
		}
		
		return (HttpServletRequest request, String headerName, GenericEntity target) -> {
			Enumeration<String> values = request.getHeaders(headerName);
			boolean valueSet = false;
			while(values.hasMoreElements()) {
				if(valueSet) {
					badRequest("Only one value should be set for header parameter %s", headerName);
				}
				valueSet = true;
				
				String value = values.nextElement();
				try {
					property.set(target, Integer.parseInt(value));
				} catch (NumberFormatException e){
					badRequest("Invalid value for header %s, expected integer value but got %s", headerName, value);
				}
			}
		};
	}
	
	private Setter getStringSetterFor(Property property) {
		if(property.getType().getTypeCode() != TypeCode.stringType) {
			illegalArgument("Property %s of entity %s should be of type string but is of type %s.", 
					property.getName(), property.getDeclaringType().getTypeName(), property.getType().getTypeName());
		}
		
		return (HttpServletRequest request, String headerName, GenericEntity target) -> {
			Enumeration<String> values = request.getHeaders(headerName);
			boolean valueSet = false;
			while(values.hasMoreElements()) {
				if(valueSet) {
					badRequest("Only one value should be set for header parameter %s", headerName);
				}
				valueSet = true;

				property.set(target, values.nextElement());
			}
		};
	}
	
	private Setter getStringListSetterFor(Property property) {
		if(!property.getType().isCollection()) {
			illegalArgument("Property %s of entity %s should be of type List<String> or Set<String> but is of type %s.", 
					property.getName(), property.getDeclaringType().getTypeName(), property.getType().getTypeName());
		}
		
		CollectionType type = (CollectionType) property.getType();
		if(type.getCollectionElementType().getTypeCode() != TypeCode.stringType) {
			illegalArgument("Property %s of entity %s should be of type list<string> or set<string> but is of type %s<%s>.", 
					property.getName(), property.getDeclaringType().getTypeName(), property.getType().getTypeName(), type.getTypeName());
		}
		
		LinearCollectionType collectionType = property.getType().cast();
		
		return (HttpServletRequest request, String headerName, GenericEntity target) -> {
			if(property.get(target) == null) {
				property.set(target, collectionType.createPlain());
			}
			Enumeration<String> values = request.getHeaders(headerName);
			
			Collection<String> propValue = property.get(target);
			while(values.hasMoreElements()) {
				String valueList = values.nextElement();
				Stream.of(valueList.split(",")).map(String::trim).filter(val -> !"".equals(val)).forEach(propValue::add);
			}
		};
	}
}
