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
package com.braintribe.model.processing.session.impl.session;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.delete;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.instantiation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.manifestation;
import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.voidManipulation;
import static com.braintribe.utils.lcd.CollectionTools2.iteratorAtTheEndOf;

import java.util.ListIterator;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.FieldAccessingPropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.session.api.notifying.interceptors.InterceptorIdentification;
import com.braintribe.utils.collection.impl.SimplePartiallyOrderedSet;

public abstract class AbstractGmSession implements GmSession {

	protected static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	protected final SimplePartiallyOrderedSet<PropertyAccessInterceptor, Class<? extends InterceptorIdentification>> interceptors = new SimplePartiallyOrderedSet<>();
	protected PropertyAccessInterceptor firstInterceptor = FieldAccessingPropertyAccessInterceptor.INSTANCE;

	protected String description;

	/** String descriptor of this session for better debugging/troubleshooting. */
	@Configurable
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType) {
		return createEntity(entityType, null);
	}

	@Override
	public <T extends GenericEntity> T createRaw(EntityType<T> entityType) {
		return createEntity(entityType, false, null);
	}

	protected <T extends GenericEntity> T createEntity(EntityType<T> entityType, String partition) {
		return createEntity(entityType, true, partition);
	}
	
	protected <T extends GenericEntity> T createEntity(EntityType<T> entityType, boolean initialize, String partition) {
		T entity = entityType.createRaw();
		entity.setPartition(partition);
		
		entity.attach(this);

		InstantiationManipulation instantiationManipulation = createInstantiationManipulation(entity);
		DeleteManipulation inverseManipulation = createUninstantiationManipulation(entity);
		instantiationManipulation.linkInverse(inverseManipulation);

		noticeManipulation(instantiationManipulation);

		if (initialize)
			entityType.initialize(entity);
		
		return entity;
	}

	@Override
	public void deleteEntity(GenericEntity entity) {
		detachEntity(entity);
		
		DeleteManipulation deleteManipulation = delete(entity, DeleteMode.ignoreReferences);
		ManifestationManipulation manifestationManipulation = manifestation(entity);

		deleteManipulation.linkInverse(manifestationManipulation);
		
		noticeManipulation(deleteManipulation);
	}

	private void detachEntity(GenericEntity entity) {
		if (entity.session() == this)
			entity.detach();
		else
			throw new GmSessionRuntimeException("Cannot detach an entity from this session as it is attached to another session. Entity: " + entity);
	}
	
	@Override
	public PropertyAccessInterceptor getInterceptor() {
		return firstInterceptor;
	}

	/**
	 * This method assumes at least one 
	 */
	protected void refreshInterceptorChain() {
		firstInterceptor = FieldAccessingPropertyAccessInterceptor.INSTANCE;

		if (interceptors.isEmpty())
			return;

		ListIterator<PropertyAccessInterceptor> it = iteratorAtTheEndOf(interceptors.stream().collect(Collectors.toList()));
		while (it.hasPrevious()) {
			PropertyAccessInterceptor secondInterceptor = firstInterceptor;
			firstInterceptor = it.previous();
			firstInterceptor.next = secondInterceptor;
		}
	}

	private InstantiationManipulation createInstantiationManipulation(GenericEntity entity) {
		InstantiationManipulation instantiationManipulation = instantiation(entity);
		return instantiationManipulation;
	}

	private DeleteManipulation createUninstantiationManipulation(GenericEntity entity) {
		return delete(entity, DeleteMode.ignoreReferences);
	}

	@Override
	public void attach(GenericEntity entity) throws GmSessionRuntimeException {
		if (!entity.isEnhanced())
			throw new GmSessionRuntimeException("Generic entity must be enhanced. Entity: " + entity);

		// TODO not sure if this is actually illegal. I had some tests where this would throw an exception when query
		// result is being merged to the session.
		// GmSession attachedSession = attachableEntity.accessSession();
		// if (attachedSession != null && attachedSession != this) {
		// throw new GmSessionRuntimeException("Entity is already attached to other session.");
		// }
		
		entity.attach(this);

		ManifestationManipulation manifestationManipulation = manifestation(entity);
		manifestationManipulation.linkInverse(voidManipulation());

		this.noticeManipulation(manifestationManipulation);
	}

	@Override
	public String toString() {
		return super.toString() + (description == null ? "" : " [" + description + "]");
	}

}
