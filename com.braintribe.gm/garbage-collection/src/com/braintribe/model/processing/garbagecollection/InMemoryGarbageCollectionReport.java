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
package com.braintribe.model.processing.garbagecollection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.common.lcd.Empty;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.genericmodel.GMCoreTools;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.NullSafe;

/**
 * Holds data about a GC run, i.e. which entities have been deleted, how many per type, etc.
 *
 * @author michael.lafite
 */
public class InMemoryGarbageCollectionReport implements GarbageCollectionReport {

	private static final String NL = CommonTools.LINE_SEPARATOR;
	private static final String SEPARATOR_LINE = "--------------------------------------------------------------------------------"
			+ NL;
	private static final String HEADER_LINE = "----------------------- GarbageCollection Report -------------------------------"
			+ NL;

	private static final String INDENTATION = "  ";

	private enum EntitySetType {
		SUBSET, ROOT, REACHABLE, DELETED
	}

	private final List<GarbageCollectionSubsetReport> subsetReports;

	public InMemoryGarbageCollectionReport(final List<GarbageCollectionSubsetReport> subsetReports) {
		this.subsetReports = subsetReports;
	}

	@Override
	public String createReport(final GarbageCollectionReportSettings settings) {
		final StringBuilder builder = new StringBuilder();

		final GarbageCollectionEntityInfoGetter combinedEntityInfoGetter = new GarbageCollectionReportEntityInfoGetter();

		builder.append(SEPARATOR_LINE);
		builder.append(SEPARATOR_LINE);
		builder.append(HEADER_LINE);
		builder.append(SEPARATOR_LINE);
		builder.append(NL);
		builder.append("Successfully performed garbage collection on "
				+ CommonTools.getCountAndSingularOrPlural(this.subsetReports, "subset") + "." + NL);
		appendStatistics(builder, combinedEntityInfoGetter);

		for (final GarbageCollectionSubsetReport subsetReport : NullSafe.iterable(this.subsetReports)) {
			builder.append(SEPARATOR_LINE);
			builder.append("Statistics for subset " + subsetReport.subsetId + ":" + NL);
			appendStatistics(builder, subsetReport);

			if (settings.isListIndividualEntities()) {
				final List<String> entityTypeSignatures = CollectionTools
						.getSortedList(subsetReport.getEntityTypeSignatures(EntitySetType.DELETED));

				builder.append(NL);
				builder.append("Deleted entities:" + NL);

				if (!entityTypeSignatures.isEmpty()) {
					for (final String entityTypeSignature : entityTypeSignatures) {
						final List<GarbageCollectionEntityInfo> deletedEntities = CollectionTools
								.getSortedList(subsetReport.getEntities(EntitySetType.DELETED, entityTypeSignature));
						builder.append("EntityType " + entityTypeSignature + " ("
								+ GMCoreTools.getEntityCountString(deletedEntities) + " deleted):" + NL);
						for (final GarbageCollectionEntityInfo deletedEntity : deletedEntities) {
							builder.append(INDENTATION + deletedEntity.stringRepresentation + NL);
						}
					}
				} else {
					builder.append(INDENTATION + "<none>" + NL);
				}

			}
		}

		builder.append(SEPARATOR_LINE);
		builder.append(SEPARATOR_LINE);
		builder.append(NL);

		return builder.toString();
	}

	private static void appendStatistics(final StringBuilder builder,
			final GarbageCollectionEntityInfoGetter entityInfoGetter) {
		final Set<GarbageCollectionEntityInfo> subsetEntities = entityInfoGetter.getEntities(EntitySetType.SUBSET);
		final Set<GarbageCollectionEntityInfo> rootEntities = entityInfoGetter.getEntities(EntitySetType.ROOT);
		final Set<GarbageCollectionEntityInfo> reachableEntities = entityInfoGetter
				.getEntities(EntitySetType.REACHABLE);
		final Set<GarbageCollectionEntityInfo> deletedEntities = entityInfoGetter.getEntities(EntitySetType.DELETED);

		builder.append(INDENTATION + "Subset entities: " + subsetEntities.size() + NL);
		builder.append(INDENTATION + "Root entities: " + rootEntities.size() + NL);
		builder.append(INDENTATION + "Reachable entities: " + reachableEntities.size() + NL);
		builder.append(INDENTATION + "Deleted entities: " + deletedEntities.size() + NL);
		builder.append(NL);

		builder.append("Entity type statistics (subset/root/reachable/deleted):" + NL);
		final List<String> entityTypeSignatures = CollectionTools
				.getSortedList(entityInfoGetter.getEntityTypeSignatures(EntitySetType.SUBSET));
		for (final String entityTypeSignature : entityTypeSignatures) {
			builder.append(INDENTATION + "EntityType " + entityTypeSignature + ": "
					+ entityInfoGetter.getEntities(EntitySetType.SUBSET, entityTypeSignature).size() + "/"
					+ entityInfoGetter.getEntities(EntitySetType.ROOT, entityTypeSignature).size() + "/"
					+ entityInfoGetter.getEntities(EntitySetType.REACHABLE, entityTypeSignature).size() + "/"
					+ entityInfoGetter.getEntities(EntitySetType.DELETED, entityTypeSignature).size() + NL);
		}
	}

