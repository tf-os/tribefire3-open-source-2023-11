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
package com.braintribe.model.processing.smood.population;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.newConcurrentMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.query.Index;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.eval.api.repo.RepositoryInfo;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.smood.population.index.FilteringLookupIndex;
import com.braintribe.model.processing.smood.population.index.LookupIndex;
import com.braintribe.model.processing.smood.population.index.MetricIndex;
import com.braintribe.model.processing.smood.population.index.MultiMetricIndex;
import com.braintribe.model.processing.smood.population.index.UniqueLookupIndex;
import com.braintribe.model.processing.smood.population.info.IndexInfoImpl;
import com.braintribe.model.processing.smood.population.info.RepositoryInfoImpl;
import com.braintribe.model.query.PropertyQuery;

/**
 * Contains all the entity data of the {@link Smood}, including indices for quicker access.
 * <p>
 * Moreover, if an enriched meta-model is given (to the Smood), the corresponding {@link CmdResolver} is passed here (by
 * the Smood), so that additional indices are maintained for all the configured properties. Whether an index has a
 * metric (i.e. is ordered, and ranges can be retrieved) depends on the type of the property and the
 * {@link Index#getIndexType() meta data configuration}. For exact information on a given type see
 * {@link SmoodIndexTools#supportsMetric(GenericModelType)}
 * 
 * @see EntityPopulation
 */
public class PopulationManager {

	public static final String GLOBAL_ID_INDEX_ID = globalIdIndexId(GenericEntity.T.getTypeSignature());

	public final RepositoryInfo repositoryInfo = new RepositoryInfoImpl();

	/** Population from the outside, used when re-indexing. */
	private final Collection<GenericEntity> population;

	/** Maps entity type to corresponding {@link EntityPopulation} */
	private final Map<EntityType<?>, EntityPopulation> entityPopulations = newConcurrentMap();
	/** @see #acquireSuperTypes(EntityType) */
	private final Map<EntityType<?>, Iterable<EntityType<?>>> superTypes = newConcurrentMap();

	/** Maps index-id to an index (all indices are stored here). */
	private final Map<String, LookupIndex> allIndices = newConcurrentMap(); // keys are indexIds
	/** Maps index-id to a metric index (i.e. non-id index on property with the right type). */
	private final Map<String, MetricIndex> metricIndices = newConcurrentMap(); // keys are indexIds

	protected final MetaDataResolver mdResolver = new MetaDataResolver();

	private static final MetricIndex EMPTY_INDEX = new MultiMetricIndex(GenericModelTypeReflection.TYPE_INTEGER);

	protected final LookupIndex globalIdIndex = newGlobalIdIndex();
	private final Map<String, LookupIndex> signatureToGlobalIdIndex = asMap(GenericEntity.T.getTypeSignature(), globalIdIndex);
	private final ReentrantLock signatureToGlobalIdIndexLock = new ReentrantLock();

	public PopulationManager(Collection<GenericEntity> population) {
		this.population = population;
	}

	private static LookupIndex newGlobalIdIndex() {
		LookupIndex result = new UniqueLookupIndex();

		IndexInfoImpl indexInfo = result.getIndexInfo();
		indexInfo.setEntitySignature(GenericEntity.T.getTypeSignature());
		indexInfo.setIndexId(GLOBAL_ID_INDEX_ID);
		indexInfo.setPropertyName(GenericEntity.globalId);
		indexInfo.setHasMetric(false);

		return result;
	}

	/**
	 * Sets the {@link CmdResolver}, which is important to figure out what properties are indexed. See description of this
	 * class ({@link PopulationManager}) for more details.
	 * 
	 * @see Index
	 */
	public void w_setCmdResolver(CmdResolver cmdResolver) {
		this.mdResolver.setCmdResolver(cmdResolver);

		w_forceReIndexing();
	}

	/**
	 * We re-index all the entities, but we keep the original id indices. That is necessary, because in some cases when
	 * resolving meta-data for entities (regarding which properties are indexed), the "cortex" access might trigger a
	 * {@link PropertyQuery} which is identifying the owner by an id - i.e. we would have an exception due to an entity
	 * missing in the Smood if we got rid of the id-index.
	 */
	private void w_forceReIndexing() {
		allIndices.clear();
		metricIndices.clear();
		entityPopulations.clear();

		for (Entry<String, LookupIndex> entry : signatureToGlobalIdIndex.entrySet())
			allIndices.put(globalIdIndexId(entry.getKey()), entry.getValue());

		for (GenericEntity entity : population)
			w_registerEntity(entity);
	}

	public <T extends GenericEntity> Collection<T> getEntirePopulation(EntityType<?> entityType) {
		return (Collection<T>) getIdIndex(entityType).allValues();
	}

	public LookupIndex getIdIndex(EntityType<?> entityType) {
		EntityPopulation result = entityPopulations.get(entityType);
		return result != null ? result.getIdIndex() : EMPTY_INDEX;
	}

	public MetricIndex getMetricIndex(String indexId) {
		MetricIndex result = metricIndices.get(indexId);
		if (result == null && allIndices.containsKey(indexId))
			throw new IllegalStateException("Index with id '" + indexId + "' is not metric!");

		return result != null ? result : EMPTY_INDEX;
	}

