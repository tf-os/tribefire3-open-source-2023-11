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
package com.braintribe.model.processing.traversing.impl.visitors;

import com.braintribe.model.generic.path.api.IListItemModelPathElement;
import com.braintribe.model.generic.path.api.IMapKeyModelPathElement;
import com.braintribe.model.generic.path.api.IMapValueModelPathElement;
import com.braintribe.model.generic.path.api.IPropertyModelPathElement;
import com.braintribe.model.generic.path.api.IRootModelPathElement;
import com.braintribe.model.generic.path.api.ISetItemModelPathElement;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

public abstract class ElementTypeOrientedVisitor implements GmTraversingVisitor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		switch (pathElement.getElementType()) {
		case ListItem:
			onListItemEnter(context, (IListItemModelPathElement) pathElement);
			break;
		case MapKey:
			onMapKeyEnter(context, (IMapKeyModelPathElement) pathElement);
			break;
		case MapValue:
			onMapValueEnter(context, (IMapValueModelPathElement) pathElement);
			break;
		case Property:
			onPropertyEnter(context, (IPropertyModelPathElement) pathElement);
			break;
		case Root:
			onRootEnter(context, (IRootModelPathElement) pathElement);
			break;
		case SetItem:
			onSetItemEnter(context, (ISetItemModelPathElement) pathElement);
			break;
		default:
			throw new GmTraversingException("Unknown element type: " + pathElement.getElementType());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onElementLeave(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		switch (pathElement.getElementType()) {
		case ListItem:
			onListItemLeave(context, (IListItemModelPathElement) pathElement);
			break;
		case MapKey:
			onMapKeyLeave(context, (IMapKeyModelPathElement) pathElement);
			break;
		case MapValue:
			onMapValueLeave(context, (IMapValueModelPathElement) pathElement);
			break;
		case Property:
			onPropertyLeave(context, (IPropertyModelPathElement) pathElement);
			break;
		case Root:
			onRootLeave(context, (IRootModelPathElement) pathElement);
			break;
		case SetItem:
			onSetItemLeave(context, (ISetItemModelPathElement) pathElement);
			break;
		default:
			throw new GmTraversingException("Unknown element type: " + pathElement.getElementType());
		}
	}

	@SuppressWarnings("unused")
	protected void onPropertyEnter(GmTraversingContext context, IPropertyModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onPropertyLeave(GmTraversingContext context, IPropertyModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onRootEnter(GmTraversingContext context, IRootModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onRootLeave(GmTraversingContext context, IRootModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onListItemEnter(GmTraversingContext context, IListItemModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onListItemLeave(GmTraversingContext context, IListItemModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onSetItemEnter(GmTraversingContext context, ISetItemModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onSetItemLeave(GmTraversingContext context, ISetItemModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onMapKeyEnter(GmTraversingContext context, IMapKeyModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onMapKeyLeave(GmTraversingContext context, IMapKeyModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onMapValueEnter(GmTraversingContext context, IMapValueModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onMapValueLeave(GmTraversingContext context, IMapValueModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

}
