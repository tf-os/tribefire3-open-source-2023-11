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
package com.braintribe.model.processing.impl;

import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.ConstraintViolation;
import com.braintribe.model.processing.PropertyValidationContext;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.traversing.api.path.TraversingPropertyModelPathElement;

/**
 * Contains various information about what is to be validated as well as a metadata resolver
 * ({@link #getPropertyMdResolver()}. Constraint violations can be reported by
 * {@link #notifyConstraintViolation(String)}.
 * 
 * @author Neidhart.Orlich
 *
 */
public class PropertyValidationContextImpl extends ValidationContextImpl implements PropertyValidationContext {
	private final Property property;
	private final GenericModelType propertyType;
	private final PropertyMdResolver propertyMdResolver;
	private final GenericEntity entity;
	private final EntityType<GenericEntity> entityType;

	public PropertyValidationContextImpl(TraversingPropertyModelPathElement pathElement, ModelMdResolver mdResolver,
			Consumer<ConstraintViolation> constraintViolationConsumer) {
		super(pathElement, constraintViolationConsumer);

		this.entity = pathElement.getEntity();
		this.entityType = entity.entityType();
		this.property = pathElement.getProperty();
		this.propertyType = property.getType();
		this.propertyMdResolver = mdResolver.entity(entity).property(property);

	}

	@Override
	public Property getProperty() {
		return property;
	}

	@Override
	public Object getPropertyValue() {
		return getValue();
	}

	@Override
	public GenericModelType getPropertyType() {
		return propertyType;
	}

	@Override
	public PropertyMdResolver getPropertyMdResolver() {
		return propertyMdResolver;
	}

	@Override
	public GenericEntity getEntity() {
		return entity;
	}

	@Override
	public EntityType<GenericEntity> getEntityType() {
		return entityType;
	}

}
