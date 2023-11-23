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
public class GenericAsmClass implements AsmType {

	public static final GenericAsmClass voidType = new GenericAsmClass(AsmClassPool.voidType);

	private AsmClass asmClass;
	private AsmType[] genericParameters;
	private String signature;

	public GenericAsmClass(AsmClass asmClass) {
		set(asmClass, (GenericAsmClass[]) null);
	}

	public GenericAsmClass(AsmClass asmClass, AsmType... genericParameters) {
		if (asmClass.isPrimitive()) {
			throw new IllegalArgumentException("Primitive type cannot be used with generics. Type: " + asmClass);
		}

		set(asmClass, genericParameters);
	}

	private void set(AsmClass asmClass, AsmType[] genericParameters) {
		this.asmClass = asmClass;
		this.genericParameters = genericParameters;
	}

	public AsmType[] getGenericParameters() {
		return genericParameters;
	}

	@Override
	public String genericSignatureOrNull() {
		return getInternalNameLonger();
	}

	@Override
	public String getInternalNameLonger() {
		if (signature == null)
			if (genericParameters == null)
				signature = asmClass.getInternalNameLonger();
			else
				signature = buildGenericSignature();

		return signature;
	}

	@Override
	public AsmClass getRawType() {
		return asmClass;
	}

	@Override
	public Class<?> getJavaType() {
		return asmClass.getJavaType();
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	private String buildGenericSignature() {
		String s = asmClass.getInternalNameLonger();
		s = s.substring(0, s.length() - 1); // remove the ';' at the end
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		sb.append("<");
		for (AsmType gp : genericParameters)
			sb.append(gp.getInternalNameLonger());

		sb.append(">;");

		return sb.toString();
	}

}
