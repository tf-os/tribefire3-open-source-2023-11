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
package com.braintribe.model.processing.traversing.engine.impl;

import com.braintribe.model.processing.traversing.api.GmTraversingEvent;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

public abstract class GmTraversingEventImpl implements GmTraversingEvent {
	public GmTraversingEventImpl next;
	public GmTraversingEventImpl parent;
	private boolean skipDescendants;
	protected TraversingModelPathElement pathElement;

	public GmTraversingEventImpl(TraversingModelPathElement pathElement, GmTraversingEventImpl parent) {
		super();
		this.pathElement = pathElement;
		this.parent = parent;
	}

	public abstract boolean isEnter();

	@Override
	public TraversingModelPathElement getPathElement() {
		return pathElement;
	}

	public void setSkipDescendants(boolean skipDescendants) {
		this.skipDescendants = skipDescendants;
	}

	public GmTraversingEventImpl getParent() {
		return parent;
	}

	public boolean isSkippedByParent() {
		if (parent == null) {
			return false;
		} else {
			return parent.skipDescendants;
		}
	}
}
