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
package com.braintribe.model.generic.path;

import java.util.ListIterator;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.api.IModelPathElement;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;

@SuppressWarnings("unusable-by-js")
public abstract class ModelPathElement implements Iterable<ModelPathElement>, IModelPathElement {
	private Object value;
	private GenericModelType type;
	private ModelPathNode node;
	private int depth;

	public ModelPathElement(GenericModelType type, Object value) {
		if (type instanceof BaseType && value != null) {
			BaseType baseType = (BaseType) type;
			type = baseType.getActualType(value);
			if (type == null)
				type = baseType;
		} else if (type instanceof EntityType && value != null) {
			type = GMF.getTypeReflection().getType(value);
		}

		this.type = type;
		this.value = value;
	}

	protected void onOrphaned() {
		this.node = null;
		this.depth = 0;
	}

	protected void onAdopt(ModelPathNode nodeParam) {
		if (this.node != null)
			// TODO update depth of all next elements
			removeFromPath();

		this.node = nodeParam;
		// TODO REVIEW with Dirk
		this.depth = nodeParam.previous.element == null ? 0 : nodeParam.previous.element.getDepth() + 1;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	protected ModelPathNode getNode() {
		if (node == null) {
			ModelPath path = new ModelPath();
			path.add(this);
		}

		return node;
	}

	public ModelPath getPath() {
		return getNode().path;
	}

	public ModelPathElement getNext() {
		ListIterator<ModelPathElement> it = iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public ModelPathElement getPrevious() {
		ListIterator<ModelPathElement> it = iterator();
		return it.hasPrevious() ? it.previous() : null;
	}

	@Override
	public ListIterator<ModelPathElement> iterator() {
		return getNode().path.listIterator(node.previous);
	}

	public ListIterator<ModelPathElement> iteratorAfter() {
		return getNode().path.listIterator(node);
	}

	@Override
	public <T> T getValue() {
		return (T) value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public <T extends GenericModelType> T getType() {
		return (T) type;
	}

	public void setType(GenericModelType type) {
		this.type = type;
	}

	public abstract ModelPathElementType getPathElementType();

	public <T extends ModelPathElement> T append(T modelPathElement) {
		iteratorAfter().add(modelPathElement);

		return modelPathElement;
	}

	public <T extends ModelPathElement> T prepend(T modelPathElement) {
		iterator().add(modelPathElement);

		return modelPathElement;
	}

	public void removeFromPath() {
		if (node != null) {
			ListIterator<ModelPathElement> it = iterator();
			it.next();
			it.remove();
		}
	}

	public abstract ModelPathElement copy();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.getTypeSignature().hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelPathElement other = (ModelPathElement) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.getTypeSignature().equals(other.type.getTypeSignature()))
			return false;
		if (value instanceof Set<?> && this instanceof PropertyPathElement) {
			if (!(other instanceof PropertyPathElement)) {
				return false;
			}
			if (((PropertyPathElement) this).getEntity() != ((PropertyPathElement) other).getEntity())
				return false;
			if (!((PropertyPathElement) this).getProperty().getName().equals(((PropertyPathElement) other).getProperty().getName()))
				return false;
		} else {
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
		}
		return true;
	}
}
