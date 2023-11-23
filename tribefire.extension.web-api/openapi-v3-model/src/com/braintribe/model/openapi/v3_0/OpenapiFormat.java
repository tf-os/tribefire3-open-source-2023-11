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
package com.braintribe.model.openapi.v3_0;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#dataTypeFormat
 *
 * @see OpenapiType
 */
public enum OpenapiFormat implements EnumBase, StringRepresented {
	INT32("int32"), //
	INT64("int64"), //
	FLOAT("float"), //
	DOUBLE("double"), //
	BYTE("byte"), //
	BINARY("binary"), //
	DATE("date"), //
	DATE_TIME("date-time"), //
	PASSWORD("password");

	public static final EnumType T = EnumTypes.T(OpenapiFormat.class);

	@Override
	public EnumType type() {
		return T;
	}

	String stringRepresentation;

	private static Map<String, OpenapiFormat> stringToFormat;

	private OpenapiFormat(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}

	public static OpenapiFormat parse(String formatString) {
		if (stringToFormat == null) {
			Map<String, OpenapiFormat> map = new HashMap<>();

			Stream.of(OpenapiFormat.values()).forEach(v -> map.put(v.stringRepresentation(), v));

			stringToFormat = map;
		}

		return stringToFormat.get(formatString);
	}

	@Override
	public String stringRepresentation() {
		return stringRepresentation;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
