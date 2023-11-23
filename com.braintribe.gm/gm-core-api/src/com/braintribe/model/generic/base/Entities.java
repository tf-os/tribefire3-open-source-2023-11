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
package com.braintribe.model.generic.base;

import java.util.Collection;
import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

/**
 * 
 * @author Dirk Scheffler
 * @implNote TODO: Move this to the upcoming With.entity() EntityApi
 */
public class Entities {
	/**
	 * Generates a string that contains the entity's type as well as the values of all it's properties, except for confidential ones. 
	 * Absent properties are simply "described" as absent and for collections only the collection type and size are used.
	 */
	public static String asString(GenericEntity genericEntity) {
		EntityType<?> entityType = genericEntity.entityType();

		StringBuilder builder = new StringBuilder();
		builder.append(entityType.getShortName());

		boolean enhanced = genericEntity instanceof EnhancedEntity;

		builder.append('[');
		builder.append('@');
		builder.append(System.identityHashCode(genericEntity));
		for (Property property : entityType.getProperties()) {
			builder.append(',');
			renderProperty(builder, genericEntity, enhanced, property);
		}
		builder.append(']');

		return builder.toString();
	}

	protected static void renderProperty(StringBuilder builder, GenericEntity genericEntity, boolean enhanced, Property property) {
		if (property.isConfidential())
			return;

		String propertyName = property.getName();

		builder.append(propertyName);
		builder.append('=');

		if (enhanced && property.isAbsent(genericEntity)) {
			builder.append("absent");
		} else {
			GenericModelType type = property.getType();
			Object value = property.get(genericEntity);

			if (value == null) {
				builder.append("null");
			} else {
				if (type instanceof BaseType)
					type = GMF.getTypeReflection().getType(value);

				if (type instanceof EntityType) {
					EntityType<?> valueEntityType = (EntityType<?>) type;
					builder.append(valueEntityType.getShortName());

					builder.append('[');
					builder.append('@');
					builder.append(System.identityHashCode(value));
					builder.append(',');
					/* we say false for enhanced (3rd parameter) cause id cannot be absent anyway */
					renderProperty(builder, (GenericEntity) value, false, valueEntityType.getIdProperty());
					builder.append(']');
				} else if (type instanceof EnumType) {
					String prefix = type.getJavaType().getSimpleName();
					builder.append(prefix + "." + value);
				} else if (type instanceof CollectionType) {
					CollectionType collectionType = (CollectionType) type;
					CollectionKind collectionKind = collectionType.getCollectionKind();
					builder.append(collectionKind);
					builder.append("[size=");
					builder.append(size(value, collectionKind));
					builder.append(']');
				} else {
					if (value instanceof String) {
						value = '"' + escape((String) value) + '"';
					}
					builder.append(value);
				}
			}
		}
	}

	private static int size(Object value, CollectionKind collectionKind) {
		switch (collectionKind) {
			case list:
			case set:
				return ((Collection<?>) value).size();
			case map:
				return ((Map<?, ?>) value).size();
		}

		throw new IllegalStateException("Unexpected collectionKind: " + collectionKind);
	}
	
	/**
	 * Escapes string content to be a valid string literal.
	 * 
	 * @return an escaped version of <code>unescaped</code>, suitable for being enclosed in double quotes in Java source
	 */
	private static String escape(String unescaped) {
		int extra = 0;
		for (int in = 0, n = unescaped.length(); in < n; ++in) {
			switch (unescaped.charAt(in)) {
				case '\0':
				case '\n':
				case '\r':
				case '\"':
				case '\\':
					++extra;
					break;
			}
		}

		if (extra == 0) {
			return unescaped;
		}

		char[] oldChars = unescaped.toCharArray();
		char[] newChars = new char[oldChars.length + extra];
		for (int in = 0, out = 0, n = oldChars.length; in < n; ++in, ++out) {
			char c = oldChars[in];
			switch (c) {
				case '\0':
					newChars[out++] = '\\';
					c = '0';
					break;
				case '\n':
					newChars[out++] = '\\';
					c = 'n';
					break;
				case '\r':
					newChars[out++] = '\\';
					c = 'r';
					break;
				case '\"':
					newChars[out++] = '\\';
					c = '"';
					break;
				case '\\':
					newChars[out++] = '\\';
					c = '\\';
					break;
			}
			newChars[out] = c;
		}

		return String.valueOf(newChars);
	}

}
