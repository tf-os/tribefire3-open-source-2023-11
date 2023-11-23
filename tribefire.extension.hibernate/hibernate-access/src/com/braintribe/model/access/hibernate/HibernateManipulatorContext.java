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
package com.braintribe.model.access.hibernate;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Owner;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.manipulator.api.ManipulatorContext;
import com.braintribe.model.processing.manipulator.api.ReferenceDetacher;
import com.braintribe.model.processing.manipulator.expert.basic.AbstractDeleteManipulator;
import com.braintribe.model.processing.manipulator.expert.basic.AbstractManipulatorContext;
import com.braintribe.model.processing.manipulator.expert.basic.ChangeValueManipulator;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.session.api.managed.NotFoundException;

public class HibernateManipulatorContext extends AbstractManipulatorContext {

	private final Map<PersistentEntityReference, GenericEntity> entitiesToDelete = newMap();
	private final HibernateApplyStatistics statistics;
	private final HibernateAccess access;
	private final Session session;

	private final Map<EntityReference, GenericEntity> references = CodingMap.create(newLinkedMap(), EntRefHashingComparator.INSTANCE);

	private final CmdResolver cmdResolver;

	public HibernateManipulatorContext(Map<EntityReference, GenericEntity> loadedEntities, HibernateAccess access, Session session,
			HibernateApplyStatistics statistics) {
		this.access = access;
		this.cmdResolver = access.getCmdResolver();
		this.session = session;
		this.statistics = statistics;

		// set specific experts
		deleteManipulator = new HibernateDeleteManipulator();
		changeValueManipulator = new HibernateChangeValueManipulator();

		references.putAll(loadedEntities);
	}

	public Map<PersistentEntityReference, GenericEntity> getEntitiesToDelete() {
		return entitiesToDelete;
	}

	public Map<EntityReference, GenericEntity> getReferences() {
		return references;
	}

	public Map<PreliminaryEntityReference, GenericEntity> getPreliminaryReferenceMap() {
		return (Map<PreliminaryEntityReference, GenericEntity>) (Map<?, ?>) references.entrySet().stream() //
				.filter(e -> e.getKey() instanceof PreliminaryEntityReference) //
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public GenericEntity createPreliminaryEntity(GenericEntity preliminaryEntityReference) {
		PreliminaryEntityReference reference = (PreliminaryEntityReference) preliminaryEntityReference;
		EntityType<GenericEntity> entityType = typeReflection.getType(reference.getTypeSignature());
		GenericEntity entity = entityType.createPlainRaw();

		String partition = reference.getRefPartition();
		if (partition != null)
			entity.setPartition(partition);

		references.put(reference, entity);

		return entity;
	}

	@Override
	public void deleteEntityIfPreliminary(GenericEntity ref) {
		references.remove(ref);
	}

	@Override
	protected GenericEntity resolveEntity(EntityReference ref) {
		GenericEntity result = references.get(ref);
		if (result == null)
			throw new NotFoundException("Entity not found for reference: " + ref);

		return result;
	}

	private class HibernateDeleteManipulator extends AbstractDeleteManipulator<Void> {
		private final ReferenceDetacher referenceDetacher;

		public HibernateDeleteManipulator() {
			this.setPropertyReferenceAnalyzer(access.getPropertyReferenceAnalyzer());

			this.referenceDetacher = new HibernateReferenceDetacher(access, cmdResolver, session);
		}

		@Override
		protected ReferenceDetacher getReferenceDetacher() {
			return referenceDetacher;
		}

		@Override
		protected void deleteActualEntity(GenericEntity entityToDelete, DeleteMode deleteMode, Void deleteContext) {
			if (entityToDelete.getId() == null)
				throw new GenericModelException("Cannot delete non-persistent entity: " + entityToDelete);

			PersistentEntityReference persistentEntityReference = (PersistentEntityReference) HibernateAccessTools.createReference(entityToDelete);

			try {
				GenericEntity entity = references.remove(persistentEntityReference);
				entitiesToDelete.put(persistentEntityReference, entity);

			} catch (Exception e) {
				throw new GenericModelException("Could not delete entity with reference: " + persistentEntityReference.getRefId() + " of type: "
						+ persistentEntityReference.getTypeSignature(), e);
			}
		}

		@Override
		protected void failIfEntityReferenced(GenericEntity entityToDelete, Set<QualifiedProperty> propertiesToDetach) {
			// NOOP - we let the actual RDBMS fail
		}

		// No context-handling regarding detaching entities is needed

		@Override
		protected Void onBeforeDelete() {
			return null;
		}

		@Override
		protected void onBeforeDetach(Void deleteContext) {
			// Intentionally left empty
		}

		@Override
		protected void onAfterDetach(Void deleteContext) {
			// Intentionally left empty
		}

	}

	private class HibernateChangeValueManipulator extends ChangeValueManipulator {

		@Override
		public void apply(ChangeValueManipulation manipulation, ManipulatorContext context) {
			statistics.increaseValueChanges();
			Owner owner = manipulation.getOwner();
			if (owner instanceof EntityProperty) {
				EntityProperty entityProperty = (EntityProperty) owner;
				EntityReference ref = entityProperty.getReference();
				GenericEntity entity = references.get(ref);

				if (entity != null) {
					EntityType<?> entityType = typeReflection.getType(ref.getTypeSignature());
					Property property = entityType.getProperty(entityProperty.getPropertyName());
					Object propertyValue = property.get(entity);

					// update reference mappings in case the changed property was the id property
					if (property.isIdentifier()) {
						Object persistenceId = manipulation.getNewValue();
						PersistentEntityReference _ref = PersistentEntityReference.T.createPlain();
						_ref.setTypeSignature(entityType.getTypeSignature());
						_ref.setRefId(persistenceId);
						_ref.setRefPartition(ref.getRefPartition());
						references.put(_ref, entity);
					}

					if (property.isPartition()) {
						String partition = (String) manipulation.getNewValue();
						EntityReference _ref = GmReflectionTools.makeShallowCopy(ref);
						_ref.setPartition(partition);
						references.put(_ref, entity);
					}

					// workaround hibernate collection behavior: use existing collection by clearing it instead of
					// setting the new value
					if (propertyValue != null) {
						GenericModelType propertyType = property.getType();
						if (propertyType.isCollection()) {
							CollectionType collectionType = (CollectionType) propertyType;
							// instead of setting a new collection we clear the existing one and fill it with the new elements.
							switch (collectionType.getCollectionKind()) {
								case set:
								case list:
									Collection<Object> newCollectionValue = resolveValue(manipulation.getNewValue());
									Collection<Object> collectionValue = (Collection<Object>) propertyValue;
									collectionValue.clear();
									if (newCollectionValue != null)
										collectionValue.addAll(newCollectionValue);
									return;

								case map:
									Map<Object, Object> newMapValue = resolveValue(manipulation.getNewValue());
									Map<Object, Object> mapValue = (Map<Object, Object>) propertyValue;
									mapValue.clear();
									if (newMapValue != null)
										mapValue.putAll(newMapValue);
									return;
							}
						}
					}
				}
			}
			super.apply(manipulation, context);
		}
	}

	public void registerEntityReference(EntityReference entityReference, GenericEntity entity) {
		references.put(entityReference, entity);
	}
}
