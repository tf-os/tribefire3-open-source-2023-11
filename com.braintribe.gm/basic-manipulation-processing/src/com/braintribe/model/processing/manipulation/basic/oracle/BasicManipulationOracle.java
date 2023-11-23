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
package com.braintribe.model.processing.manipulation.basic.oracle;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.singleton;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.manipulation.api.AtomicManipulationOracle;
import com.braintribe.model.processing.manipulation.api.ManipulationOracle;
import com.braintribe.model.processing.manipulation.api.PersistenceReferenceResolver;
import com.braintribe.model.processing.session.api.managed.NotFoundException;

/**
 * @author peter.gazdik
 */
public class BasicManipulationOracle implements ManipulationOracle {

	/* Not that this Oracle assumes the manipulations are normalized, and that if id/partition are assigned, it will be
	 * the first two manipulations after instantiation. They also assume these properties are assigned only once and are
	 * not editable since.
	 * 
	 * This also doesn't allow to resolve the PersistenceEntityReference which existed after id was assigned but before
	 * partition was assigned if both were assigned (i.e. the owner of the CVM that assigns the partition.) This is a
	 * PersistenceEntityReference whose partition is null, unlike for the rest of the manipulations, and we don't do
	 * anything to handle this case.
	 * 
	 * Later with more normalization this will not be needed anyway - we will say the original manipulation is used
	 * throughout the entire stack, thus we will never encounter anything other than a PreliminaryEntityReference for
	 * the original stack (for the new entities that is), and when dealing with the induced manipulation, it will make
	 * sure that both id and partition are available. */

	private final Manipulation manipulation;
	private Manipulation inducedManipulation;
	private boolean didInternalRefResolution = false;

	private final PersistenceReferenceResolver referenceResolver;

	/* Internal ref is any ref used inside that manipulation stack, e.g. a preliminary one, or a persistent one whose
	 * partition was not assigned yet, but id was. */
	private final Map<EntityReference, PersistentEntityReference> resolvedInternalRefs = CodingMap.create(EntRefHashingComparator.INSTANCE);
	private final Map<EntityReference, GenericEntity> resolutionCache = CodingMap.create(EntRefHashingComparator.INSTANCE);

	public BasicManipulationOracle(Manipulation manipulation, PersistenceReferenceResolver referenceResolver) {
		Objects.requireNonNull(manipulation, "Manipulation of an oracle cannot be null");
		Objects.requireNonNull(referenceResolver, "PersistenceReferenceResolver of an oracle cannot be null");

		this.manipulation = manipulation;
		this.referenceResolver = referenceResolver;
	}

	public void setInducedManipulation(Manipulation inducedManipulation) {
		this.inducedManipulation = inducedManipulation;
	}

	@Override
	public Manipulation getManipulation() {
		return manipulation;
	}

	@Override
	public Manipulation getInducedManipulation() {
		return inducedManipulation;
	}

	@Override
	public AtomicManipulationOracle forAtomic(AtomicManipulation manipulation) {
		return new BasicAtomicManipulationOracle(this, manipulation);
	}

	@Override
	public GenericEntity resolve(EntityReference reference) {
		resolveAll(reference.getTypeSignature(), singleton(reference));
		return getResolved(reference);
	}

	@Override
	public GenericEntity getResolved(EntityReference reference) {
		return resolutionCache.computeIfAbsent(reference, r -> {
			throw new NotFoundException("Reference was not resolved yet: " + reference);
		});
	}

	@Override
	public Optional<GenericEntity> findResolved(EntityReference reference) {
		return Optional.ofNullable(resolutionCache.get(reference));
	}

	@Override
	public void resolveAll(Iterable<EntityReference> references) {
		resolveAll(null, references);
	}

	@Override
	public void resolveAll(String typeSignature, Iterable<EntityReference> references) {
		Map<EntityReference, PersistentEntityReference> persistentReferences = toPersistentReferences(references);
		Map<PersistentEntityReference, GenericEntity> resolved = referenceResolver.resolve(typeSignature, persistentReferences.values());

		cacheResult(persistentReferences, resolved);
	}

	private Map<EntityReference, PersistentEntityReference> toPersistentReferences(Iterable<EntityReference> references) {
		Map<EntityReference, PersistentEntityReference> result = newMap();
		List<PreliminaryEntityReference> prelimRefs = newList();

		for (EntityReference ref : references) {
			if (ref.referenceType() == EntityReferenceType.persistent)
				result.put(ref, (PersistentEntityReference) ref);
			else
				prelimRefs.add((PreliminaryEntityReference) ref);
		}

		if (prelimRefs.isEmpty())
			return result;

		ensureInternalRefsResolvable();

		for (PreliminaryEntityReference prelimRef : prelimRefs)
			result.put(prelimRef, getPeristentRefFor(prelimRef));

		return result;
	}

	private void cacheResult(Map<EntityReference, PersistentEntityReference> persistentRefs, Map<PersistentEntityReference, GenericEntity> resolved) {
		for (Entry<EntityReference, PersistentEntityReference> entry : persistentRefs.entrySet()) {
			EntityReference internalRef = entry.getKey();
			PersistentEntityReference persistenceRef = entry.getValue();

			GenericEntity entity = resolved.get(persistenceRef);

			resolutionCache.put(internalRef, entity);
			if (internalRef.referenceType() != EntityReferenceType.persistent)
				resolutionCache.put(persistenceRef, entity);
		}
	}

	private PersistentEntityReference getPeristentRefFor(EntityReference internalRef) {
		return resolvedInternalRefs.computeIfAbsent(internalRef, p -> {
			throw new NotFoundException("Preliminary reference was not part of manipulation stack of this oracle: " + internalRef);
		});
	}

	private void ensureInternalRefsResolvable() {
		if (didInternalRefResolution)
			return;

		if (inducedManipulation == null)
			throw new IllegalStateException("Cannot resolve preliminary references at this point, as no induced manipulation was provided yet."
					+ " The oracle thus assumes the manipluation stack was not commited yet, and this attempt to resolve preliminary references is a mistake.");

		resolvedInternalRefs.putAll(InternalReferenceResolver.resolve(manipulation, inducedManipulation));
		didInternalRefResolution = true;
	}

}
