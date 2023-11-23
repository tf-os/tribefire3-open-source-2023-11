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
package com.braintribe.model.processing.traversing.api.path;

import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * @author dirk.scheffler
 * @author pit.steinlin
 * @author peter.gazdik
 */
public abstract class TraversingModelPathElement implements IModelPathElement {

	private final TraversingModelPathElement previous;
	protected Object value;
	protected GenericModelType type;
	private Object customValue;
	private Object sharedCustomValue;
	protected int depth;

	public TraversingModelPathElement(TraversingModelPathElement previous, Object value, GenericModelType type) {
		this.previous = previous;
		this.value = value;
		this.type = type;
		this.depth = previous != null ? previous.getDepth() + 1 : 0;
	}
	
	protected TraversingModelPathElement(TraversingModelPathElement previous) {
		this.previous = previous;
	}

	@Override
	public TraversingModelPathElement getPrevious() {
		return previous;
	}

	@Override
	public <T> T getValue() {
		return (T) value;
	}
	
	@Override
	public <T extends GenericModelType> T getType() {
		return (T) type;
	}

	@Override
	public abstract ModelPathElementType getElementType();

	public <T> T getCustomValue() {
		return (T) customValue;
	}

	public void setCustomValue(Object customValue) {
		this.customValue = customValue;
	}

	public <T> T getSharedCustomValue() {
		return (T) sharedCustomValue;
	}

	public void setSharedCustomValue(Object sharedCustomValue) {
		this.sharedCustomValue = sharedCustomValue;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}
	
	
}
