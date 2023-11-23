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
package com.braintribe.model.processing.traversing.engine.impl.walk;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingEnterEvent;
import com.braintribe.model.processing.traversing.api.GmTraversingEvent;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.path.TraversingListItemModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingMapKeyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingMapValueModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;
import com.braintribe.model.processing.traversing.api.path.TraversingSetItemModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.customize.ModelWalkerCustomization;
import com.braintribe.model.processing.traversing.impl.visitors.GmTraversingVisitorAdapter;

/**
 * @author dirk.scheffler
 */
public class ModelWalker extends GmTraversingVisitorAdapter {

	private boolean breadthFirst;
	private ModelWalkerCustomization walkerCustomization;
	/** When appending events, the appended event will be placed immediately after cursor. */
	private GmTraversingEvent cursor;
	private final Set<GenericEntity> walkedEntities = newSet();

	/** @see ModelWalkerCustomization */
	public void setWalkerCustomization(ModelWalkerCustomization modelWalkerCustomization) {
		this.walkerCustomization = modelWalkerCustomization;
	}

	/**
	 * @param breadthFirst
	 *            if true the walker will switch the walk from depth first to breadth first mode (default is false)
	 */
	public void setBreadthFirst(boolean breadthFirst) {
		this.breadthFirst = breadthFirst;
	}

	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		if (!breadthFirst)
			cursor = context.getEvent();
		else if (context.getCurrentDepth() == 0)
			cursor = null;

		if (pathElement.getValue() == null)
			return;

		switch (pathElement.getType().getTypeCode()) {
			case entityType:
				onEntityEnter(context, (GenericEntity) pathElement.getValue(), pathElement);
				return;
			case listType:
				onListEnter(context, (List<?>) pathElement.getValue(), pathElement);
				return;
			case mapType:
				onMapEnter(context, (Map<?, ?>) pathElement.getValue(), pathElement);
				return;
			case setType:
				onSetEnter(context, (Set<?>) pathElement.getValue(), pathElement);
				return;
			default:
				return;
		}
	}

	protected void onEntityEnter(GmTraversingContext context, GenericEntity entity, TraversingModelPathElement pathElement) {
		if (!walkedEntities.add(entity))
			return;

		EntityType<?> entityType = pathElement.getType();

		for (Property property : entityType.getProperties()) {
			AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

			// if there is no absence information, or if there is absence information that is resolvable
			Boolean absentFlag = absentPropertyFlag(context, property, entity, absenceInformation, pathElement);
			if (absentFlag == null)
				// null means the property is absent and we are not allowed to access it -> this property is ignored
				continue;

			boolean isAbsent = absentFlag.booleanValue();
			Object propertyValue = isAbsent ? null : property.get(entity);
			GenericModelType actualType = property.getType().getActualType(propertyValue);

			TraversingPropertyModelPathElement propertyPathElement = new TraversingPropertyModelPathElement( //
					pathElement, //
					propertyValue, //
					actualType, //
					entity, //
					entityType, //
					property, //
					isAbsent //
			);

			propertyPathElement.setValueResolved(!isAbsent);

			appendEventPair(context, propertyPathElement);
		}
	}

	// null - property is absent and we cannot access it
	// false - property is either present, or absent but we can resolve it
	// true - property is absent, but we can traverse it- we keep it absent, but let the pathElement resolve it when asked for it.
	// Oh and no, I don't understand how we can say absent proptery can be traversed but cannot be resolved.
	private Boolean absentPropertyFlag(GmTraversingContext context, Property property, GenericEntity entity, AbsenceInformation absenceInformation,
			TraversingModelPathElement pathElement) {

		if (absenceInformation == null)
			return Boolean.FALSE;

		if (walkerCustomization == null)
			return null;

		if (walkerCustomization.isAbsenceResolvable(context, property, entity, absenceInformation))
			return Boolean.FALSE;

		if (walkerCustomization.traverseAbsentProperty(entity, property, context, pathElement))
			return Boolean.TRUE;

		return null;
	}

	protected void onListEnter(GmTraversingContext context, List<?> list, TraversingModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getType();
		GenericModelType elementType = collectionType.getCollectionElementType();

		int index = 0;
		for (Object element : list) {
			GenericModelType actualType = elementType.getActualType(element);

			TraversingListItemModelPathElement listItemElement = new TraversingListItemModelPathElement( //
					pathElement, //
					element, //
					actualType, //
					index++//
			);

			appendEventPair(context, listItemElement);
		}
	}

	protected void onSetEnter(GmTraversingContext context, Set<?> set, TraversingModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getType();
		GenericModelType elementType = collectionType.getCollectionElementType();

		for (Object element : set) {
			GenericModelType actualType = elementType.getActualType(element);

			TraversingSetItemModelPathElement setItemElement = new TraversingSetItemModelPathElement(pathElement, element, actualType);

			appendEventPair(context, setItemElement);
		}
	}

	protected void onMapEnter(GmTraversingContext context, Map<?, ?> map, TraversingModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getType();
		GenericModelType[] parameterization = collectionType.getParameterization();
		GenericModelType keyType = parameterization[0];
		GenericModelType valueType = parameterization[1];

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();

			GenericModelType actualKeyType = keyType.getActualType(key);
			GenericModelType actualValueType = valueType.getActualType(value);

			TraversingMapKeyModelPathElement mapKeyElement = new TraversingMapKeyModelPathElement( //
					pathElement, key, actualKeyType, value, actualValueType, entry);
			appendEventPair(context, mapKeyElement);

			TraversingMapValueModelPathElement mapValueElement = new TraversingMapValueModelPathElement(mapKeyElement);
			appendEventPair(context, mapValueElement);
		}
	}

	protected void appendEventPair(GmTraversingContext context, TraversingModelPathElement pathElement) {
		TraversingModelPathElement _pathElement = walkerCustomization == null ? pathElement : walkerCustomization.substitute(context, pathElement);
		GmTraversingEnterEvent enterEvent = context.appendEventPair(cursor, _pathElement);
		cursor = breadthFirst ? enterEvent : enterEvent.getLeaveEvent();
	}

}
