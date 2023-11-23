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
package com.braintribe.model.processing.itw.synthesis.gm.template;

import java.util.List;
import java.util.Set;

import com.braintribe.asm.MethodVisitor;
import com.braintribe.model.weaving.ProtoGmProperty;

public class Variable implements TemplateNode {

	private final String variableName;
	private final List<ProtoGmProperty> nonOverlayProperties;

	public Variable(String variableName, List<ProtoGmProperty> nonOverlayProperties) {
		this.variableName = variableName;
		this.nonOverlayProperties = nonOverlayProperties;
	}

	@Override
	public void merge(MethodVisitor mv, VariableResolver variableResolver) {
		variableResolver.mergeVariable(variableName, nonOverlayProperties, mv);
	}

	@Override
	public void collectVariables(Set<Variable> variables) {
		variables.add(this);
	}

}
