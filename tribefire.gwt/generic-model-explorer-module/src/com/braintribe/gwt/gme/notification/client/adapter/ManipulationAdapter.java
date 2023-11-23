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
package com.braintribe.gwt.gme.notification.client.adapter;

import java.util.Collections;
import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.OwnerType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;

/**
 * Generic expert based manipulation adapter.
 * 
 */
public class ManipulationAdapter implements ManipulationListener {

	// --- public manipulation events ---

	public interface OnPropertyChange<GE extends GenericEntity, PropertyType> {
		void onChangeValue(GE entity, String propertyName, PropertyType newValue);
	}

	public interface OnCollectionAdd<GE extends GenericEntity, ItemType> {
		void onAdd(GE entity, String propertyName, Map<Object, ItemType> itemsToAdd);
	}

	public interface OnCollectionRemove<GE extends GenericEntity, ItemType> {
		void onRemove(GE entity, String propertyName, Map<Object, ItemType> itemsToRemove);
	}

	private static <T extends GenericEntity> EntityType<T> entityTypeOf(Class<T> entityClass) {
		if (entityClass != null)
			return GMF.getTypeReflection().getEntityType(entityClass);
		return null;
	}

	// --- private helpers ---

	private static GenericEntity getLocalEntityPropertyEntity(PropertyManipulation manipulation) {
		if (manipulation.getOwner().ownerType() == OwnerType.LOCAL_ENTITY_PROPERTY)
			return ((LocalEntityProperty) manipulation.getOwner()).getEntity();
		return null;
	}

	private static <GE extends GenericEntity> boolean isTypeAndName(GE manipulationEntity, EntityType<GE> assignEntityType, String manipulationName, String assignName) {
		if (manipulationEntity != null && assignEntityType != null) {
			EntityType<GenericEntity> typeOfEntity = manipulationEntity.entityType();
			boolean isAssignable = assignEntityType.isAssignableFrom(typeOfEntity);
			boolean isPropertyName = assignName == null || assignName.equals(manipulationName);
			return isAssignable && isPropertyName;
		}
		return assignEntityType == null;
	}

	// - - - - - - - - - - internal abstract adapter listener - - - - - - - - -

	private static abstract class AbsatractListener<PM extends PropertyManipulation, GE extends GenericEntity> implements ManipulationAdapterListener<PM> {

		private final EntityType<GE> entityType;
		private final String propertyName;

		public AbsatractListener(Class<GE> entityClass, String propertyName) {
			this.entityType = entityTypeOf(entityClass);
			this.propertyName = propertyName;
		}

		@Override
		public void noticeManipulation(PM manipulation) {
			GE lepEntity = (GE) getLocalEntityPropertyEntity(manipulation);
			String lepPropertyName = manipulation.getOwner().getPropertyName();
			if (isTypeAndName(lepEntity, entityType, lepPropertyName, propertyName))
				noticeManipulation(manipulation, lepEntity, lepPropertyName);
		}

		protected abstract void noticeManipulation(PM manipulation, GE entity, String propertyName);

	}

	// --- internal used expert registry ---

	private final ConfigurableGmExpertRegistry expertRegistry = new ConfigurableGmExpertRegistry();

	// --- public listener support ---

	/**
	 * Adds a new listener to the corresponding expert.
	 * 
	 * @param manipulationClass
	 *            - class of the expert
	 * @param manipulationListener
	 *            - listener to register to the expert
	 */
	public <M extends Manipulation> void addListener(Class<M> manipulationClass, ManipulationAdapterListener<M> manipulationListener) {
		ManipulationAdapterExpert<M> expert = expertRegistry.findExpert(ManipulationAdapterExpert.class).forType(manipulationClass);
		if (expert == null) {
			ConfigurableGmExpertDefinition expertDefinition = new ConfigurableGmExpertDefinition();
			expertDefinition.setDenotationType(manipulationClass);
			expertDefinition.setExpertType(ManipulationListener.class);
			expertDefinition.setExpert(expert = new ManipulationAdapterExpert<M>());
			expertRegistry.setExpertDefinitions(Collections.<GmExpertDefinition> singletonList(expertDefinition));
		}
		expert.addDelegate(manipulationListener);
	}

	public <GE extends GenericEntity, PT extends Object> void addListener(Class<GE> entityClass, String propertyName, final OnPropertyChange<GE, PT> listener) {
		addListener(ChangeValueManipulation.class, new AbsatractListener<ChangeValueManipulation, GE>(entityClass, propertyName) {
			@Override
			protected void noticeManipulation(ChangeValueManipulation manipulation, GE entity, String propertyName) {
				listener.onChangeValue(entity, propertyName, (PT) manipulation.getNewValue());
			}
		});
	}

	public <GE extends GenericEntity, IT extends GenericEntity> void addListener(Class<GE> entityClass, String propertyName, final OnCollectionAdd<GE, IT> listener) {
		addListener(AddManipulation.class, new AbsatractListener<AddManipulation, GE>(entityClass, propertyName) {
			@Override
			protected void noticeManipulation(AddManipulation manipulation, GE entity, String propertyName) {
				Object map = manipulation.getItemsToAdd();
				listener.onAdd(entity, propertyName, (Map<Object, IT>) map);
			}
		});
	}

	public <GE extends GenericEntity, IT extends GenericEntity> void addListener(Class<GE> entityClass, String propertyName, final OnCollectionRemove<GE, IT> listener) {
		addListener(RemoveManipulation.class, new AbsatractListener<RemoveManipulation, GE>(entityClass, propertyName) {
			@Override
			public void noticeManipulation(RemoveManipulation manipulation, GE entity, String propertyName) {
				Object map = manipulation.getItemsToRemove();
				listener.onRemove(entity, propertyName, (Map<Object, IT>) map);
			}
		});
	}

	// --- implemented delegate manipulation using an expert ---

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		ManipulationListener expert = expertRegistry.findExpert(ManipulationListener.class).forInstance(manipulation);
		if (expert != null)
			expert.noticeManipulation(manipulation);
	}

}
