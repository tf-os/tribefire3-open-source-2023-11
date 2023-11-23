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
package com.braintribe.model.access.security.manipulation.experts;

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityContext;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpositionContext;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.CollectionTools;

/**
 * {@link ManipulationSecurityExpert} for {@link Unique} constraint.
 */
public class UniqueKeyPropertyExpert implements ManipulationSecurityExpert {

	@Override
	public Object createExpertContext(ManipulationSecurityContext context) {
		return newMap();
	}

	private static class EntityEntry {
		boolean deleted = false;
		GenericEntity instance;
		EntityReference entityReference;
		Map<String, PropertyValueEntry> propertiesSet = newMap();

		String typeSignature() {
			return entityReference.getTypeSignature();
		}
	}

	private static class PropertyValueEntry {
		ChangeValueManipulation manipulation;
		Object newValue;

		PropertyValueEntry(ChangeValueManipulation manipulation, Object newValue) {
			this.manipulation = manipulation;
			this.newValue = newValue;
		}

	}

	// ####################################
	// ## . . . . . EXPOSITION . . . . . ##
	// ####################################

	@Override
	public void expose(ManipulationSecurityExpositionContext context) {
		if (context.getTargetReference() == null)
			return;

		switch (context.getCurrentManipulationType()) {
			case CHANGE_VALUE:
				notifyNewValue(context);
				checkIfIdChange(context);
				return;

			case DELETE:
				notifyDelete(context);
				return;

			default:
				return;
		}
	}

	private void notifyNewValue(ManipulationSecurityExpositionContext context) {
		String typeSignature = context.getTargetSignature();
		String propertyName = context.getTargetPropertyName();

		if (!isPropertyUnique(typeSignature, propertyName, context))
			return;

		ChangeValueManipulation cvm = context.getCurrentManipulation();
		Object newValue = getNormalizedValue(context, cvm.getNewValue());

		EntityEntry entry = acquireEntityEntry(context);
		entry.propertiesSet.put(propertyName, new PropertyValueEntry(cvm, newValue));
	}

	private Object getNormalizedValue(ManipulationSecurityExpositionContext context, Object value) {
		if (value instanceof EntityReference)
			return context.getNormalizeReference((EntityReference) value);
		else
			/* We do not have to handle collections as we do not support unique constraint for such properties */
			return value;
	}

	private void checkIfIdChange(ManipulationSecurityExpositionContext context) {
		ManipulationSecurityExpertTools.updateIdIfNeededChange(context, context.<Map<?, ?>> getExpertContext());
	}

	private void notifyDelete(ManipulationSecurityExpositionContext context) {
		acquireEntityEntry(context).deleted = true;
	}

	private EntityEntry acquireEntityEntry(ManipulationSecurityExpositionContext context) {
		GenericEntity reference = getKeyForEntityEntry(context);

		Map<GenericEntity, EntityEntry> map = context.getExpertContext();

		EntityEntry entry = map.get(reference);

		if (entry == null) {
			entry = new EntityEntry();
			entry.instance = context.getTargetInstance();
			entry.entityReference = context.getTargetReference();
			map.put(reference, entry);
		}

		return entry;
	}

	/**
	 * We want to use the target instance as key if possible, if not, we use the normalized reference. Why? Because
	 * later, we are making a query for entity with given value and then we check whether the property for that entity
	 * will be changed - {@link PropertyValidator#valueAlreadySetForEntityAndNotChangedNow(EntityEntry, String, Object)}
	 * i.e. we need to find an EntityEntry based on instance only. But of course, if the target instance is null, we
	 * have to use the normalized reference. Note that this is OK for us, because if the target instance is null, we
	 * know that no query would return an entity which corresponds to the given normalized reference (because we already
	 * tried the query with given reference and it returned null, thus making the target instance null).
	 */
	private GenericEntity getKeyForEntityEntry(ManipulationSecurityExpositionContext context) {
		GenericEntity instance = context.getTargetInstance();
		return instance != null ? instance : context.getNormalizedTargetReference();
	}

	// ####################################
	// ## . . . . . VALIDATION . . . . . ##
	// ####################################

	@Override
	public void validate(ManipulationSecurityContext context) {
		new PropertyValidator(context).executeValidation();
	}

	private static class PropertyValidator {
		private final Map<GenericEntity, EntityEntry> entityEntries;
		private final ManipulationSecurityContext context;

		private final Map<PropertyValueDescriptor, Set<GenericEntity>> valueDescriptorToSettingEntity = newMap();
		private final Set<GenericEntity> deletedEntities = newSet();

		PropertyValidator(ManipulationSecurityContext context) {
			this.context = context;
			this.entityEntries = context.getExpertContext();

			for (Entry<GenericEntity, EntityEntry> entry : entityEntries.entrySet()) {
				GenericEntity entity = entry.getKey();
				EntityEntry entityEntry = entry.getValue();

				if (entityEntry.deleted) {
					deletedEntities.add(entityEntry.instance);
					continue;
				}

				String typeSignature = entityEntry.typeSignature();

				for (Entry<String, PropertyValueEntry> setPropertyEntry : entityEntry.propertiesSet.entrySet()) {
					String propertyName = setPropertyEntry.getKey();
					PropertyValueEntry propertyEntry = setPropertyEntry.getValue();

					acquireSettingEntities(typeSignature, propertyName, propertyEntry.newValue).add(entity);
				}
			}
		}

