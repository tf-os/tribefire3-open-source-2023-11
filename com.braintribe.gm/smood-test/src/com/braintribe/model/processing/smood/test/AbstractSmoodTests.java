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
package com.braintribe.model.processing.smood.test;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Before;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.test.builder.DataBuilder;
import com.braintribe.model.processing.query.test.model.MetaModelProvider;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * 
 */
public abstract class AbstractSmoodTests {

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected DataBuilder b;
	protected Smood smood;

	@Before
	public void setup() {
		smood = new Smood(new ReentrantReadWriteLock());
		setSmoodMetaModel();

		b = newBuilder();

		postConstruct();
	}

	protected void setSmoodMetaModel() {
		smood.setMetaModel(provideEnrichedMetaModel());
	}

	protected GmMetaModel provideEnrichedMetaModel() {
		return MetaModelProvider.provideEnrichedModel();
	}

	protected void postConstruct() {
		// intentionally left blank, to be implemented by sub-types if needed
	}

	protected DataBuilder newBuilder() {
		return new DataBuilder(smood);
	}

	protected void registerAtSmood(GenericEntity... entities) {
		registerAtSmood(true, entities);
	}

	protected void registerAtSmood(boolean generateId, GenericEntity... entities) {
		for (GenericEntity entity : entities)
			smood.registerEntity(entity, generateId);
	}

	protected EntityReference entityReference(GenericEntity entity) {
		return entity.reference();
	}

	protected <T extends GenericEntity> EntityType<T> entityType(T entity) {
		return entity.entityType();
	}

	protected <K, V> Map<K, V> asMap(Object... objs) {
		return CollectionTools2.asMap(objs);
	}

}
