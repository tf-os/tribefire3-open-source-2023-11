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
package com.braintribe.wire.impl.compile;

import com.braintribe.asm.Opcodes;

class MethodDescriptorBuilderImpl implements MethodDescriptorBuilder {
	MethodDescriptorImpl methodDescriptor = new MethodDescriptorImpl();
	
	public MethodDescriptorBuilderImpl(TypeInfo owner, String name, int opcode) {
		methodDescriptor.owner = owner;
		methodDescriptor.name = name;
		methodDescriptor.opcode = opcode;
	}
	
	public MethodDescriptorBuilderImpl(TypeInfo owner, String name) {
		methodDescriptor.owner = owner;
		methodDescriptor.name = name;
		methodDescriptor.opcode = owner.isInterface? Opcodes.INVOKEINTERFACE: Opcodes.INVOKEVIRTUAL;
	}

	@Override
	public MethodDescriptorBuilder par(TypeInfo typeInfo) {
		methodDescriptor.parameters.add(typeInfo);
		return this;
	}

	@Override
	public MethodDescriptorBuilder par(Class<?> type) {
		methodDescriptor.parameters.add(new TypeInfo(type));
		return this;
	}

	@Override
	public MethodDescriptor ret(Class<?> type) {
		methodDescriptor.returnType = new TypeInfo(type);
		return methodDescriptor;
	}

	@Override
	public MethodDescriptor ret(TypeInfo typeInfo) {
		methodDescriptor.returnType = typeInfo;
		return methodDescriptor;
	}

	@Override
	public MethodDescriptorBuilder opcode(int opcode) {
		methodDescriptor.opcode = opcode;
		return this;
	}
}
