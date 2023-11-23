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
package com.braintribe.model.processing.traverse;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;

public class ConfigurableEntityVisiting extends EntityVisiting {

	private BiFunction<EntityType<?>, GenericEntity, Boolean> entityAdder;
	private BiConsumer<EnumType, Enum<?>> enumAdder;

	public ConfigurableEntityVisiting(BiFunction<EntityType<?>, GenericEntity, Boolean> entityAdder, BiConsumer<EnumType, Enum<?>> enumAdder) {
		super();
		this.entityAdder = entityAdder;
		this.enumAdder = enumAdder;
		this.visitEnums = true;
	}
	
	public ConfigurableEntityVisiting(BiFunction<EntityType<?>, GenericEntity, Boolean> entityAdder) {
		super();
		this.entityAdder = entityAdder;
		this.visitEnums = false;
	}

	@Override
	protected boolean add(GenericEntity entity, EntityType<?> type) {
		return entityAdder.apply(type, entity);
	}

	@Override
	protected void add(Enum<?> constant, EnumType type) {
		enumAdder.accept(type, constant);
	}

}
