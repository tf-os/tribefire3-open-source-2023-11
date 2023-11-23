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
package com.braintribe.gwt.gmview.client.parse;

import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ParserArgument {
	
	private String value;
	private TypeCondition typeCondition;
	private int limit;
	private int offset;
	private PersistenceGmSession gmSession;
	private boolean simplifiedAssignment;

	public ParserArgument(String value, TypeCondition typeCondition) {
		this.value = value;
		this.typeCondition = typeCondition;
	}
	
	public ParserArgument(String value, TypeCondition typeCondition, int limit, int offset, PersistenceGmSession gmSession) {
		this.value = value;
		this.typeCondition = typeCondition;
		this.limit = limit;
		this.offset = offset;
		this.gmSession = gmSession;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public TypeCondition getTypeCondition() {
		return typeCondition;
	}

	public void setTypeCondition(TypeCondition typeCondition) {
		this.typeCondition = typeCondition;
	}

	public boolean hasValue() {
		return value != null && !value.trim().isEmpty();
	}
	
	public int getLimit() {
		return limit;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	public boolean isSimplifiedAssignment() {
		return simplifiedAssignment;
	}
	
	public void setSimplifiedAssignment(boolean simplifiedAssignment) {
		this.simplifiedAssignment = simplifiedAssignment;
	}
	
}
