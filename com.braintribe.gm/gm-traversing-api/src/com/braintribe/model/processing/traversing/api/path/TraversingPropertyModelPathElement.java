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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.api.IPropertyModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

/**
 * This {@link TraversingPropertyRelatedModelPathElement} addresses the property value itself which in cases of collection properties precedes other
 * {@link TraversingPropertyRelatedModelPathElement}s
 * 
 * @author dirk.scheffler
 * @author pit.steinlin
 * @author peter.gazdik
 */
public class TraversingPropertyModelPathElement extends TraversingPropertyRelatedModelPathElement implements IPropertyModelPathElement {

	private boolean valueResolved;
	private boolean typeResolved;
	private boolean absent;
	private final GenericEntity entity;
	private final Property property;
	private final EntityType<?> entityType;

	public TraversingPropertyModelPathElement(TraversingModelPathElement previous, Object value, GenericModelType type, GenericEntity entity,
			EntityType<?> entityType, Property property, boolean absenceFlag) {
		super(previous, value, type);
		this.entity = entity;
		this.entityType = entityType;
		this.property = property;
		this.absent = absenceFlag;
	}

	public TraversingPropertyModelPathElement(TraversingModelPathElement previous, GenericEntity entity, EntityType<?> entityType, Property property,
			boolean absenceFlag) {
		super(previous);
		this.absent = absenceFlag;
		this.entity = entity;
		this.entityType = entityType;
		this.property = property;
		this.absent = absenceFlag;
	}

	@Override
	public void substituteValue(Object value) {
		super.substituteValue(value);
		valueResolved = true;
		typeResolved = true;
	}

	@Override
	public Property getProperty() {
		return property;
	}

	@Override
	public <T extends GenericEntity> T getEntity() {
		return (T) entity;
	}

	@Override
	public <T extends GenericEntity> EntityType<T> getEntityType() {
		return entityType.cast();
	}

	@Override
	public <T> T getValue() {
		if (!valueResolved) {
			value = getProperty().get(getEntity());
			valueResolved = true;
		}

		return (T) value;
	}

	@Override
	public <T extends GenericModelType> T getType() {
		if (!typeResolved) {
			type = getProperty().getType().getActualType(getValue());
			typeResolved = true;
		}

		return (T) type;
	}

	@Override
	public ModelPathElementType getElementType() {
		return ModelPathElementType.Property;
	}

	public boolean isAbsent() {
		return absent;
	}

	public void setValueResolved(boolean valueResolved) {
		this.valueResolved = valueResolved;
	}

	public void setTypeResolved(boolean typeResolved) {
		this.typeResolved = typeResolved;
	}
}
