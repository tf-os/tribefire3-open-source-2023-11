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
package com.braintribe.model.access.collaboration;

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.collaboration.StageStats;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * @author peter.gazdik
 */
/* package */ class StageRegistry {

	private final Map<String, PersistenceStage> stageByName = new ConcurrentHashMap<>();

	private final Map<PersistenceStage, Set<GenericEntity>> stageToEntities = new ConcurrentHashMap<>();
	private final Map<GenericEntity, PersistenceStage> entityToStage = new ConcurrentHashMap<>();
	private final Map<PersistenceStage, StageStatsImpl> stageToStats = new ConcurrentHashMap<>();

	public void indexStages(Stream<PersistenceStage> allStages) {
		allStages.forEach(this::onNewStage);
	}

	public StageStats getStageStats(String name) {
		PersistenceStage stage = getStage(name);

		return acquireStats(stage);
	}

	public PersistenceStage getStage(String name) {
		return stageByName.computeIfAbsent(name, n -> {
			throw new NotFoundException("Stage not found: " + name);
		});
	}

	public void onManipulation(Manipulation m, PersistenceStage stage) {
		switch (m.manipulationType()) {
			case COMPOUND:
				for (Manipulation nestedManipulation : ((CompoundManipulation) m).getCompoundManipulationList())
					onManipulation(nestedManipulation, stage);
				return;

			case ADD:
			case REMOVE:
			case CHANGE_VALUE:
			case CLEAR_COLLECTION:
				acquireStats(stage).updates++;
				return;

			case DELETE:
				acquireStats(stage).deletes++;
				unregister(((DeleteManipulation) m).getEntity());
				return;

			case INSTANTIATION:
				acquireStats(stage).instantiations++;
				register(((InstantiationManipulation) m).getEntity(), stage);
				return;

			default:
				return;
		}
	}

	private StageStatsImpl acquireStats(PersistenceStage stage) {
		return stageToStats.computeIfAbsent(stage, s -> new StageStatsImpl());
	}

	private void register(GenericEntity entity, PersistenceStage stage) {
		entityToStage.put(entity, stage);
		acquireSet(stageToEntities, stage).add(entity);
	}

	private void unregister(GenericEntity entity) {
		PersistenceStage stage = entityToStage.remove(entity);
		stageToEntities.get(stage).remove(entity);
	}

	public PersistenceStage findStage(GenericEntity entity) {
		return entityToStage.get(entity);
	}

	public PersistenceStage getStage(GenericEntity entity) {
		return entityToStage.computeIfAbsent(entity, e -> {
			throw new NotFoundException("Stage not found for entity: " + entity);
		});
	}

	public void onNewStage(PersistenceStage stage) {
		stageByName.put(stage.getName(), stage);
	}

	public void onPersistenceStageRename(String oldName, String newName) {
		PersistenceStage stage = getStage(oldName);

		if (!newName.equals(stage.getName()))
			throw new IllegalArgumentException("Stage was not renamed as expected. Expected: '" + oldName + "' -> '" + newName
					+ "', but actual new name is: " + stage.getName());

		stageByName.remove(oldName);
		stageByName.put(newName, stage);
	}

	public void mergeFirstStageToSecond(String sourceStageName, String targetStageName) {
		PersistenceStage sourceStage = stageByName.get(sourceStageName);
		PersistenceStage targetStage = stageByName.get(targetStageName);

		if (sourceStage == null || targetStage == null)
			throw new IllegalArgumentException(String.format("At least one stage was not found. Source: %s for name %s, Target: %s for name %s",
					sourceStage, sourceStageName, targetStage, targetStageName));

		updateEntityStageMappings(sourceStage, targetStage);
		updateStageStats(sourceStage, targetStage);
	}

	private void updateEntityStageMappings(PersistenceStage sourceStage, PersistenceStage targetStage) {
		Set<GenericEntity> sourceEntities = stageToEntities.get(sourceStage);
		if (isEmpty(sourceEntities))
			return;

		acquireSet(stageToEntities, targetStage).addAll(sourceEntities);

		for (GenericEntity sourceEntity : sourceEntities)
			entityToStage.replace(sourceEntity, sourceStage, targetStage);
	}

	private void updateStageStats(PersistenceStage sourceStage, PersistenceStage targetStage) {
		StageStatsImpl sourceStats = acquireStats(sourceStage);
		StageStatsImpl targetStats = acquireStats(targetStage);

		targetStats.add(sourceStats);
		stageToStats.remove(sourceStage);
	}

	public Set<GenericEntity> getEntitiesForStage(String stageName) {
		PersistenceStage stage = requireNonNull(stageByName.get(stageName), "Unkown stage:" + stageName);
		Set<GenericEntity> entities = stageToEntities.get(stage);
		return isEmpty(entities) ? emptySet() : unmodifiableSet(entities);
	}

	static class StageStatsImpl implements StageStats {

		private int instantiations;
		private int updates;
		private int deletes;

		@Override
		public int getInstantiations() {
			return instantiations;
		}

		@Override
		public int getDeletes() {
			return deletes;
		}

		@Override
		public int getUpdates() {
			return updates;
		}

		public void add(StageStatsImpl other) {
			instantiations += other.instantiations;
			deletes += other.deletes;
			updates += other.updates;
		}

	}

}
