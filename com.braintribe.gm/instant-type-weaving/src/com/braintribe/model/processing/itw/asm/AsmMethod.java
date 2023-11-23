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
public abstract class AsmMethod implements AsmMember {

	protected AsmClassPool classPool;
	protected String signature;

	public AsmMethod(AsmClassPool classPool) {
		this.classPool = classPool;
	}

	public String getSignature() {
		if (signature == null) {
			
			//The majority will be one of these:
			//()Lcom/braintribe/model/generic/reflection/EntityType;
			//(Ljava/lang/Object;)V
			//()Ljava/lang/Object;
			
			//Since the AsmMethod will live throughout the lifetime of tje JVM, we can
			//call signature.intern() to save a lot of memory space.

			signature = AsmUtils.createMethodSignature(getReturnType(), getParams()).intern();

		}

		return signature;
	}

	@Override
	public abstract AsmClass getDeclaringClass();

	public abstract String getReturnTypeName();

	public abstract AsmClass getReturnType();

	public abstract int getModifiers();

	public abstract AsmClass[] getParams();
}
