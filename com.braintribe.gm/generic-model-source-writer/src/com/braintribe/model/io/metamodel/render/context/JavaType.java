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
package com.braintribe.model.io.metamodel.render.context;

/**
 * @author peter.gazdik
 */
public class JavaType {

	public final String rawType;
	public final String keyType;
	public final String valueType;

	public final boolean isPrimitive;

	public static JavaType booleanType = new JavaType("boolean", true);
	public static JavaType intType = new JavaType("int", true);
	public static JavaType doubleType = new JavaType("double", true);
	public static JavaType floatType = new JavaType("float", true);
	public static JavaType longType = new JavaType("long", true);
	public static JavaType shortType = new JavaType("short", true);
	public static JavaType byteType = new JavaType("byte", true);
	public static JavaType charType = new JavaType("char", true);

	public JavaType(String rawType) {
		this(rawType, null, null);
	}

	public JavaType(String rawType, String elementType) {
		this(rawType, null, elementType);
	}

	public JavaType(String rawType, String keyType, String valueType) {
		this(rawType, keyType, valueType, false);
	}

	private JavaType(String rawType, boolean isPrimitive) {
		this(rawType, null, null, isPrimitive);
	}

	public JavaType(String rawType, String keyType, String valueType, boolean isPrimitive) {
		this.rawType = rawType;
		this.keyType = keyType;
		this.valueType = valueType;
		this.isPrimitive = isPrimitive;
	}

}
