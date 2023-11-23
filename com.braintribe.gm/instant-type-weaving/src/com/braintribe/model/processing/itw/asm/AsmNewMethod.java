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

/**
 * 
 */
public class AsmNewMethod extends AsmMethod {

	private AsmClass declaringClass;
	private String name;
	private AsmClass returnType;
	private AsmClass[] params;
	private int modifiers;

	private static AsmClass[] EMPTY_PARAMS = new AsmClass[0];
	
	private AsmNewMethod(AsmNewClass declaringClass, String name, AsmClass returnType, AsmClass[] params, int modifiers) {
		super(returnType.classPool);
		this.declaringClass = declaringClass;
		this.name = name;
		this.returnType = returnType;
		if (params != null && params.length == 0) {
			this.params = EMPTY_PARAMS;
		} else {
			this.params = params;
		}
		this.modifiers = modifiers;
	}

	public static AsmNewMethod newRegisteredMethod(AsmNewClass declaringClass, String name, AsmClass returnType, AsmClass[] params,
			int modifiers) {
		return declaringClass.notifyNewMethod(new AsmNewMethod(declaringClass, name, returnType, params, modifiers));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AsmClass getReturnType() {
		return returnType;
	}

	@Override
	public String getReturnTypeName() {
		return returnType.getInternalName();
	}

	@Override
	public AsmClass[] getParams() {
		return params;
	}

	@Override
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public AsmClass getDeclaringClass() {
		return declaringClass;
	}

}
