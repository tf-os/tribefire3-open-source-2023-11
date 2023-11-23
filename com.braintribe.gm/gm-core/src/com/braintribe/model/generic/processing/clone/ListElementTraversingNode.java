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
package com.braintribe.model.generic.processing.clone;

import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.reflection.GenericModelType;

public class ListElementTraversingNode extends TraversingNode {
	private Object value;
	private GenericModelType type;
	
	public ListElementTraversingNode(GenericModelType type) {
		super();
		this.type = type;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public GenericModelType getType() {
		return type;
	}
	
	@Override
	public Object getValue() {
		return value;
	}
	

	@Override
	public CriterionType getCriterionType() {
		return CriterionType.LIST_ELEMENT;
	}

}