	private class GarbageCollectionReportEntityInfoGetter implements GarbageCollectionEntityInfoGetter {

		@Override
		public Set<GarbageCollectionEntityInfo> getEntities(final EntitySetType entitySetType) {
			return getEntities(entitySetType, null);
		}

		@Override
		public Set<GarbageCollectionEntityInfo> getEntities(final EntitySetType entitySetType,
				final String entityTypeSignature) {
			final Set<GarbageCollectionEntityInfo> entities = new HashSet<GarbageCollectionEntityInfo>();
			for (final GarbageCollectionSubsetReport subsetReport : NullSafe
					.iterable(InMemoryGarbageCollectionReport.this.subsetReports)) {
				entities.addAll(subsetReport.getEntities(entitySetType, entityTypeSignature));
			}
			return entities;
		}

		@Override
		public Set<String> getEntityTypeSignatures(final EntitySetType entitySetType) {
			final Set<String> typeSignatures = new HashSet<String>();
			for (final GarbageCollectionSubsetReport subsetReport : NullSafe
					.iterable(InMemoryGarbageCollectionReport.this.subsetReports)) {
				typeSignatures.addAll(subsetReport.getEntityTypeSignatures(entitySetType));
			}
			return typeSignatures;
		}

	}

	private interface GarbageCollectionEntityInfoGetter {

		Set<String> getEntityTypeSignatures(EntitySetType entitySetType);

		Set<GarbageCollectionEntityInfo> getEntities(EntitySetType entitySetType);

		Set<GarbageCollectionEntityInfo> getEntities(EntitySetType entitySetType, String entityTypeSignature);
	}

	public static class GarbageCollectionSubsetReport implements GarbageCollectionEntityInfoGetter {

		private final String subsetId;

		private final Map<EntitySetType, Map<String, Set<GarbageCollectionEntityInfo>>> entitySetTypeToMapOfTypeSignatureToEntitySet = new HashMap<EntitySetType, Map<String, Set<GarbageCollectionEntityInfo>>>();
		private final Map<EntitySetType, Set<GarbageCollectionEntityInfo>> entitySetTypeToEntitySet = new HashMap<EntitySetType, Set<GarbageCollectionEntityInfo>>();

		public GarbageCollectionSubsetReport(final String subsetId, final Set<GenericEntity> subsetEntities,
				final Set<GenericEntity> rootEntities, final Set<GenericEntity> reachableEntities) {
			this.subsetId = subsetId;
			for (final EntitySetType entitySetType : EntitySetType.values()) {
				this.entitySetTypeToMapOfTypeSignatureToEntitySet.put(entitySetType,
						new HashMap<String, Set<GarbageCollectionEntityInfo>>());
				this.entitySetTypeToEntitySet.put(entitySetType, new HashSet<GarbageCollectionEntityInfo>());
			}

			addToEntitySet(subsetEntities, EntitySetType.SUBSET);
			addToEntitySet(rootEntities, EntitySetType.ROOT);
			addToEntitySet(reachableEntities, EntitySetType.REACHABLE);
		}

		@Override
		public Set<String> getEntityTypeSignatures(final EntitySetType entitySetType) {
			return this.entitySetTypeToMapOfTypeSignatureToEntitySet.get(entitySetType).keySet();
		}

		@Override
		public Set<GarbageCollectionEntityInfo> getEntities(final EntitySetType entitySetType) {
			return getEntities(entitySetType, null);
		}

