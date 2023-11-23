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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.garbagecollection.GarbageCollectionReport.GarbageCollectionReportSettings;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;
import com.braintribe.utils.lcd.CollectionTools;

public class InMemoryGarbageCollectionTest {

	private static Logger logger = Logger
			.getLogger(com.braintribe.model.processing.garbagecollection.InMemoryGarbageCollectionTest.class);

	protected PersistenceGmSession session;

	@Before
	public void setUp() {
		this.session = TestModelTestTools.newSession(TestModelTestTools.newSmoodAccessMemoryOnly("test", TestModelTestTools.createTestModelMetaModel()));
	}

	public static void runGcAndCheckResults(final PersistenceGmSession session, final Set<GenericEntity> subsetEntities,
			final Set<GenericEntity> rootEntities, final Set<GenericEntity> expectedExistingEntities,
			final Set<GenericEntity> expectedRemovedEntities) throws GmSessionException {

		session.commit();

		final InMemoryGarbageCollection garbageCollection = new InMemoryGarbageCollection();
		final List<SubsetConfiguration> subsetConfigurations = new ArrayList<>();
		final PreassignedEntitiesFinder rootPreassignedEntitiesFinder = new PreassignedEntitiesFinder(rootEntities);
		final PreassignedEntitiesFinder subsetPreassignedEntitiesFinder = new PreassignedEntitiesFinder(subsetEntities);
		final SubsetConfiguration conf = new SubsetConfiguration("test", rootPreassignedEntitiesFinder,
				subsetPreassignedEntitiesFinder);
		subsetConfigurations.add(conf);

		final GarbageCollectionReport report = garbageCollection.performGarbageCollection(session, subsetConfigurations,
				false);

		final EntityQuery entityQuery = EntityQuery.T.create();
		entityQuery.setEntityTypeSignature(GenericEntity.class.getName());
		final List<GenericEntity> allEntitiesList = session.query().entities(entityQuery).list();
		final Set<GenericEntity> allEntities = new HashSet<GenericEntity>(allEntitiesList);

		assertThat(CollectionTools.getMissingElements(allEntities, expectedExistingEntities)).isEmpty();
		assertThat(CollectionTools.getMissingElements(expectedExistingEntities, allEntities)).isEmpty();
		assertThat(CollectionTools.getIntersection(allEntities, expectedRemovedEntities)).isEmpty();

		logger.debug(report.createReport(new GarbageCollectionReportSettings()));
	}

	private static Set<GenericEntity> getSet(final GenericEntity... entities) {
		return CollectionTools.getSet(entities);
	}

	//@formatter:off
	/*
	 * Simple graph. One deletion
	 * Created: c1, s1, s2
	 * SubSet: same as created
	 * Root: c1
	 * Final: c1, s1
	 * Deleted: s2
	 */
	//@formatter:on
	@Test
	public void testSimpleGraph() throws GmSessionException {
		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final SimpleEntity s1 = this.session.create(SimpleEntity.T);
		c1.setSimpleEntityProperty(s1);
		final SimpleEntity s2 = this.session.create(SimpleEntity.T);

		final Set<GenericEntity> rootEntities = getSet(c1);
		final Set<GenericEntity> subsetEntities = getSet(c1, s1, s2);
		final Set<GenericEntity> expectedExistingEntities = getSet(c1, s1);
		final Set<GenericEntity> expectedRemovedEntities = getSet(s2);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * No root entries.
	 * Created: c1, c2, s1, s2
	 * SubSet: same as created
	 * Root:
	 * Final: Empty
	 * Deleted: c1, c2, s1, s2
	 */
	//@formatter:on
	@Test
	public void testNoRootEntities() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final SimpleEntity s1 = this.session.create(SimpleEntity.T);
		c1.setSimpleEntityProperty(s1);

		final ComplexEntity c2 = this.session.create(ComplexEntity.T);
		final SimpleEntity s2 = this.session.create(SimpleEntity.T);
		c2.setComplexEntityProperty(c1);
		c1.setSimpleEntityProperty(s2);

		final Set<GenericEntity> rootEntities = getSet();
		final Set<GenericEntity> subsetEntities = getSet(c1, c2, s1, s2);
		final Set<GenericEntity> expectedExistingEntities = getSet();
		final Set<GenericEntity> expectedRemovedEntities = getSet(c1, c2, s1, s2);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Only root entries
	 * Created: c1, c2, ac1
	 * SubSet: same as created
	 * Root: c1, c2, ac1
	 * Final: c1, c2, ac1
	 * Deleted:
	 */
	//@formatter:on
	@Test
	public void testOnlyRootEntities() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity c2 = this.session.create(ComplexEntity.T);
		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);

