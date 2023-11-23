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
package com.braintribe.testing.tools.gm.builder;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

/**
 * @author peter.gazdik
 */
public class EntityBuilder {

	private final Consumer<GenericEntity> postConstructHandler;
	private final EntityType<?> entityType;
	private final GenericEntity entity;

	public EntityBuilder(EntityType<?> entityType, Function<EntityType<?>, GenericEntity> entityFactory,
			Consumer<GenericEntity> postConstructHandler) {

		this.entityType = entityType;
		this.postConstructHandler = postConstructHandler;
		this.entity = entityFactory.apply(entityType);
	}

	public EntityBuilder set(String propertyName, Object value) {
		entity.write(property(propertyName), value);
		return this;
	}

	public EntityBuilder add(String propertyName, Object value) {
		Collection<Object> collection = (Collection<Object>) entity.read(property(propertyName));
		collection.add(value);
		return this;
	}

	public EntityBuilder put(String propertyName, Object key, Object value) {
		Map<Object, Object> map = (Map<Object, Object>) entity.read(property(propertyName));
		map.put(key, value);
		return this;
	}

	public <T extends GenericEntity> T done() {
		postConstructHandler.accept(entity);
		return (T) entity;
	}

	private Property property(String propertyName) {
		return entityType.getProperty(propertyName);
	}

}
