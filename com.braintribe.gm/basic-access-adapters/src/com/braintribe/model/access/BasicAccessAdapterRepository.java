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
package com.braintribe.model.access;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.braintribe.model.access.api.LookupIndex;
import com.braintribe.model.access.api.MetricIndex;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.repo.DelegatingRepository;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.eval.api.repo.IndexingRepository;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.conditions.Condition;

/**
 * TODO
 * 
 * {@link BasicAccessAdapter} should have methods to set {@link #assumeIdIsIndexed} and also to register an index
 * 
 * Test how it is to code against this API
 * 
 */
public class BasicAccessAdapterRepository implements IndexingRepository, DelegatingRepository {

	protected static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected final BasicAccessAdapter access;
	protected final Map<String, String> noIndices = new ConcurrentHashMap<String, String>(); // key is indexId
	protected final Map<String, IndexInfo> indexInfos = new ConcurrentHashMap<String, IndexInfo>(); // key is indexId
	protected final Map<String, LookupIndex> indices = new ConcurrentHashMap<String, LookupIndex>(); // key is indexId

	private boolean assumeIdIsIndexed = false;

	public BasicAccessAdapterRepository(BasicAccessAdapter access) {
		this.access = access;
	}

	/**
	 * If this flag is set to <tt>true</tt>, the repository would by default consider all id properties to be indexed,
	 * and for each such property would register a {@link LookupIndex} which resolves the values using
	 * {@link BasicAccessAdapter#getEntity(EntityReference)} method. This of course mean, one should only set this flag
	 * if that method has an efficient implementation
	 * <p>
	 * Note that setting this flag with the default implementation of this method would cause an infinite loop.
	 */
	public void setAssumeIdIsIndexed(boolean assumeIdIsIndexed) {
		if (assumeIdIsIndexed && !access.getIgnorePartitions())
			throw new IllegalArgumentException("Cannot set 'assumeIdIsIndexed' to true when not ignoring partitions.."
					+ " Note that the default for ignorePartitions is true, so check your code where you are setting it to false and remove it.");

		this.assumeIdIsIndexed = assumeIdIsIndexed;
	}

	protected boolean getAssumeIdIsIndexed() {
		return assumeIdIsIndexed;
	}

	// #############################################
	// ## . . . . . Repository methods . . . . . ##
	// #############################################

	/** {@inheritDoc} */
	@Override
	public Iterable<? extends GenericEntity> providePopulation(String typeSignature) {
		try {
			// return access.loadPopulation(typeSignature);
			return access.queryPopulation(typeSignature, null, null);

		} catch (ModelAccessException e) {
			throw new RuntimeQueryEvaluationException("Error while loading population for type:" + typeSignature, e);
		}
	}

