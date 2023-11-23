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
package com.braintribe.model.processing.query.support;

import java.util.Collection;
import java.util.Iterator;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.eval.api.repo.DelegatingRepository;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.processing.query.eval.api.repo.Repository;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.conditions.Condition;

/**
 * This is a wrapper for an actual repository which does it's own
 * 
 * @author peter.gazdik
 */
public class IdentityManagedRepository implements IndexingRepository, DelegatingRepository {

	private final Repository repository;
	private final boolean canAdoptEntities;
	private final DelegatingRepository delegatingRepository;
	private final IndexingRepository indexingRepository;
	private final ManagedGmSession session;

	public IdentityManagedRepository(Repository repository, boolean canAdoptEntities, ManagedGmSession session) {
		this.repository = repository;
		this.canAdoptEntities = canAdoptEntities;
		this.delegatingRepository = repository instanceof DelegatingRepository ? (DelegatingRepository) repository : null;
		this.indexingRepository = repository instanceof IndexingRepository ? (IndexingRepository) repository : null;
		this.session = session;
	}

	// #############################################
	// ## . . . . . Repository methods . . . . . .##
	// #############################################

	@Override
	public Iterable<? extends GenericEntity> providePopulation(String typeSignature) {
		return new MergingIterable<>(repository.providePopulation(typeSignature));
	}

	@Override
	public GenericEntity resolveReference(EntityReference reference) {
		GenericEntity entity = session.query().entity(reference).find();
		if (entity != null)
			return entity;
		else
			return merge(repository.resolveReference(reference));
	}

	@Override
	public String defaultPartition() {
		return repository.defaultPartition();
	}

	// ############################################
	// ## . . Delegating Repository methods . . .##
	// ############################################

	@Override
	public Iterable<? extends GenericEntity> provideEntities(String typeSignature, Condition condition, Ordering ordering) {
		return new MergingIterable<>(delegatingRepository.provideEntities(typeSignature, condition, ordering));
	}

	@Override
	public boolean supportsFulltextSearch() {
		return delegatingRepository.supportsFulltextSearch();
	}

	// ############################################
	// ## . . . IndexingRepository methods . . . ##
	// ############################################

	@Override
	public Collection<? extends GenericEntity> getIndexRange(String indexId, Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		return merge(indexingRepository.getIndexRange(indexId, from, fromInclusive, to, toInclusive));
	}

	@Override
	public Collection<? extends GenericEntity> getFullRange(String indexId, boolean reverseOrder) {
		return merge(indexingRepository.getFullRange(indexId, reverseOrder));
	}

	@Override
	public GenericEntity getValueForIndex(String indexId, Object indexValue) {
		return merge(indexingRepository.getValueForIndex(indexId, indexValue));
	}

	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndex(String indexId, Object indexValue) {
		return merge(indexingRepository.getAllValuesForIndex(indexId, indexValue));
	}

	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndices(String indexId, Collection<?> indexValues) {
		return merge(indexingRepository.getAllValuesForIndices(indexId, indexValues));
	}

	// ############################################
	// ## . . . . . . . . Helpers . . . . . . . .##
	// ############################################

	private <T> T merge(T data) {
		if (data == null)
			return null;

		try {
			return session.merge().adoptUnexposed(canAdoptEntities).doFor(data);

		} catch (GmSessionException e) {
			throw new RuntimeException("Merging entities failed!", e);
		}
	}

	@Override
	public IndexInfo provideIndexInfo(String typeSignature, String propertyName) {
		return indexingRepository != null ? indexingRepository.provideIndexInfo(typeSignature, propertyName) : null;
	}

	private class MergingIterable<T> implements Iterable<T> {
		private final Iterable<T> iterable;

		public MergingIterable(Iterable<T> iterable) {
			this.iterable = iterable;
		}

		@Override
		public Iterator<T> iterator() {
			return new MergingIterator<T>(iterable.iterator());
		}
	}

	private class MergingIterator<T> implements Iterator<T> {
		private final Iterator<T> iterator;

		public MergingIterator(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			T next = iterator.next();
			return merge(next);
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}
}
