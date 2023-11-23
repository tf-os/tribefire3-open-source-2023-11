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

import java.util.HashSet;
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
 * Questions for Peter and Michel:
 * <ul>
 * 		<li>is any ListItemPathElement (and co) really always property related -> no -> how to deal with it</li>
 * 
 * </ul>
 * @author dirk.scheffler
 *
 */
public class ModelWalker extends GmTraversingVisitorAdapter {
	private boolean breadthFirst;
	private ModelWalkerCustomization modelWalkerCustomization;
	private GmTraversingEvent cursor;
	private Set<GenericEntity> walkedEntities = new HashSet<GenericEntity>(); 
	
	public void setWalkerCustomization(ModelWalkerCustomization modelWalkerCustomization) {
		this.modelWalkerCustomization = modelWalkerCustomization;
	}

	/**
	 * @param breadthFirst
	 *            if true the walker will switch the walk from depth first to
	 *            breadth first mode (default is false)
	 */
	public void setBreadthFirst(boolean breadthFirst) {
		this.breadthFirst = breadthFirst;
	}
	
	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {
		if (breadthFirst) {
			if (context.getCurrentDepth() == 0) {
				cursor = null; 
			}
		}
		else {
			cursor = context.getEvent();
		}
		
		if (pathElement.getValue() == null)
			return;
		
		switch (pathElement.getType().getTypeCode()) {
		case entityType:
			onEntityEnter(context, (GenericEntity) pathElement.getValue(), pathElement);
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
			break;
		}
	}
	
	protected void appendEventPair(GmTraversingContext context, TraversingModelPathElement pathElement) {
		TraversingModelPathElement substitutePathElement = modelWalkerCustomization != null ? modelWalkerCustomization.substitute(context, pathElement) : pathElement;
		GmTraversingEnterEvent enterEvent = context.appendEventPair(cursor, substitutePathElement);
		cursor = breadthFirst? enterEvent: enterEvent.getLeaveEvent();
	}

	protected void onEntityEnter(GmTraversingContext context, GenericEntity entity, TraversingModelPathElement pathElement) {
		if (!walkedEntities.add(entity))
			return;
		
		EntityType<GenericEntity> entityType = pathElement.getType();
		
		for (Property property : entityType.getProperties()) {
			AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

			// if there is no absence information, or if there is absence information that is resolvable
			if (absenceInformation == null || 
					(modelWalkerCustomization != null && 
					 modelWalkerCustomization.isAbsenceResolvable(context,property, entity, absenceInformation))) {

				Object propertyValue = property.get(entity);
				GenericModelType actualType = property.getType().getActualType(propertyValue);
				TraversingPropertyModelPathElement propertyPathElement = new TraversingPropertyModelPathElement(
						pathElement, 
						propertyValue, 
						actualType, 
						entity, 
						entityType, 
						property, 
						false); // indicate that property is not absent

				appendEventPair(context, propertyPathElement);
			} else { //if absence information is traversable 
				if (modelWalkerCustomization != null && 
					modelWalkerCustomization.traverseAbsentProperty(entity, property, context, pathElement)) {

					Object propertyValue = property.get(entity);
					GenericModelType actualType = property.getType().getActualType(propertyValue);
					TraversingPropertyModelPathElement propertyPathElement = new TraversingPropertyModelPathElement(
							pathElement, 
							propertyValue, 
							actualType, 
							entity, 
							entityType, 
							property, 
							true); // indicate that property is absent

					appendEventPair(context, propertyPathElement);
				}
			}
		}
	}
	
	protected void onListEnter(GmTraversingContext context, List<?> list, TraversingModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getType();
		GenericModelType elementType = collectionType.getCollectionElementType();
		
		int index = 0; 
		for (Object element: list) {
			GenericModelType actualType = elementType.getActualType(element);
			
			TraversingListItemModelPathElement listItemElement = new TraversingListItemModelPathElement(
					pathElement, 
					element, 
					actualType, 
					index++);
			
			appendEventPair(context, listItemElement);
		}
	}
	
	protected void onSetEnter(GmTraversingContext context, Set<?> set, TraversingModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getType();
		GenericModelType elementType = collectionType.getCollectionElementType();
		
		for (Object element: set) {
			GenericModelType actualType = elementType.getActualType(element);
			
			TraversingSetItemModelPathElement setItemElement = new TraversingSetItemModelPathElement(
					pathElement, 
					element, 
					actualType);
			
			appendEventPair(context, setItemElement);
		}
	}
	
	protected void onMapEnter(GmTraversingContext context, Map<?, ?> map, TraversingModelPathElement pathElement) {
		CollectionType collectionType = pathElement.getType();
		GenericModelType[] parameterization = collectionType.getParameterization();
		GenericModelType keyType = parameterization[0];
		GenericModelType valueType = parameterization[1];
		
		for (Map.Entry<?, ?> entry: map.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			GenericModelType actualKeyType = keyType.getActualType(key);
			GenericModelType actualValueType = valueType.getActualType(value);
			
			TraversingMapKeyModelPathElement mapKeyElement = new TraversingMapKeyModelPathElement(
					pathElement, 
					key, 
					actualKeyType, 
					value, actualValueType, entry);
			
			appendEventPair(context, mapKeyElement);
			
			TraversingMapValueModelPathElement mapValueElement = new TraversingMapValueModelPathElement(mapKeyElement);
			
			appendEventPair(context, mapValueElement);
		}
		
	}
}
