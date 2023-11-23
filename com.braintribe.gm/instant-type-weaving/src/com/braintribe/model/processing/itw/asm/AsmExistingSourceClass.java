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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 
 */
public class AsmExistingSourceClass extends AsmLoadableClass {

	private final AsmClass superClass;
	private final List<AsmClass> interfaces;

	public AsmExistingSourceClass(String name, AsmClass superClass, List<AsmClass> interfaces, byte[] bytes, AsmClassPool classPool) {
		super(name, classPool);
		this.superClass = superClass;
		this.interfaces = interfaces;
		this.bytes = bytes;
	}

	@Override
	public AsmClass getSuperClass() {
		return superClass;
	}

	@Override
	public List<AsmClass> getInterfaces() {
		return interfaces;
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public AsmMethod getMethod(String name, AsmClass returnType, AsmClass... params) {
		throw new UnsupportedOperationException("Method 'AsmExistingSourceClass.getMethod' is not implemented yet!");
	}

	@Override
	protected AsmMethod getMethod(MethodSignature ms, AsmClass returnType) {
		throw new UnsupportedOperationException("Method 'AsmExistingSourceClass.getMethod' is not implemented yet!");
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
	public Stream<AsmMethod> getMethods() {
		throw new UnsupportedOperationException("Method 'AsmExistingSourceClass.getDeclaredMethods' is not implemented yet!");
	}

	@Override
	public Stream<AsmField> getFields() {
		// TODO fix this
		return Stream.empty();
		
	}

	
}
