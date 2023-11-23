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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.garbagecollection.InMemoryGarbageCollectionReport.GarbageCollectionSubsetReport;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.genericmodel.GMCoreTools;
import com.braintribe.utils.lcd.SetTools;

/**
 * {@link GarbageCollection} implementation that assumes that all entities are available in memory.
 *
 * @author michael.lafite
 */
public class InMemoryGarbageCollection implements GarbageCollection {

	private static final Logger logger = Logger.getLogger(InMemoryGarbageCollection.class);

	private int numberOfEntitiesEntitiesIdeallyDeletedAtOnce = 500;

	@Override
	public GarbageCollectionReport performGarbageCollection(final PersistenceGmSession session,
			final List<SubsetConfiguration> subsetConfigurations, final boolean testModeEnabled)
					throws GarbageCollectionException {

		if (logger.isInfoEnabled()) {
			logger.info("Starting garbage collection ...");
		}

		final List<GarbageCollectionSubsetReport> subsetReports = new ArrayList<GarbageCollectionSubsetReport>();

		if (!CommonTools.isEmpty(subsetConfigurations)) {
			for (final SubsetConfiguration subsetConfiguration : subsetConfigurations) {
				subsetReports.add(performGarbageCollectionOnSubset(session, subsetConfiguration, testModeEnabled));
			}
		} else {
			if (logger.isInfoEnabled()) {
				logger.info(
						"Nothing to do, because no " + SubsetConfiguration.class.getSimpleName() + "s have been set.");
			}
		}
		final InMemoryGarbageCollectionReport garbageCollectionReport = new InMemoryGarbageCollectionReport(
				subsetReports);

		if (logger.isInfoEnabled()) {
			logger.info("Garbage collection finished.");
		}

		return garbageCollectionReport;
	}