		void executeValidation() {
			for (EntityEntry entityEntry : entityEntries.values())
				validate(entityEntry);
		}

		private void validate(EntityEntry entry) {
			if (entry.deleted || entry.propertiesSet.isEmpty())
				return;

			String typeSignature = entry.typeSignature();

			for (Entry<String, PropertyValueEntry> setProperties : entry.propertiesSet.entrySet()) {
				String propertyName = setProperties.getKey();
				PropertyValueEntry propertyEntry = setProperties.getValue();

				if (propertyEntry.newValue == null)
					// if we set some value to null, we do not need to check anything (that will not cause any
					// violation)
					continue;

				if (/* isPropertyUnique(typeSignature, propertyName) && */valueNotUnique(entry, propertyName, propertyEntry.newValue)) {
					SecurityViolationEntry validationEntry = SecurityViolationEntry.T.create();
					validationEntry.setEntityReference(entry.entityReference);
					validationEntry.setPropertyName(propertyName);
					validationEntry.setCausingManipulation(propertyEntry.manipulation);
					validationEntry.setDescription("[UniqueKey]" + typeSignature + "#" + propertyName + ": " + propertyEntry.newValue);

					context.addViolationEntry(validationEntry);
				}
			}
		}

		private boolean valueNotUnique(EntityEntry entry, String propertyName, Object newValue) {
			return valueBeingSetForOtherEntityAsWell(entry, propertyName, newValue)
					|| valueAlreadySetForEntityAndNotChangedNow(entry, propertyName, newValue);

		}

		private boolean valueBeingSetForOtherEntityAsWell(EntityEntry entry, String propertyName, Object newValue) {
			PropertyValueDescriptor pvd = new PropertyValueDescriptor(entry.typeSignature(), propertyName, newValue);
			Set<GenericEntity> settingEntities = valueDescriptorToSettingEntity.get(pvd);

			return (settingEntities.size() > 1);
		}

		private boolean valueAlreadySetForEntityAndNotChangedNow(EntityEntry entry, String propertyName, Object newValue) {
			GenericEntity entity = queryEntityByPropertyValue(entry, propertyName, newValue);

			if (entity == null || entity.equals(entry.instance) || deletedEntities.contains(entity))
				/* if no entity with given value exists, or if it exists, but is the one we are setting this for right
				 * now or if we are deleting the entity with such value -> No violation */
				return false;

			/* if the entity with such value exists, we check if the value will not be changed to something else - if it
			 * is --> No violation */
			return !valueWillBeChangedNow(entity, propertyName, newValue);
		}

		private GenericEntity queryEntityByPropertyValue(EntityEntry entry, String propertyName, Object newValue) {
			PersistenceGmSession session = context.getSession();

			EntityQuery query = EntityQueryBuilder.from(entry.typeSignature()).where().property(propertyName).eq(newValue).done();

			List<GenericEntity> entities = executeQuery(session, query);

			if (CollectionTools.isEmpty(entities))
				return null;

			if (entities.size() > 1)
				// This should never happen (querying on unique property should not result in multiple entities)
				throw new IllegalStateException("Unexpected query result. Querying unique property '" + propertyName
						+ "' resulted in multiple entities being returned. Entities: " + entities);

			return entities.get(0);
		}

		private boolean valueWillBeChangedNow(GenericEntity entity, String propertyName, Object oldValue) {
			EntityEntry entityEntry = entityEntries.get(entity);
			if (entityEntry == null)
				return false;

			PropertyValueEntry propertyValueEntry = entityEntry.propertiesSet.get(propertyName);
			if (propertyValueEntry == null)
				return false;

			// also check if new value is different than old value, if it is, return false (no change)
			return !oldValue.equals(propertyValueEntry.newValue);
		}

		private List<GenericEntity> executeQuery(PersistenceGmSession session, EntityQuery query) {
			return session.query().entities(query).list();
		}

		private Set<GenericEntity> acquireSettingEntities(String typeSignature, String propertyName, Object newValue) {
			PropertyValueDescriptor pvd = new PropertyValueDescriptor(typeSignature, propertyName, newValue);

			return acquireSet(valueDescriptorToSettingEntity, pvd);
		}

		static class PropertyValueDescriptor {
			private static final Object NULL_OBJECT = new Object();

			String typeSignature;
			String propertyName;
			Object propertyValue;

			public PropertyValueDescriptor(String typeSignature, String propertyName, Object propertyValue) {
				this.typeSignature = typeSignature;
				this.propertyName = propertyName;
				this.propertyValue = propertyValue != null ? propertyValue : NULL_OBJECT;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}

				if (!(obj instanceof PropertyValueDescriptor)) {
					return false;
				}
				PropertyValueDescriptor other = (PropertyValueDescriptor) obj;

				return typeSignature.equals(other.typeSignature) //
						&& propertyName.equals(other.propertyName) //
						&& propertyValue.equals(other.propertyValue);
			}

			@Override
			public int hashCode() {
				// Property name may be omitted, that would not bring much
				return 31 * typeSignature.hashCode() + propertyValue.hashCode();
			}
		}
	}

	/** This may be overridden by sub-class if other method should be used to find out if property is mandatory. */
	protected boolean isPropertyUnique(String typeSignature, String propertyName, ManipulationSecurityContext context) {
		return context.getCmdResolver().getMetaData() //
				.entityTypeSignature(typeSignature) //
				.property(propertyName) //
				.is(Unique.T);
	}

}
