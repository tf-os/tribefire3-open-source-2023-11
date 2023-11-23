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
package com.braintribe.model.processing.query.test.builder;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.smood.Smood;

/**
 * 
 */
public class AbstractBuilder<T extends GenericEntity, B extends AbstractBuilder<T, B>> {

	protected final B self;
	protected final Smood smood;
	protected final Class<T> clazz;
	protected final EntityType<T> entityType;
	protected final T instance;

	protected AbstractBuilder(Class<T> clazz, Smood smood) {
		this.clazz = clazz;
		this.smood = smood;

		this.entityType = GMF.getTypeReflection().getEntityType(clazz);
		this.instance = newInstance();
		this.self = (B) this;
	}

	protected T newInstance() {
		return entityType.create();
	}

	public T create() {
		return create(smood != null);
	}

	public T create(boolean register) {
		return register ? createAndRegister(true) : instance;
	}

	private T createAndRegister(boolean generateId) {
		smood.registerEntity(instance, generateId);

		return instance;
	}

	public B id(Object value) {
		instance.setId(value);
		return self;
	}

	public B partition(String value) {
		instance.setPartition(value);
		return self;
	}

	public B globalId(String globalId) {
		instance.setGlobalId(globalId);
		return self;
	}
	
}