	public LookupIndex getLookupIndex(String indexId) {
		LookupIndex result = allIndices.get(indexId);
		return result != null ? result : EMPTY_INDEX;
	}

	protected LookupIndex getIndexOrNull(String indexId) {
		return allIndices.get(indexId);
	}

	/**
	 * Returns {@link IndexInfo} for entity type given by signature and property name.
	 * <p>
	 * NOTE that the indices are 'initialized' lazily, at the moment when entity of given type is registered via (
	 * {@link #w_registerEntity(GenericEntity)}). This means, if no entity of given type was ever registered within the
	 * smood, this method will return <tt>null</tt>.
	 */
	public IndexInfo provideIndexInfo(String typeSignature, String propertyName) {
		if (GenericEntity.globalId.equals(propertyName))
			return acquireGlobalIdIndex(typeSignature).getIndexInfo();

		LookupIndex index = allIndices.get(SmoodIndexTools.indexId(typeSignature, propertyName));
		return index != null ? index.getIndexInfo() : null;
	}

	private static String globalIdIndexId(String typeSignature) {
		return typeSignature + ":" + GenericEntity.globalId;
	}

	/** Adds entity to the population for given type and handles all the indices of course. */
	public void w_registerEntity(GenericEntity entity) {
		w_acquireEntityPopulation(entity.entityType()).w_registerEntity(entity);
	}

	/** Removes given entity from the population all the relevant indices. */
	public void w_removeEntity(GenericEntity entity) {
		w_acquireEntityPopulation(entity.entityType()).w_removeEntity(entity);
	}

	/**
	 * Method that updates the indices when some property was changed.
	 * <p>
	 * We assume entity is {@link EnhancedEntity}. How else would could we be notified with a
	 * {@link ChangeValueManipulation}.
	 */
	public void w_onChangeValue(GenericEntity entity, String propertyName, Object oldValue, Object newValue) {
		EntityType<GenericEntity> et = entity.entityType();
		Property p = et.getProperty(propertyName);
		w_acquireEntityPopulation(et).w_onChangeValue(entity, p, oldValue, newValue);
	}

	/**
	 * Returns a set containing given {@link EntityType} and all it's supertypes (i.e. all types given type is assignable
	 * to).
	 */
	// FYI: this is guarded by a write-lock, but doesn't have to, so I don't change the name
	protected Iterable<EntityType<?>> acquireSuperTypes(EntityType<?> et) {
		return superTypes.computeIfAbsent(et, _et -> _et.getTransitiveSuperTypes(false, true));
	}

	protected EntityPopulation w_acquireEntityPopulation(EntityType<?> et) {
		EntityPopulation result = entityPopulations.get(et);
		if (result == null)
			entityPopulations.put(et, result = EntityPopulation.w_create(et, this));

		return result;
	}

	public Set<EntityType<?>> getUsedTypes() {
		return Collections.unmodifiableSet(superTypes.keySet());
	}

	// This needs synchronization, as two threads creating two LookupIndex instances would be a problem
	// It also needs to be able to create a new instance even when just reading
	public LookupIndex acquireGlobalIdIndex(String typeSignature) {
		LookupIndex lookupIndex = signatureToGlobalIdIndex.get(typeSignature);
		if (lookupIndex != null) {
			return lookupIndex;
		}
		signatureToGlobalIdIndexLock.lock();
		try {
			lookupIndex = signatureToGlobalIdIndex.get(typeSignature);
			if (lookupIndex != null) {
				return lookupIndex;
			}

			lookupIndex = provideTypeFilteringGlobalIdIndex(typeSignature);
			signatureToGlobalIdIndex.put(typeSignature, lookupIndex);

			return lookupIndex;
		} finally {
			signatureToGlobalIdIndexLock.unlock();
		}
	}

	// Note that this method is unreachable for GenericEntity signature, as that entry is in the map from the beginning
	private LookupIndex provideTypeFilteringGlobalIdIndex(String typeSignature) {
		EntityType<?> et = GMF.getTypeReflection().getEntityType(typeSignature);
		LookupIndex result = new FilteringLookupIndex(globalIdIndex, et::isInstance);

		String indexId = globalIdIndexId(typeSignature);
		allIndices.put(indexId, result);

		IndexInfoImpl indexInfo = result.getIndexInfo();
		indexInfo.setEntitySignature(typeSignature);
		indexInfo.setIndexId(indexId);
		indexInfo.setPropertyName(GenericEntity.globalId);
		indexInfo.setHasMetric(false);

		return result;
	}

	protected void w_onNewIndex(LookupIndex propertyIndex) {
		IndexInfoImpl indexInfo = propertyIndex.getIndexInfo();

		allIndices.put(indexInfo.getIndexId(), propertyIndex);

		if (indexInfo.hasMetric())
			metricIndices.put(indexInfo.getIndexId(), (MetricIndex) propertyIndex);

		repositoryInfo.getIndexInfos().add(indexInfo);
	}

}
