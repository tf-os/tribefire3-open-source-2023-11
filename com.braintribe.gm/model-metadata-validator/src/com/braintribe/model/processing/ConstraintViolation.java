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
package com.braintribe.model.processing;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.path.TraversingListItemModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingMapKeyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingMapValueModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyRelatedModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingSetItemModelPathElement;
import com.braintribe.utils.CommonTools;

public class ConstraintViolation {
	private final Object value;
	private final String message;
	private final TraversingModelPathElement pathElement;

	public static ConstraintViolation create(String message, TraversingModelPathElement pathElement) {
		return new ConstraintViolation(message, pathElement);
	}

	public static ConstraintViolation create(String string, TraversingModelPathElement pathElement, Property property) {
		GenericEntity entity = pathElement.getValue();
		TraversingModelPathElement propertyPathElement = new TraversingPropertyModelPathElement(pathElement, entity, entity.entityType(), property,
				false);
		return create(string, propertyPathElement);
	}

	public ConstraintViolation(String message, TraversingModelPathElement pathElement) {
		this(pathElement.getValue(), message, pathElement);
	}
	public ConstraintViolation(Object value, String message, TraversingModelPathElement pathElement) {
		this.value = value;
		this.message = message;
		this.pathElement = pathElement;
	}

	public Object getValue() {
		return value;
	}

	public String getMessage() {
		return message;
	}

	private static StringBuilder pathToString(TraversingModelPathElement pathElement) {
		StringBuilder result;

		TraversingModelPathElement previous = pathElement.getPrevious();
		if (previous != null && previous.getElementType() != ModelPathElementType.Root) {
			result = pathToString(previous);
		} else {
			result = new StringBuilder();
		}

		renderPathElement(result, pathElement);

		return result;
	}

	private static void renderPathElement(StringBuilder appendable, TraversingModelPathElement pathElement) {
		switch (pathElement.getElementType()) {
			case ListItem:
				TraversingListItemModelPathElement listPathElement = (TraversingListItemModelPathElement) pathElement;
				appendable.append('[');
				appendable.append(String.valueOf(listPathElement.getIndex()));
				appendable.append(']');
				break;
			case MapKey:
				appendable.append("->");
				TraversingMapKeyModelPathElement keyModelPathElement = (TraversingMapKeyModelPathElement) pathElement;
				appendable.append(keyModelPathElement.getKey().toString());
				break;
			case MapValue:
				appendable.append('[');
				TraversingMapValueModelPathElement valueModelPathElement = (TraversingMapValueModelPathElement) pathElement;
				appendable.append(valueModelPathElement.getKey().toString());
				appendable.append(']');
				break;
			case Property:
				if (pathElement.getPrevious().getElementType() != ModelPathElementType.Root) {
					appendable.append('.');
				}
				TraversingPropertyRelatedModelPathElement propertyrelatedPathElement = (TraversingPropertyRelatedModelPathElement) pathElement;
				appendable.append(propertyrelatedPathElement.getProperty().getName());
				break;
			case Root:
				appendable.append("(Root Element)");
				break;
			case SetItem:
				appendable.append("->");
				TraversingSetItemModelPathElement setItemModelPathElement = (TraversingSetItemModelPathElement) pathElement;
				appendable.append(setItemModelPathElement.getValue().toString());
				break;
			default:
				break;

		}
	}

	public String getPathString() {
		return pathToString(pathElement).toString();
	}

	@Override
	public String toString() {
		return getPathString() + ": " + CommonTools.getStringRepresentation(value) + " \n\t" + message;
	}

	public TraversingModelPathElement getPathElement() {
		return pathElement;
	}

}
