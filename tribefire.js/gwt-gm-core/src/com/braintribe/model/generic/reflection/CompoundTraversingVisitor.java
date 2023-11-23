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
package com.braintribe.model.generic.reflection;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link TraversingVisitor} implementation that passes the {@link TraversingContext} to its
 * {@link #setDelegates(List) delegates}.
 * 
 * @author michael.lafite
 */
public class CompoundTraversingVisitor implements TraversingVisitor {

	private List<TraversingVisitor> delegates;

	public CompoundTraversingVisitor() {
		// nothing to do;
	}

	public CompoundTraversingVisitor(List<TraversingVisitor> delegates) {
		this.delegates = delegates;
	}
	
	public CompoundTraversingVisitor(TraversingVisitor... delegates) {
		this.delegates = Arrays.asList(delegates);
	}

	public List<TraversingVisitor> getDelegates() {
		return this.delegates;
	}

	public void setDelegates(List<TraversingVisitor> delegates) {
		this.delegates = delegates;
	}

	@Override
	public void visitTraversing(TraversingContext traversingContext) {
		if (this.delegates != null) {
			for (TraversingVisitor delegate : this.delegates) {
				delegate.visitTraversing(traversingContext);
			}
		}
	}
}
