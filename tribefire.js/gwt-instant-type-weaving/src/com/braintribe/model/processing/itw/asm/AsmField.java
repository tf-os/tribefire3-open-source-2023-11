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
public class AsmField implements AsmMember {

	private AsmClass declaringClass;
	private String name;
	private AsmType type;

	private AsmField(AsmClass owner, String name, AsmType type) {
		this.declaringClass = owner;
		this.name = name;
		this.type = type;
	}

	public static AsmField newExistingField(AsmExistingClass declaringClass, String name, AsmClass type) {
		return new AsmField(declaringClass, name, type);
	}

	public static AsmField newRegisteredField(AsmNewClass declaringClass, String name, AsmType type) {
		return declaringClass.notifyNewField(new AsmField(declaringClass, name, type));
	}

	@Override
	public AsmClass getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public String getName() {
		return name;
	}

	public AsmType getType() {
		return type;
	}

	public AsmClass getRawType() {
		return type.getRawType();
	}

}
