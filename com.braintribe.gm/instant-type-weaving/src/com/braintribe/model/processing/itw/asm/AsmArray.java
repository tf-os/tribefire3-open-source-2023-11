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
package com.braintribe.model.processing.itw.asm;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 
 */
public class AsmArray extends AsmClass {

	private final AsmClass componentType;

	public AsmArray(AsmClass componentType) {
		super(createArrayClassName(componentType), componentType.classPool);
		this.componentType = componentType;
	}

	public static String createArrayClassName(AsmClass componentType) {
		return "[" + componentType.getInternalNameLonger();
	}

	@Override
	public AsmMethod getMethod(String name, AsmClass returnType, AsmClass... params) {
		throw new UnsupportedOperationException("Method 'AsmArray.getMethod' is not supported!");
	}

	@Override
	protected AsmMethod getMethod(MethodSignature ms, AsmClass returnType) {
		throw new UnsupportedOperationException("Method 'AsmArray.getMethod' is not supported!");
	}

	@Override
	public AsmClass getDeclaringClass() {
		return null;
	}

	@Override
	public List<AsmClass> getDeclaredClasses() {
		return Collections.emptyList();
	}

	@Override
	public Class<?> getJavaType() {
		return Array.newInstance(componentType.getJavaType(), 0).getClass();
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public AsmClass getSuperClass() {
		return null;
	}

	@Override
	public List<AsmClass> getInterfaces() {
		return null;
	}

	@Override
	public Stream<AsmMethod> getMethods() {
		return Stream.empty();
	}

	@Override
	public Stream<AsmField> getFields() {
		return Stream.empty();
	}

	public AsmClass getComponentType() {
		return componentType;
	}

	@Override
	public String toString() {
		return componentType.toString() + "[]";
	}

}
