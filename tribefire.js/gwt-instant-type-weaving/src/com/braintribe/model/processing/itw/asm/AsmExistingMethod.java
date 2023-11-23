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

import java.lang.reflect.Method;

import com.braintribe.asm.Type;

/**
 * 
 */
public class AsmExistingMethod extends AsmMethod {

	private final Method method;

	public AsmExistingMethod(AsmClassPool classPool, Method method) {
		super(classPool);
		this.method = method;
	}

	public Method getExistingMethod() {
		return method;
	}

	@Override
	public String getName() {
		return method.getName();
	}

	@Override
	public String getReturnTypeName() {
		return Type.getDescriptor(method.getReturnType());
	}

	@Override
	public int getModifiers() {
		return method.getModifiers();
	}

	private AsmClass[] params;
	private AsmClass declaringClass;
	private AsmClass returnType;

	@Override
	public AsmClass[] getParams() {
		if (params == null) {
			params = AsmUtils.toAsmClasses(classPool, method.getParameterTypes());
		}
		return params;
	}

	@Override
	public AsmClass getDeclaringClass() {
		if (declaringClass == null) {
			declaringClass = classPool.get(method.getDeclaringClass());
		}

		return declaringClass;
	}

	@Override
	public AsmClass getReturnType() {
		if (returnType == null) {
			returnType = classPool.get(method.getReturnType());
		}

		return returnType;
	}

	public boolean isDefault() {
		return method.isDefault();
	}

	@Override
	public String toString() {
		return method.toString();
	}
}
