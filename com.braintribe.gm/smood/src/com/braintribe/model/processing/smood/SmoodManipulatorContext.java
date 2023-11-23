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
package com.braintribe.model.processing.smood;

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulator.expert.basic.AbstractManipulatorContext;
import com.braintribe.model.processing.smood.manipulation.AbsentCollectionIgnoringAddManipulator;
import com.braintribe.model.processing.smood.manipulation.AbsentCollectionIgnoringRemoveManipulator;
import com.braintribe.utils.lcd.StringTools;

/**
 * 
 */
public class SmoodManipulatorContext extends AbstractManipulatorContext {

	private final Smood smood;
	private final GmSession session;
	private boolean isLocalRequest;
	private boolean checkRefereesOnDelete;
	private boolean manifestUnknownEntities;

	private final Map<PreliminaryEntityReference, GenericEntity> instantiations = CodingMap.create(newLinkedMap(), EntRefHashingComparator.INSTANCE);
	private final Map<EntityType<?>, Set<GenericEntity>> manifestations = newMap();

	public SmoodManipulatorContext(Smood smood) {
		this.smood = smood;
		this.session = smood.getGmSession();
		this.changeValueManipulator = smood.getChangeValueManipulator();
		this.deleteManipulator = smood.getDeleteManipulator(DeleteMode.dropReferences);
		this.manifestionManipulator = smood.getManifestationManipulator();
	}

	public Map<PreliminaryEntityReference, GenericEntity> getInstantiations() {
		return instantiations;
	}

	public Map<EntityType<?>, Set<GenericEntity>> getManifestations() {
		return manifestations;
	}

	public void setIsLocalRequest(boolean isLocalRequest) {
		this.isLocalRequest = isLocalRequest;

		setValueResolution(!isLocalRequest);
	}

	/** @see ManipulationApplicationBuilder#checkRefereesOnDelete(boolean) */
	public void setCheckRefereesOnDelete(boolean checkRefereesOnDelete) {
		this.checkRefereesOnDelete = checkRefereesOnDelete;
	}

	/** @see ManipulationApplicationBuilder#checkRefereesOnDelete(boolean) */
	public boolean getCheckRefereesOnDelete() {
		return checkRefereesOnDelete;
	}

	public void setIgnoreAbsentCollectionManipulations(boolean ignoreAbsentCollectionManipulations) {
		if (ignoreAbsentCollectionManipulations) {
			this.addManipulator = AbsentCollectionIgnoringAddManipulator.INSTANCE;
			this.bulkRemoveFromCollectionManipulator = AbsentCollectionIgnoringRemoveManipulator.INSTANCE;
		}
	}

	public void setManifestUnknownEntities(boolean manifestUnknownEntities) {
		this.manifestUnknownEntities = manifestUnknownEntities;
	}

	@Override
	protected GenericEntity resolveEntity(EntityReference entityReference) {
		// first lookup for entity in the instantiations done on this context
		// TODO add this prelim check to HibernateManipulationContext
		GenericEntity entity = entityReference instanceof PreliminaryEntityReference ? instantiations.get(entityReference) : null;
		if (entity != null)
			return entity;

		if (manifestUnknownEntities) {
			entity = smood.findEntity(entityReference);
			return entity == null ? manifestEntity(entityReference) : entity;

		} else {
			return smood.getEntity(entityReference);
		}
	}

	/**
	 * This method creates a new instance (for remote case), or simply uses the given entity as the desired entity (local case). In both cases, this
	 * method MAKES SURE that the ENTITY IS ATTACHED TO THE SESSION, and therefore (since the session than calls
	 * {@link GmSession#noticeManipulation(Manipulation)}) this entity is ALSO REGISTERED IN THE SMOOD.
	 * <p>
	 * In case the entity has no id, an entry will be registered in the map retrievable via {@link #getInstantiations()} . The only post-processing
	 * these entities might need is assigning an automatic id, if it was desired and the id was not set by some other manipulation, which comes after
	 * the instantiation.
	 */
	@Override
	public GenericEntity createPreliminaryEntity(GenericEntity entityOrReference) {
		PreliminaryEntityReference reference;
		GenericEntity entity;

		if (isLocalRequest) {
			entity = entityOrReference;
			smood.registerEntity(entity, false);

			EntityReference ref = entity.reference();
			if (ref instanceof PersistentEntityReference)
				return entityOrReference;

			reference = (PreliminaryEntityReference) ref;

		} else if (entityOrReference instanceof PreliminaryEntityReference) {
			reference = (PreliminaryEntityReference) entityOrReference;
			EntityType<GenericEntity> entityType = typeReflection.getType(reference.getTypeSignature());
			/* All initialization manipulations are tracked, that's how session.create is implemented. Thus, this must use createRaw. */
			entity = session.createRaw(entityType);

			String partition = reference.getRefPartition();
			if (partition != null)
				entity.setPartition(partition);

		} else
			throw new GenericModelException("invalid entity value for InstantiationManipulation: " + entityOrReference);

		instantiations.put(reference, entity);

		return entity;
	}

	private GenericEntity manifestEntity(EntityReference entityReference) {
		if (!(entityReference instanceof PersistentEntityReference))
			throw new GenericModelException("Cannot manifest non-persistence reference: " + entityReference);

		EntityType<GenericEntity> et = typeReflection.getType(entityReference.getTypeSignature());
		GenericEntity entity = et.createRaw();

		if (!StringTools.isEmpty(entityReference.getRefPartition()))
			entity.setPartition(entityReference.getRefPartition());

		for (Property p : et.getProperties()) {
			if (p.isIdentifier())
				p.set(entity, entityReference.getRefId());
			else if (!p.isPartition())
				p.setAbsenceInformation(entity, GMF.absenceInformation());
		}

		session.attach(entity);

		acquireSet(manifestations, et).add(entity);

		return entity;
	}

	@Override
	public void deleteEntityIfPreliminary(GenericEntity entityOrReference) {
		if (isLocalRequest) {
			/* This whole "local" request ist BS. We simply attach the entity to Smood, so in case it already had an id, it is not part of
			 * instantiations */
			if (entityOrReference.getId() == null)
				instantiations.remove(entityOrReference.reference());

		} else {
			instantiations.remove(entityOrReference);
		}
	}

}