	/** {@inheritDoc} */
	// @Override
	@Override
	public Iterable<? extends GenericEntity> provideEntities(String typeSignature, Condition condition, Ordering ordering) {
		try {
			return access.queryPopulation(typeSignature, condition, ordering);

		} catch (ModelAccessException e) {
			throw new RuntimeQueryEvaluationException("Error while loading population for type:" + typeSignature, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public GenericEntity resolveReference(EntityReference reference) {
		try {
			return access.getEntity(reference);

		} catch (ModelAccessException e) {
			throw new RuntimeQueryEvaluationException("Entity reference could not be resolved: " + reference, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String defaultPartition() {
		return access.getIgnorePartitions() ? access.defaultPartition : null;
	}

	@Override
	public boolean supportsFulltextSearch() {
		return access.supportsFulltextSearch();
	}

	// #############################################
	// ## . . . IndexingRepository methods . . . ##
	// #############################################

	/**
	 * Finds registered {@link MetricIndex} by given <tt>indexId</tt>, and delegates to it's
	 * {@link MetricIndex#getIndexRange(Object, Boolean, Object, Boolean)} method.
	 */
	@Override
	public Collection<? extends GenericEntity> getIndexRange(String indexId, Object from, Boolean fromInclusive, Object to, Boolean toInclusive) {
		return resolveMetricIndex(indexId).getIndexRange(from, fromInclusive, to, toInclusive);
	}

	/**
	 * Finds registered {@link MetricIndex} by given <tt>indexId</tt>, and delegates to it's
	 * {@link MetricIndex#getIndexRange(Object, Boolean, Object, Boolean)} method.
	 */
	@Override
	public Collection<? extends GenericEntity> getFullRange(String indexId, boolean reverseOrder) {
		return resolveMetricIndex(indexId).getFullRange(reverseOrder);
	}

	private MetricIndex resolveMetricIndex(String indexId) {
		LookupIndex index = getIndex(indexId);
		if (!(index instanceof MetricIndex))
			throw new RuntimeException("Index is not metric: " + indexId);

		return (MetricIndex) index;
	}

	/**
	 * Finds registered {@link LookupIndex} by given <tt>indexId</tt>, and delegates to it's
	 * {@link LookupIndex#getAllValuesForIndices(Collection)} method.
	 */
	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndices(String indexId, Collection<?> indexValues) {
		return getIndex(indexId).getAllValuesForIndices(indexValues);
	}

	/**
	 * Finds registered {@link LookupIndex} by given <tt>indexId</tt>, and delegates to it's
	 * {@link LookupIndex#getAllValuesForIndex(Object)} method.
	 */
	@Override
	public Collection<? extends GenericEntity> getAllValuesForIndex(String indexId, Object indexValue) {
		return getIndex(indexId).getAllValuesForIndex(indexValue);
	}

	/**
	 * Finds registered {@link LookupIndex} by given <tt>indexId</tt>, and delegates to it's
	 * {@link LookupIndex#getAllValuesForIndex(Object)} method.
	 */
	@Override
	public GenericEntity getValueForIndex(String indexId, Object indexValue) {
		return getIndex(indexId).getValueForIndex(indexValue);
	}

	protected LookupIndex getIndex(String indexId) {
		LookupIndex result = indices.get(indexId);
		if (result == null)
			throw new RuntimeException("Index not found: " + indexId);

		return result;
	}

	/**
	 * This method provides {@link IndexInfo} for given property. If no information about index for given property
	 * exists, this method tries to initialize it by asking for {@link #newIndex(String, String)}. Depending on the
	 * result of this method, it either registers the returned {@link LookupIndex}, or (if the result was <tt>null</tt>)
	 * remembers that there is no index on given property.
	 * <p>
	 * When access implementations want to define custom indices, the method to override is
	 * {@link #newIndex(String, String)}.
	 */
	@Override
	public IndexInfo provideIndexInfo(String typeSignature, String propertyName) {
		String indexId = toIndexId(typeSignature, propertyName);

		if (noIndices.containsKey(indexId))
			return null;

		IndexInfo result = indexInfos.get(indexId);

		if (result == null) {
			LookupIndex index = newIndex(typeSignature, propertyName);
			result = registerIndex(typeSignature, propertyName, index);
		}

		return result;
	}

	/**
	 * In general, this method should return a {@link LookupIndex} for given property, or <tt>null</tt>, if the property
	 * is not indexed. For supporting custom logic which determines if given property is indexed, this method is the one
	 * to override.
	 * <p>
	 * The default implementation checks the {@link #setAssumeIdIsIndexed(boolean) assumeIdIsIndexed} flag, and if set
	 * to <tt>true</tt> and if given property is an <tt>id</tt> of given entity, then this method returns a
	 * {@link LookupIndex} which resolves values using {@link BasicAccessAdapter#getEntity(EntityReference)} method.
	 * <p>
	 * Note that if your index also supports ranges (i.e. retrieving entities for values in a certain range), the index
	 * has to implement {@link MetricIndex} in order for the framework to take advantage of it.
	 * 
	 * @see #setAssumeIdIsIndexed(boolean)
	 * @see LookupIndex
	 * @see MetricIndex
	 */
	protected LookupIndex newIndex(String typeSignature, String propertyName) {
		return assumeIdIsIndexed ? newIdIndex(typeSignature, propertyName) : null;
	}

	protected final LookupIndex newIdIndex(String typeSignature, String propertyName) {
		EntityType<?> entityType = typeReflection.getEntityType(typeSignature);
		Property property = entityType.getProperty(propertyName);

		return property.isIdentifier() ? new IdLookupIndex(typeSignature) : null;
	}

	class IdLookupIndex implements LookupIndex {
		private final String typeSignature;

		public IdLookupIndex(String typeSignature) {
			this.typeSignature = typeSignature;
		}

		@Override
		public Collection<? extends GenericEntity> getAllValuesForIndices(Collection<?> ids) {
			List<PersistentEntityReference> references = ids.stream() //
					.map(this::asReference) //
					.collect(Collectors.toList());

			try {
				return access.getEntities(references);

			} catch (ModelAccessException e) {
				throw new RuntimeQueryEvaluationException("Error while resolving references. ", e);
			}
		}

		@Override
		public Collection<? extends GenericEntity> getAllValuesForIndex(Object id) {
			return Arrays.asList(getValueForIndex(id));
		}

		@Override
		public GenericEntity getValueForIndex(Object id) {
			PersistentEntityReference reference = asReference(id);
			return BasicAccessAdapterRepository.this.resolveReference(reference);
		}

		private PersistentEntityReference asReference(Object id) {
			PersistentEntityReference result = PersistentEntityReference.T.createPlain();
			result.setTypeSignature(typeSignature);
			result.setRefId(id);

			return result;
		}

	}

	/**
	 * Registers given index for given property. Note that the <tt>index</tt> parameter may also be <tt>null</tt>, thus
	 * stating that there is no index for given property.
	 * 
	 * @return {@link IndexInfo} describing given {@link LookupIndex}, or <tt>null</tt>, if given index was
	 *         <tt>null</tt>
	 */
	public final IndexInfo registerIndex(String typeSignature, String propertyName, LookupIndex index) {
		String indexId = toIndexId(typeSignature, propertyName);

		if (index == null) {
			noIndices.put(indexId, indexId);
			return null;
		}

		IndexInfo indexInfo = new BasicIndexInfo(indexId, typeSignature, propertyName, index instanceof MetricIndex);
		indexInfos.put(indexId, indexInfo);
		indices.put(indexId, index);

		return indexInfo;
	}

	protected String toIndexId(String typeSignature, String propertyName) {
		return typeSignature + "#" + propertyName;
	}

	public static class BasicIndexInfo implements IndexInfo {
		private final String indexId;
		private final String typeSignature;
		private final String propertyName;
		private final boolean hasMetric;

		public BasicIndexInfo(String indexId, String typeSignature, String propertyName, boolean hasMetric) {
			this.indexId = indexId;
			this.typeSignature = typeSignature;
			this.propertyName = propertyName;
			this.hasMetric = hasMetric;
		}

		@Override
		public String getIndexId() {
			return indexId;
		}

		/**
		 * This method always returns <tt>false</tt> since our only index access is using the
		 * {@link BasicAccessAdapter#getEntity(EntityReference)} method (which obviously does not support ranges).
		 */
		@Override
		public boolean hasMetric() {
			return hasMetric;
		}

		@Override
		public String getEntitySignature() {
			return typeSignature;
		}

		@Override
		public String getPropertyName() {
			return propertyName;
		}
	}
}