	public GarbageCollectionSubsetReport performGarbageCollectionOnSubset(final PersistenceGmSession session,
			final SubsetConfiguration subsetConfiguration, final boolean testModeEnabled)
					throws GarbageCollectionException {

		if (logger.isInfoEnabled()) {
			logger.info("Performing garbage collection on subset " + subsetConfiguration.getSubsetId() + " ...");
		}

		// get all the entities of the current subset
		if (logger.isTraceEnabled()) {
			logger.trace("Finding subset entities ...");
		}
		final Set<GenericEntity> subsetEntities = subsetConfiguration.getSubsetEntitiesFinder().findEntities(session);
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + GMCoreTools.getEntityCountString(subsetEntities, "subset") + ".");
		}

		// get the root entities of this subset, i.e. entities that definitely must not be removed
		if (logger.isTraceEnabled()) {
			logger.trace("Finding root entities ...");
		}
		final Set<GenericEntity> rootEntities = subsetConfiguration.getRootEntitiesFinder().findEntities(session);
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + GMCoreTools.getEntityCountString(rootEntities, "root") + ".");
		}

		// make sure subset contains all root entities
		{
			List<GenericEntity> rootEntitiesThatAreNotPartOfSubset = CollectionTools.getAdditionalElements(rootEntities,
					subsetEntities);
			if (!CommonTools.isEmpty(rootEntitiesThatAreNotPartOfSubset)) {
				throw new GarbageCollectionException("Subset " + subsetConfiguration.getSubsetId()
						+ " does not include all root entities! " + rootEntitiesThatAreNotPartOfSubset.size()
						+ " root entities are not contained in subset: " + rootEntitiesThatAreNotPartOfSubset);
			}
		}

		// find all entities that are reachable from at least one root entity
		if (logger.isTraceEnabled()) {
			logger.trace("Finding reachable entities ...");
		}
		final Set<GenericEntity> reachableEntities = GMCoreTools.findReachableEntities(rootEntities);
		// we don't want to include the root entities
		reachableEntities.removeAll(rootEntities);
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + GMCoreTools.getEntityCountString(reachableEntities, "reachable") + ".");
		}

		// MLA: this could indicate a configuration error, but it could also be necessary some times
		// (e.g. if there is one root entity that is used in multiple subsets.)
		// if (!CollectionTools.containsAll(subsetEntities, reachableEntities)) {
		// throw new GarbageCollectionException(
		// "In subset "
		// + subsetConfiguration.getSubsetId()
		// +
		// " there is at least one entity reachable from the root entities but not included in the set of subset
		// entities!");
		// }

		/*
		 * the whole subset without the reachable entities gives us the unreachable entities, i.e. the ones to be
		 * deleted.
		 */
		if (logger.isTraceEnabled()) {
			logger.trace("Finding unreachable entities ...");
		}
		final Set<GenericEntity> unreachableEntities = new HashSet<GenericEntity>(subsetEntities);
		unreachableEntities.removeAll(rootEntities);
		unreachableEntities.removeAll(reachableEntities);
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + GMCoreTools.getEntityCountString(unreachableEntities, "unreachable") + ".");
		}

		final GarbageCollectionSubsetReport report = new GarbageCollectionSubsetReport(
				subsetConfiguration.getSubsetId(), subsetEntities, rootEntities, reachableEntities);

		/*
		 * Find all independent subsets of entities (i.e. which are not unconnected). All entities of a subset will be
		 * deleted at once, i.e. with the same commit.
		 */
		if (logger.isTraceEnabled()) {
			logger.trace("Searching for independent subsets ...");
		}
		final Set<Set<GenericEntity>> setsOfEntitiesToBeDeletedAtOnce = GMCoreTools
				.findIndependentSubsets(unreachableEntities);
		if (logger.isDebugEnabled()) {
			logger.debug("Found "
					+ CommonTools.getCountAndSingularOrPlural(setsOfEntitiesToBeDeletedAtOnce, "independent subset")
					+ " (that can be deleted separately).");
		}

		final int independentSubsetsCount = setsOfEntitiesToBeDeletedAtOnce.size();
		if (logger.isTraceEnabled()) {
			logger.trace("Trying to merge independent subsets (based on configured target size) ...");
		}
		SetTools.mergeSetsIfPossible(setsOfEntitiesToBeDeletedAtOnce, this.numberOfEntitiesEntitiesIdeallyDeletedAtOnce,
				true);
		if (logger.isDebugEnabled()) {
			logger.debug("Finished merging "
					+ CommonTools.getCountAndSingularOrPlural(independentSubsetsCount, "independent subset")
					+ " (target size is " + this.numberOfEntitiesEntitiesIdeallyDeletedAtOnce + "): "
					+ CommonTools.getParametersString("before", independentSubsetsCount, "after",
							setsOfEntitiesToBeDeletedAtOnce.size()));
		}

		if (logger.isInfoEnabled()) {
			logger.info("Analyzed subset " + subsetConfiguration.getSubsetId() + " which consists of "
					+ GMCoreTools.getEntityCountString(subsetEntities) + " with "
					+ GMCoreTools.getEntityCountString(rootEntities, "root") + " from which "
					+ GMCoreTools.getEntityCountString(reachableEntities, "further") + " "
					+ CommonTools.getSingularOrPlural("is", "are", reachableEntities) + " reachable. This leaves "
					+ GMCoreTools.getEntityCountString(unreachableEntities, "unreachable") + " (separated into "
					+ CommonTools.getCountAndSingularOrPlural(independentSubsetsCount, "independent subset")
					+ ") which will be deleted in "
					+ CommonTools.getCountAndSingularOrPlural(setsOfEntitiesToBeDeletedAtOnce, "step") + ".");
		}

		for (final Set<GenericEntity> entitiesToBeDeletedAtOnce : setsOfEntitiesToBeDeletedAtOnce) {

			if (logger.isTraceEnabled()) {
				logger.trace("Deleting the following " + GMCoreTools.getEntityCountString(entitiesToBeDeletedAtOnce)
						+ ":" + CommonTools.LINE_SEPARATOR + CollectionTools.getStringRepresentation(
								entitiesToBeDeletedAtOnce, "", "  ", "", CommonTools.LINE_SEPARATOR, false));
			} else if (logger.isDebugEnabled()) {
				logger.trace("Deleting " + GMCoreTools.getEntityCountString(entitiesToBeDeletedAtOnce) + " ...");
			}

			if (!testModeEnabled) {
				for (final GenericEntity entityToBeDeleted : entitiesToBeDeletedAtOnce) {
					GMCoreTools.removeEntityReferences(entityToBeDeleted);
				}

				for (final GenericEntity entityToBeDeleted : entitiesToBeDeletedAtOnce) {
					session.deleteEntity(entityToBeDeleted);
				}

				try {
					session.commit();
				} catch (final GmSessionException e) {
					throw new GarbageCollectionException(
							"Error while deleting entities in subset " + subsetConfiguration.getSubsetId() + "!", e);
				}
			}

			report.entitiesDeleted(entitiesToBeDeletedAtOnce);

			if (logger.isDebugEnabled()) {
				logger.debug("Successfully deleted " + GMCoreTools.getEntityCountString(entitiesToBeDeletedAtOnce) + "."
						+ (testModeEnabled ? " (Since test mode is enabled, the actual deletion has been skipped.)"
								: ""));
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Successfully performed garbage collection on subset " + subsetConfiguration.getSubsetId()
					+ ".  (Deleted " + GMCoreTools.getEntityCountString(unreachableEntities) + ".)");
		}

		return report;
	}

	public int getNumberOfEntitiesEntitiesIdeallyDeletedAtOnce() {
		return this.numberOfEntitiesEntitiesIdeallyDeletedAtOnce;
	}

	/**
	 * Defines the number of entities <i>ideally</i> deleted at once. This is just a target setting, i.e. GC will try to
	 * delete about as many entities at once as configured. If it finds less entities to be deleted in a subset, it will
	 * (of course) delete less. Furthermore, if there is a huge set of connected entities with a size greater than the
	 * number configured here, the GC may still delete more entities at once.
	 */
	public void setNumberOfEntitiesEntitiesIdeallyDeletedAtOnce(
			final int numberOfEntitiesEntitiesIdeallyDeletedAtOnce) {
		this.numberOfEntitiesEntitiesIdeallyDeletedAtOnce = numberOfEntitiesEntitiesIdeallyDeletedAtOnce;
	}
}
