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
package com.braintribe.utils.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.braintribe.utils.template.TemplateException;

public class Sequence implements TemplateNode {
	private List<TemplateNode> nodes = new ArrayList<TemplateNode>();
	
	public void add(TemplateNode templateNode) {
		nodes.add(templateNode);
	}
	
	@Override
	public void merge(StringBuilder builder, MergeContext context)
			throws TemplateException {
		for (TemplateNode node: nodes) {
			node.merge(builder, context);
		}
	}
	
	@Override
	public void collectVariables(List<Variable> collections) {
		for (TemplateNode node: nodes) {
			node.collectVariables(collections);
		}
	}
	
	@Override
	public boolean walk(Predicate<TemplateNode> visitor) {
		if (!visitor.test(this))
			return false;
		
		for (TemplateNode node: nodes) {
			if (!node.walk(visitor))
				return false;
		}
		return true;
	}
}