		final Set<GenericEntity> rootEntities = getSet(c1, c2, ac1);
		final Set<GenericEntity> subsetEntities = getSet(c1, c2, ac1);
		final Set<GenericEntity> expectedExistingEntities = getSet(c1, c2, ac1);
		final Set<GenericEntity> expectedRemovedEntities = getSet();

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Bigger graph. No entries to be deleted. All can be reached
	 * Created: c1, c2, c3, s1, s2, s3, ac1
	 * SubSet: same as created
	 * Root: c1, c2, c3
	 * Relation: (c1,s1) (c2, s2) (c3, ac1)  (ac1, s3)
	 * Final: c1, c2, c3, s1, s2, s3, ac1
	 * Deleted:
	 */
	//@formatter:on
	@Test
	public void testBiggerGraphNoEntitiesRemoved() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity c2 = this.session.create(ComplexEntity.T);
		final ComplexEntity c3 = this.session.create(ComplexEntity.T);

		final SimpleEntity s1 = this.session.create(SimpleEntity.T);
		final SimpleEntity s2 = this.session.create(SimpleEntity.T);
		final SimpleEntity s3 = this.session.create(SimpleEntity.T);

		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);

		c1.setSimpleEntityProperty(s1);
		c2.setSimpleEntityProperty(s2);
		c3.setAnotherComplexEntityProperty(ac1);
		ac1.setSimpleEntityProperty(s3);

		final Set<GenericEntity> rootEntities = getSet(c1, c2, c3);
		final Set<GenericEntity> subsetEntities = getSet(c1, c2, c3, s1, s2, s3, ac1);
		final Set<GenericEntity> expectedExistingEntities = getSet(c1, c2, c3, s1, s2, s3, ac1);
		final Set<GenericEntity> expectedRemovedEntities = getSet();

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Root entity reachable from another entity that is loose.
	 * Created: c1, ac2
	 * SubSet: same as created
	 * Root: c1
	 * Relation: (ac2, c1)
	 * Final: c1
	 * Deleted: ac2
	 */
	//@formatter:on
	@Test
	public void testReachableRootEntityFromLooseEntity() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final AnotherComplexEntity ac2 = this.session.create(AnotherComplexEntity.T);

		ac2.setComplexEntityProperty(c1);

		final Set<GenericEntity> rootEntities = getSet(c1);
		final Set<GenericEntity> subsetEntities = getSet(c1, ac2);
		final Set<GenericEntity> expectedExistingEntities = getSet(c1);
		final Set<GenericEntity> expectedRemovedEntities = getSet(ac2);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Loose entity reachable from an entity of another subset
	 * Created: c1, r1, s1
	 * SubSet1: c1, s1
	 * SubSet2: r1 (will not be passed to the unit tests)
	 * Root: c1
	 * Relation: (r1, s1)
	 * Expected exception because loose entity is referenced and it cannot be deleted
	 * TODO: We must test with more than one session. With one session the deletion is allowed.
	 */
	//@formatter:on
	@Test(expected = GmSessionException.class)
	@Category(KnownIssue.class)
	@Ignore //Does not work
	public void testReachableLooseEntityFromAnothersSubsetEntity() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity r1 = this.session.create(ComplexEntity.T);
		final SimpleEntity s1 = this.session.create(SimpleEntity.T);

		r1.setSimpleEntityProperty(s1);

		final Set<GenericEntity> rootEntities = getSet(c1);
		final Set<GenericEntity> subsetEntities = getSet(c1, s1);

		// the next two sets are not important in this test.
		// an exception must been thrown while performing the garbage collection
		final Set<GenericEntity> expectedExistingEntities = getSet();
		final Set<GenericEntity> expectedRemovedEntities = getSet();

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * A loose entity has a reference to an entity of another subset
	 * Created: c1, ac1, r1
	 * SubSet1: c1, ac1
	 * SubSet2: r1 (will not be passed to the unit tests)
	 * Root: c1
	 * Relation: (ac1, r1)
	 * Final: c1, r1
	 * Deleted: ac1
	 */
	//@formatter:on
	@Test
	public void testReachableAnotherSubsetEntityFromLooseEntity() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity r1 = this.session.create(ComplexEntity.T);
		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);

		ac1.setComplexEntityProperty(r1);

		final Set<GenericEntity> rootEntities = getSet(c1);
		final Set<GenericEntity> subsetEntities = getSet(c1, ac1);

		final Set<GenericEntity> expectedExistingEntities = getSet(c1, r1);
		final Set<GenericEntity> expectedRemovedEntities = getSet(ac1);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * A bound entity has a reference to an entity of another subset
	 * Created: c1, ac1, r1
	 * SubSet1: c1, ac1
	 * SubSet2: r1 (will not be passed to the unit tests)
	 * Root: c1
	 * Relation: (c1, ac1) & (ac1, r1)
	 * Final: c1, ac1, r1
	 * Deleted:
	 */
	//@formatter:on
	@Test
	public void testReachableAnotherSubsetEntityFromBoundEntity() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity r1 = this.session.create(ComplexEntity.T);
		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);

		c1.setAnotherComplexEntityProperty(ac1);
		ac1.setComplexEntityProperty(r1);

		final Set<GenericEntity> rootEntities = getSet(c1);
		final Set<GenericEntity> subsetEntities = getSet(c1, ac1);

		final Set<GenericEntity> expectedExistingEntities = getSet(c1, r1, ac1);
		final Set<GenericEntity> expectedRemovedEntities = getSet();

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Entities from others subset exist in the session, no relations
	 * Created: c1, r1
	 * SubSet1: c1
	 * SubSet2: r1 (will not be passed to the unit tests)
	 * Root: c1
	 * Relation:
	 * Final: c1, r1
	 * Deleted:
	 */
	//@formatter:on
	@Test
	public void testExistingEntitiesFromAnotherSubsetNoRelation() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity r1 = this.session.create(ComplexEntity.T); // entity from another subset

		final Set<GenericEntity> rootEntities = getSet(c1);
		final Set<GenericEntity> subsetEntities = getSet(c1);

		final Set<GenericEntity> expectedExistingEntities = getSet(c1, r1);
		final Set<GenericEntity> expectedRemovedEntities = getSet();

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Two depended loose entities
	 * Created: ac1, ac2
	 * SubSet1: ac1, ac2
	 * Root:
	 * Relation: (ac1,ac2) & (ac2,ac1)
	 * Final:
	 * Deleted: ac1,ac2
	 */
	//@formatter:on
	@Test
	public void testTwoDependedLooseEntities() throws GmSessionException {

		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);
		final AnotherComplexEntity ac2 = this.session.create(AnotherComplexEntity.T);

		ac1.setAnotherComplexEntityProperty(ac2);
		ac2.setAnotherComplexEntityProperty(ac1);

		final Set<GenericEntity> rootEntities = getSet();
		final Set<GenericEntity> subsetEntities = getSet(ac1, ac2);

		final Set<GenericEntity> expectedExistingEntities = getSet();
		final Set<GenericEntity> expectedRemovedEntities = getSet(ac1, ac2);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * A self-referring loose entity
	 * Created: ac1
	 * SubSet1: ac1
	 * Root:
	 * Relation: (ac1, ac1)
	 * Final:
	 * Deleted: ac1
	 */
	//@formatter:on
	@Test
	public void testSelfReferringLooseEntity() throws GmSessionException {

		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);
		ac1.setAnotherComplexEntityProperty(ac1);

		final Set<GenericEntity> rootEntities = getSet();
		final Set<GenericEntity> subsetEntities = getSet(ac1);

		final Set<GenericEntity> expectedExistingEntities = getSet();
		final Set<GenericEntity> expectedRemovedEntities = getSet(ac1);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Two depended root entities, one loose entity
	 * Created: c1, c2, s1
	 * SubSet1: c1, c2, s1
	 * Root:  c1, c2
	 * Relation: (c1, c2) & (c2, c1)
	 * Final: c1, c2
	 * Deleted: s1
	 */
	//@formatter:on
	@Test
	public void testTwoDependedRootEntitiesAndOneLoose() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity c2 = this.session.create(ComplexEntity.T);
		final SimpleEntity s1 = this.session.create(SimpleEntity.T);

		c1.setComplexEntityProperty(c2);
		c2.setComplexEntityProperty(c1);

		final Set<GenericEntity> rootEntities = getSet(c1, c2);
		final Set<GenericEntity> subsetEntities = getSet(c1, c2, s1);

		final Set<GenericEntity> expectedExistingEntities = getSet(c1, c2);
		final Set<GenericEntity> expectedRemovedEntities = getSet(s1);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}

	//@formatter:off
	/*
	 * Complex graph, several root entities, several loose entities, self-referring
	 * Created: c1, c2, c3, c4, c5, s1, s2, s3, s4, s5, s6, ac1, ac2, ac3, ac4
	 * SubSet1: same as created
	 * Root:  c1, c2, c3, c4, c5
	 * Relation: (c1, s1) & (c2, c3) & (c2, s4) & (ac1, s2) & (ac3, s2) & (ac2, ac2) & (ac2, s3) & (ac4, ac4) & (c5, ac4)
	 *           (c5, s6) & (ac4, s5)
	 * Final: c1, s1, ac1, s2, c4, c2, s4, c3, c5, ac4, s6, s5
	 * Deleted: ac3, ac2, s3
	 */
	//@formatter:on
	@Test
	public void testComplexGraph() throws GmSessionException {

		final ComplexEntity c1 = this.session.create(ComplexEntity.T);
		final ComplexEntity c2 = this.session.create(ComplexEntity.T);
		final ComplexEntity c3 = this.session.create(ComplexEntity.T);
		final ComplexEntity c4 = this.session.create(ComplexEntity.T);
		final ComplexEntity c5 = this.session.create(ComplexEntity.T);
		final SimpleEntity s1 = this.session.create(SimpleEntity.T);
		final SimpleEntity s2 = this.session.create(SimpleEntity.T);
		final SimpleEntity s3 = this.session.create(SimpleEntity.T);
		final SimpleEntity s4 = this.session.create(SimpleEntity.T);
		final SimpleEntity s5 = this.session.create(SimpleEntity.T);
		final SimpleEntity s6 = this.session.create(SimpleEntity.T);
		final AnotherComplexEntity ac1 = this.session.create(AnotherComplexEntity.T);
		final AnotherComplexEntity ac2 = this.session.create(AnotherComplexEntity.T);
		final AnotherComplexEntity ac3 = this.session.create(AnotherComplexEntity.T);
		final AnotherComplexEntity ac4 = this.session.create(AnotherComplexEntity.T);

		c1.setSimpleEntityProperty(s1);
		c1.setAnotherComplexEntityProperty(ac1);
		ac1.setSimpleEntityProperty(s2);
		ac3.setSimpleEntityProperty(s2);

		ac2.setAnotherComplexEntityProperty(ac2);
		ac2.setSimpleEntityProperty(s3);

		c2.setComplexEntityProperty(c3);
		c2.setSimpleEntityProperty(s4);

		c5.setSimpleEntityProperty(s6);
		c5.setAnotherComplexEntityProperty(ac4);
		ac4.setAnotherComplexEntityProperty(ac4);
		ac4.setSimpleEntityProperty(s5);

		final Set<GenericEntity> rootEntities = getSet(c1, c2, c3, c4, c5);
		final Set<GenericEntity> subsetEntities = getSet(c1, c2, c3, c4, c5, s1, s2, s3, s4, s5, s6, ac1, ac2, ac3,
				ac4);

		final Set<GenericEntity> expectedExistingEntities = getSet(c1, s1, ac1, s2, c4, c2, s4, c3, c5, ac4, s6, s5);
		final Set<GenericEntity> expectedRemovedEntities = getSet(ac3, ac2, s3);

		runGcAndCheckResults(this.session, subsetEntities, rootEntities, expectedExistingEntities,
				expectedRemovedEntities);
	}
}
