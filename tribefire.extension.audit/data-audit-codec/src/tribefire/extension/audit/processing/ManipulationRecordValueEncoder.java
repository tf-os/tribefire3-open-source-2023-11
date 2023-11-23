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
package tribefire.extension.audit.processing;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.utils.DateTools;

public class ManipulationRecordValueEncoder {
	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);
	
	public static String encode(GenericModelType inferredType, Object value) {
		StringBuilder builder = new StringBuilder();
		write(builder, value, inferredType);
		return builder.toString();
	}
	
	private static void write(StringBuilder builder, Object value, GenericModelType type) {
		if (value == null) {
			builder.append("null");
			return;
		}

		switch (type.getTypeCode()) {
		// base type
		case objectType:
			write(builder, value, type.getActualType(value));
			break;
			
		// scalar types
		case dateType:
			writeDateLiteral(builder, (Date)value);
			break;
		case stringType:
			writeStringLiteral(builder, (String)value);
			break;
		case booleanType:
			builder.append(value.toString());
			break;
		case decimalType:
			builder.append(value.toString());
			builder.append('B');
			break;
		case doubleType:
			builder.append(value.toString());
			break;
		case floatType:
			builder.append(value.toString());
			builder.append('F');
			break;
		case integerType:
			builder.append(value.toString());
			break;
		case longType:
			builder.append(value.toString());
			builder.append('L');
			break;

		// collections
		case setType:
			writeSetValue(builder, (CollectionType)type, (Collection<?>)value);
			break;
		case listType:
			writeListValue(builder, (CollectionType)type, (Collection<?>)value);
			break;
		case mapType:
			writeMapValue(builder, (CollectionType)type, (Map<?, ?>)value);
			break;
		
		// custom types
		case entityType:
			writeEntity(builder, (GenericEntity)value);
			break;

		case enumType:
			writeEnum(builder, (Enum<?>)value, type.<EnumType>cast());
			break;
		default:
			throw new CodecException("unsupported GenericModelType " + type.getClass());
		}
	}

	private static void writeEnum(StringBuilder builder, 
			Enum<?> value, EnumType enumType) {
		builder.append(enumType.getTypeSignature());
		builder.append("->");
		builder.append(value.name());
	}

	protected static void writeDateLiteral(StringBuilder builder, Date date) {
		String encodedDate = DateTools.encode(date, dateFormat);
		builder.append('@');
		builder.append(encodedDate);
	}

	protected static void writeStringLiteral(StringBuilder builder, String value) {
		builder.append('\'');
		writeEscaped(builder, value);
		builder.append('\'');
	}

	public static void writeEscaped(StringBuilder builder, String string) {
    	int len = string.length();
    	
    	int s = 0;
    	int i = 0;

    	for (; i < len; i++) {
    		char c = string.charAt(i);

    		if (c == '\\') {
    			builder.append(string, s, i);
    			builder.append("\\\\");
    			s = i + 1;
    		}
    		else if (c == '\'') {
    			builder.append(string, s, i);
    			builder.append("\\'");
    			s = i + 1;
    		}
    	}
    	if (i > s) {
    		if (s == 0)
    			builder.append(string);
    		else
    			builder.append(string, s, i);
    	}
	}
	
	public static void writeEscapedSlow(StringBuilder builder, String s) {
		// TODO better string escaping
		int c = s.length();

		for (int i = 0; i < c; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '\\':
				builder.append("\\\\");
				break;
			case '\'':
				builder.append("\\'");
				break;
			default:
				builder.append(ch);
			}
		}
	}

	protected static void writeListValue(StringBuilder builder, CollectionType collectionType, Collection<?> collection) {
		builder.append('[');
		GenericModelType elementType = collectionType.getCollectionElementType();
		
		boolean first = true;
		for (Object value: collection) {
			if (first) {
				first = false;
			}
			else {
				builder.append(',');
			}
			write(builder, value, elementType);
		}
		builder.append(']');
	}
	
	protected static void writeSetValue(StringBuilder builder, CollectionType collectionType, Collection<?> collection) {
		builder.append('(');
		GenericModelType elementType = collectionType.getCollectionElementType();
		
		boolean first = true;
		for (Object value: collection) {
			if (first) {
				first = false;
			}
			else {
				builder.append(',');
			}
			write(builder, value, elementType);
		}
		builder.append(')');
	}
	
	
	protected static void writeMapValue(StringBuilder builder, CollectionType collectionType, Map<?, ?> map) {
		builder.append('{');
		GenericModelType[] parameterization = collectionType.getParameterization();
		GenericModelType keyType = parameterization[0];
		GenericModelType valueType = parameterization[1];

		boolean first = true;
		for (Map.Entry<?, ?> entry: map.entrySet()) {
			if (first) {
				first = false;
			}
			else {
				builder.append(',');
			}
			Object key = entry.getKey();
			Object value = entry.getValue();
			write(builder, key, keyType);
			builder.append(':');
			write(builder, value, valueType);
		}
		builder.append('}');
	}
	
	protected static void writeEntity(StringBuilder builder, GenericEntity entity) {
		EntityReference reference = null;
		
		if (entity.isVd() && entity instanceof EntityReference) {
			reference = (EntityReference)entity;
		}
		else {
			reference = entity.reference();
		}

		if (reference.referenceType() == EntityReferenceType.preliminary) {
			builder.append('~');
		}
		
		builder.append(reference.getTypeSignature());
		builder.append('[');
		write(builder, reference.getRefId(), BaseType.INSTANCE);
		
		String partition = reference.getRefPartition();
		
		if (partition != null) {
			builder.append(',');
			writeStringLiteral(builder, partition);
		}
		
		builder.append(']');
	}
}
