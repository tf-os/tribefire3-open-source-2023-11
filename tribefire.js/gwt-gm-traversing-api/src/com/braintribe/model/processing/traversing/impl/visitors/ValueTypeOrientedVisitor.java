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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;

public abstract class ValueTypeOrientedVisitor implements GmTraversingVisitor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		if (pathElement.getValue() == null)
			return;
		
		GenericModelType type = pathElement.getType();
		switch (type.getTypeCode()) {
		case booleanType:
		case dateType:
		case decimalType:
		case doubleType:
		case floatType:
		case integerType:
		case longType:
		case stringType:
			onSimpleValueEnter(context, pathElement.getValue(), pathElement);
			break;
		case entityType:
			onEntityEnter(context, (GenericEntity) pathElement.getValue(), pathElement);
			break;
		case enumType:
			onEnumValueEnter(context, (Enum<?>) pathElement.getValue(), pathElement);
			break;
		case listType:
			onListEnter(context, (List<?>) pathElement.getValue(), pathElement);
			break;
		case mapType:
			onMapEnter(context, (Map<?, ?>) pathElement.getValue(), pathElement);
			break;
		case setType:
			onSetEnter(context, (Set<?>) pathElement.getValue(), pathElement);
			break;
		default:
			throw new GmTraversingException("Unknown value type: " + pathElement.getType());
		}
	}

	@Override
	public void onElementLeave(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		if (pathElement.getValue() == null)
			return;
		
		GenericModelType type = pathElement.getType();
		switch (type.getTypeCode()) {
		case booleanType:
		case dateType:
		case decimalType:
		case doubleType:
		case floatType:
		case integerType:
		case longType:
		case stringType:
			onSimpleValueLeave(context, pathElement.getValue(), pathElement);
			break;
		case entityType:
			onEntityLeave(context, (GenericEntity) pathElement.getValue(), pathElement);
			break;
		case enumType:
			onEnumValueLeave(context, (Enum<?>) pathElement.getValue(), pathElement);
			break;
		case listType:
			onListLeave(context, (List<?>) pathElement.getValue(), pathElement);
			break;
		case mapType:
			onMapLeave(context, (Map<?, ?>) pathElement.getValue(), pathElement);
			break;
		case setType:
			onSetLeave(context, (Set<?>) pathElement.getValue(), pathElement);
			break;
		default:
			throw new GmTraversingException("Unknown value type: " + pathElement.getType());
		}
	}

	@SuppressWarnings("unused")
	protected void onSimpleValueEnter(GmTraversingContext context, Object value, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onSimpleValueLeave(GmTraversingContext context, Object value, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onEnumValueEnter(GmTraversingContext context, Enum<?> value, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onEnumValueLeave(GmTraversingContext context, Enum<?> value, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onListEnter(GmTraversingContext context, List<?> list, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onListLeave(GmTraversingContext context, List<?> list, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onSetEnter(GmTraversingContext context, Set<?> set, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onSetLeave(GmTraversingContext context, Set<?> set, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onMapEnter(GmTraversingContext context, Map<?, ?> map, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onMapLeave(GmTraversingContext context, Map<?, ?> map, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onEntityEnter(GmTraversingContext context, GenericEntity entity, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

	@SuppressWarnings("unused")
	protected void onEntityLeave(GmTraversingContext context, GenericEntity entity, TraversingModelPathElement pathElement) {
		// intentionally left blank; can be implemented by sub-class
	}

}