		@Override
		public Set<GarbageCollectionEntityInfo> getEntities(final EntitySetType entitySetType,
				final String typeSignature) {

			Set<GarbageCollectionEntityInfo> result = null;

			if (typeSignature == null) {
				result = this.entitySetTypeToEntitySet.get(entitySetType);
			} else {

				final Map<String, Set<GarbageCollectionEntityInfo>> typeSignaturesToEntities = this.entitySetTypeToMapOfTypeSignatureToEntitySet
						.get(entitySetType);

				if (typeSignaturesToEntities.containsKey(typeSignature)) {
					result = typeSignaturesToEntities.get(typeSignature);
				} else {
					result = Empty.set();
				}
			}

			return result;
		}

		public void entityDeleted(final GenericEntity entity) {
			addToEntitySet(entity, EntitySetType.DELETED);
		}

		public void entitiesDeleted(final Set<GenericEntity> entities) {
			addToEntitySet(entities, EntitySetType.DELETED);
		}

		private void addToEntitySet(final Set<GenericEntity> entities, final EntitySetType entitySetType) {
			for (final GenericEntity entity : NullSafe.iterable(entities)) {
				addToEntitySet(entity, entitySetType);
			}
		}

		private void addToEntitySet(final GenericEntity entity, final EntitySetType entitySetType) {
			final GarbageCollectionEntityInfo entityInfo = new GarbageCollectionEntityInfo(entity);

			{
				final Map<String, Set<GarbageCollectionEntityInfo>> typeSignaturesToEntities = this.entitySetTypeToMapOfTypeSignatureToEntitySet
						.get(entitySetType);

				if (!typeSignaturesToEntities.containsKey(entityInfo.entityTypeSignature)) {
					typeSignaturesToEntities.put(entityInfo.entityTypeSignature,
							new HashSet<GarbageCollectionEntityInfo>());
				}

				final Set<GarbageCollectionEntityInfo> typeSignatureSpecificEntitySet = typeSignaturesToEntities
						.get(entityInfo.entityTypeSignature);

				if (typeSignatureSpecificEntitySet.contains(entityInfo)) {
					throw new IllegalArgumentException(
							"Error while adding entity info to set! Entity info already contained! "
									+ CommonTools.getParametersString("entity", entity, "entitySetType", entitySetType,
											"entityTypeSignature", entityInfo.entityTypeSignature,
											"typeSignatureSpecificEntitySet", typeSignatureSpecificEntitySet));
				}
				typeSignatureSpecificEntitySet.add(entityInfo);
			}

			{
				final Set<GarbageCollectionEntityInfo> entitySet = getEntities(entitySetType);
				if (entitySet.contains(entityInfo)) {
					throw new IllegalArgumentException(
							"Error while adding entity info to set! Entity info already contained! "
									+ CommonTools.getParametersString("entity", entity, "entitySetType", entitySetType,
											"entitySet", entitySet));
				}
				entitySet.add(entityInfo);
			}
		}
	}

	public static class GarbageCollectionEntityInfo implements Comparable<GarbageCollectionEntityInfo> {
		private final String stringRepresentation;
		private final String entityTypeSignature;

		public GarbageCollectionEntityInfo(final GenericEntity entity) {
			Arguments.notNull(entity);
			this.stringRepresentation = entity.toString();
			this.entityTypeSignature = entity.entityType().getTypeSignature();
		}

		@Override
		public int compareTo(final GarbageCollectionEntityInfo other) {
			int result = this.stringRepresentation.compareTo(other.stringRepresentation);
			if (result == 0) {
				result = this.stringRepresentation.compareTo(other.stringRepresentation);
			}
			return result;
		}

		@Override
		public int hashCode() {
			return this.stringRepresentation.hashCode() + this.entityTypeSignature.hashCode();
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof GarbageCollectionEntityInfo) {
				final GarbageCollectionEntityInfo otherGarbageCollectionEntityInfo = (GarbageCollectionEntityInfo) other;
				if (this.stringRepresentation.equals(otherGarbageCollectionEntityInfo.stringRepresentation)
						&& this.entityTypeSignature.equals(otherGarbageCollectionEntityInfo.entityTypeSignature)) {
					return true;
				}
			}
			return false;
		}
	}

}
