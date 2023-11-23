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
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
 * 
 * Contains all the entity data of the {@link Smood}, including indices for quicker access.
 * 
 * @see EntityPopulation
 * 
 *      <p>
 *      Moreover, if an enriched meta-model is given (to the Smood), the corresponding {@link CmdResolver} is passed to this class (by the Smood), so
 *      that additional indices are maintained for all the configured properties. Whether an index has a metric (i.e. is ordered, and can ranges can
 *      be retrieved) depends solely on the type of the property. For exact information on a given type see
 *      {@link SmoodIndexTools#supportsMetric(GenericModelType)}
 */
public class PopulationManager {

	public static final String GLOBAL_ID_INDEX = globalIdIndexId(GenericEntity.T.getTypeSignature());

	public final RepositoryInfo repositoryInfo = new RepositoryInfoImpl();

	/** Population from the outside, used when re-indexing. */
	private final Collection<GenericEntity> population;

	/** Maps entity type to corresponding {@link EntityPopulation} */
	protected final Map<EntityType<?>, EntityPopulation> entityPopulations = newMap();
	/** @see #acquireSuperTypes(EntityType) */
	protected final Map<EntityType<?>, Iterable<EntityType<?>>> superTypes = newMap();

	/** Maps index-id to an index (all indices are stored here). */
	protected final Map<String, LookupIndex> allIndices = newMap(); // keys are indexIds
	/** Maps index-id to a metric index (i.e. non-id index on property with the right type). */
	protected final Map<String, MetricIndex> metricIndices = newMap(); // keys are indexIds

	protected final MetaDataResolver mdResolver = new MetaDataResolver();

	protected static final MetricIndex EMPTY_INDEX = new MultiMetricIndex(GenericModelTypeReflection.TYPE_INTEGER);

	protected final LookupIndex globalIdIndex = newGlobalIdIndex();
	protected final Map<String, LookupIndex> signatureToGlobalIdIndex = asMap(GenericEntity.T.getTypeSignature(), globalIdIndex);

	public PopulationManager(Collection<GenericEntity> population) {
		this.population = population;

	}

	private static LookupIndex newGlobalIdIndex() {
		LookupIndex result = new UniqueLookupIndex();

		IndexInfoImpl indexInfo = result.getIndexInfo();
		indexInfo.setEntitySignature(GenericEntity.T.getTypeSignature());
		indexInfo.setIndexId(GLOBAL_ID_INDEX);
		indexInfo.setPropertyName(GenericEntity.globalId);
		indexInfo.setHasMetric(false);

		return result;
	}

	/**
	 * Sets the {@link CmdResolver}, which is important to figure out what properties are indexed. See description of this class
	 * ({@link PopulationManager}) for more details.
	 * 
	 * @see Index
	 */
	public void setCmdResolver(CmdResolver cmdResolver) {
		this.mdResolver.setCmdResolver(cmdResolver);

		forceReIndexing();
	}

	/**
	 * We re-index all the entities, but we keep the original id indices. That is necessary, because in some cases when resolving meta-data for
	 * entities (regarding which properties are indexed), the "cortex" access might trigger a {@link PropertyQuery} which is identifying the owner by
	 * an id - i.e. we would have an exception due to an entity missing in the Smood if we got rid of the id-index.
	 */
	private void forceReIndexing() {
		allIndices.clear();
		metricIndices.clear();
		entityPopulations.clear();

		for (Entry<String, LookupIndex> entry : signatureToGlobalIdIndex.entrySet())
			allIndices.put(globalIdIndexId(entry.getKey()), entry.getValue());

		for (GenericEntity entity : population)
			registerEntity(entity);
	}

	public <T extends GenericEntity> Collection<T> getEntirePopulation(EntityType<?> entityType) {
		return (Collection<T>) getIdIndex(entityType).allValues();
	}

	public LookupIndex getIdIndex(EntityType<?> entityType) {
		return acquireEntityPopulation(entityType).getIdIndex();
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

	/**
	 * Returns {@link IndexInfo} for entity type given by signature and property name.
	 * <p>
	 * NOTE that the indices are 'initialized' lazily, at the moment when entity of given type is registered via (
	 * {@link #registerEntity(GenericEntity)}). This means, if no entity of given type was ever registered within the smood, this method will return
	 * <tt>null</tt>.
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
	public void registerEntity(GenericEntity entity) {
		acquireEntityPopulation(entity.entityType()).registerEntity(entity);
	}

	/** Removes given entity from the population all the relevant indices. */
	public void removeEntity(GenericEntity entity) {
		acquireEntityPopulation(entity.entityType()).removeEntity(entity);
	}

	/**
	 * Method that updates the indices when some property was changed.
	 * <p>
	 * We assume entity is {@link EnhancedEntity}. How else would could we be notified with a {@link ChangeValueManipulation}.
	 */
	public void onChangeValue(GenericEntity entity, String propertyName, Object oldValue, Object newValue) {
		EntityType<GenericEntity> et = entity.entityType();
		Property p = et.getProperty(propertyName);
		acquireEntityPopulation(et).onChangeValue(entity, p, oldValue, newValue);
	}

	/**
	 * Returns a set containing given {@link EntityType} and all it's supertypes (i.e. all types given type is assignable to).
	 */
	protected Iterable<EntityType<?>> acquireSuperTypes(EntityType<?> et) {
		return superTypes.computeIfAbsent(et, _et -> _et.getTransitiveSuperTypes(false, true));
	}

	protected EntityPopulation acquireEntityPopulation(EntityType<?> et) {
		EntityPopulation result = entityPopulations.get(et);
		if (result == null)
			entityPopulations.put(et, result = new EntityPopulation(et, this));

		return result;
	}

	public Set<EntityType<?>> getUsedTypes() {
		return Collections.unmodifiableSet(superTypes.keySet());
	}

	public LookupIndex acquireGlobalIdIndex(String typeSignature) {
		return signatureToGlobalIdIndex.computeIfAbsent(typeSignature, this::provideTypeFilteringGlobalIdIndex);
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

}
