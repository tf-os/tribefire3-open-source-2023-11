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
package com.braintribe.utils.genericmodel;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.common.lcd.transformer.Transformer;
import com.braintribe.common.lcd.transformer.TransformerException;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CompoundTraversingVisitor;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;

/**
 * This class contains {@link GMCoreTools} tests).
 * 
 * @author michael.lafite
 */
public class GMCoreToolsTest {

	/**
	 * Tests {@link GMCoreTools#getDescription(com.braintribe.model.generic.GenericEntity)}.
	 */
	@Test
	public void testGetDescription() {

		final long complexEntityId = 99;
		final long simpleEntityId = 45;

		final ComplexEntity complexEntity = ComplexEntity.T.create();
		complexEntity.setBooleanProperty(true);
		complexEntity.setId(complexEntityId);
		complexEntity.setIntegerProperty(123);
		complexEntity.setSimpleEnum(SimpleEnum.THREE);
		complexEntity.setStringProperty("testString");
		complexEntity.setComplexEntityProperty(complexEntity);

		final SimpleEntity simpleEntity = SimpleEntity.T.create();
		simpleEntity.setBooleanProperty(false);
		simpleEntity.setId(simpleEntityId);
		simpleEntity.setStringProperty("simpleEntityTestString");

		complexEntity.setSimpleEntityProperty(simpleEntity);

		final List<ComplexEntity> complexEntityList = new ArrayList<ComplexEntity>();
		complexEntityList.add(ComplexEntity.T.create());
		complexEntityList.add(ComplexEntity.T.create());
		complexEntityList.add(ComplexEntity.T.create());
		complexEntity.setComplexEntityList(complexEntityList);

		final String description = GMCoreTools.getDescription(complexEntity);
		assertThat(description).contains(complexEntity.getStringProperty());
		assertThat(description).contains(simpleEntity.getStringProperty());
	}

	@Test
	public void testFindReachableEntities() {

		final Set<GenericEntity> expectedReachableEntities = new HashSet<GenericEntity>();
		assertThat(GMCoreTools.findReachableEntities("justAString")).isEqualTo(expectedReachableEntities);

		final ComplexEntity complexEntity = ComplexEntity.T.create();
		expectedReachableEntities.add(complexEntity);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		// self-reference
		complexEntity.setComplexEntityProperty(complexEntity);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		// simpleEntity not added yet, i.e. not reachable
		final SimpleEntity simpleEntity = SimpleEntity.T.create();
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		complexEntity.setSimpleEntityProperty(simpleEntity);
		expectedReachableEntities.add(simpleEntity);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		final AnotherComplexEntity anotherComplexEntity = AnotherComplexEntity.T.create();
		final SimpleEntity simpleEntity2 = SimpleEntity.T.create();
		anotherComplexEntity.setSimpleEntityProperty(simpleEntity2);
		complexEntity.setAnotherComplexEntityProperty(anotherComplexEntity);
		expectedReachableEntities.add(anotherComplexEntity);
		expectedReachableEntities.add(simpleEntity2);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		complexEntity.setBooleanProperty(true);
		complexEntity.setStringProperty("simpleEntityTestString");
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		complexEntity.setComplexEntityList(new ArrayList<ComplexEntity>());
		complexEntity.getComplexEntityList().add(complexEntity);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		final ComplexEntity complexEntity2 = ComplexEntity.T.create();
		complexEntity.getComplexEntityList().add(complexEntity2);
		expectedReachableEntities.add(complexEntity2);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		final ComplexEntity complexEntity3 = ComplexEntity.T.create();
		complexEntity.setComplexEntityMap(new HashMap<String, ComplexEntity>());
		complexEntity.getComplexEntityMap().put("testMapKey", complexEntity3);
		expectedReachableEntities.add(complexEntity3);
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		final List<GenericEntity> list = new ArrayList<GenericEntity>();
		list.add(complexEntity);
		assertThat(GMCoreTools.findReachableEntities(list)).isEqualTo(expectedReachableEntities);

	}

	@Test
	public void testFindReachableEntitiesWithEntitiesWhereToStop() {

		final Set<GenericEntity> expectedReachableEntities = new HashSet<GenericEntity>();

		final ComplexEntity complexEntity = ComplexEntity.T.create();
		expectedReachableEntities.add(complexEntity);

		final AnotherComplexEntity anotherComplexEntity = AnotherComplexEntity.T.create();
		complexEntity.setAnotherComplexEntityProperty(anotherComplexEntity);
		expectedReachableEntities.add(anotherComplexEntity);

		final Set<GenericEntity> entitiesWhereToStopFurtherTraversing = new HashSet<>();

		// make sure test works with no entities where to stop
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);
		assertThat(GMCoreTools.findReachableEntities(complexEntity, null)).isEqualTo(
				expectedReachableEntities);
		assertThat(GMCoreTools.findReachableEntities(complexEntity, entitiesWhereToStopFurtherTraversing))
				.isEqualTo(expectedReachableEntities);

		// anotherComplexEntity has no further related entities; the entity itself should still be reachable --> no
		// change
		entitiesWhereToStopFurtherTraversing.add(anotherComplexEntity);
		assertThat(GMCoreTools.findReachableEntities(complexEntity, entitiesWhereToStopFurtherTraversing))
				.isEqualTo(expectedReachableEntities);

		// complexEntity is the only entity that holds a reference to anotherComplexEntity, thus it should no longer be
		// reachable
		entitiesWhereToStopFurtherTraversing.clear();
		entitiesWhereToStopFurtherTraversing.add(complexEntity);
		expectedReachableEntities.remove(anotherComplexEntity);
		assertThat(GMCoreTools.findReachableEntities(complexEntity, entitiesWhereToStopFurtherTraversing))
				.isEqualTo(expectedReachableEntities);

	}

	@Test
	public void testFindReachableEntitiesWithAbsenceInfo() throws GmSessionException {

		final IncrementalAccess access = TestModelTestTools.newSmoodAccessMemoryOnly("test", TestModelTestTools.createTestModelMetaModel());
		final PersistenceGmSession session = TestModelTestTools.newSession(access);
		final Set<GenericEntity> expectedReachableEntities = new HashSet<GenericEntity>();

		final ComplexEntity complexEntity = session.create(ComplexEntity.T);
		expectedReachableEntities.add(complexEntity);

		final SimpleEntity simpleEntity = session.create(SimpleEntity.T);
		complexEntity.setSimpleEntityProperty(simpleEntity);
		expectedReachableEntities.add(simpleEntity);

		complexEntity.setComplexEntityList(new ArrayList<ComplexEntity>());
		final ComplexEntity complexEntity2 = session.create(ComplexEntity.T);
		complexEntity.getComplexEntityList().add(complexEntity2);
		expectedReachableEntities.add(complexEntity2);

		session.commit();

		// first make sure test works without absence info
		assertThat(GMCoreTools.findReachableEntities(complexEntity)).isEqualTo(expectedReachableEntities);

		// create second session and set new expectedReachableEntities
		final PersistenceGmSession session2 = TestModelTestTools.newSession(access);
		final Set<GenericEntity> expectedReachableEntities2 = new HashSet<>();
		// makes sure there will be absence info
		final TraversingCriterion simpleTypeTC = TC.create().negation().typeCondition(TypeConditions.isKind(TypeKind.simpleType)).done();
		List<GenericEntity> list = session2.query()
				.entities(EntityQueryBuilder.from(GenericEntity.class).tc(simpleTypeTC).done()).list();
		expectedReachableEntities2.addAll(list);
		assertThat(expectedReachableEntities2).hasSize(expectedReachableEntities.size());

		// load entity
		final ComplexEntity complexEntityWithAbsenceInfo = session2.query().entity(complexEntity)
				.withTraversingCriterion(simpleTypeTC).require();

		assertThat(GMCoreTools.findReachableEntities(complexEntityWithAbsenceInfo)).isEqualTo(
				expectedReachableEntities2);
	}

	@Test
	public void testRemoveEntityReferences() {

		final ComplexEntity complexEntity = ComplexEntity.T.create();
		complexEntity.setComplexEntityProperty(complexEntity);

		final SimpleEntity simpleEntity = SimpleEntity.T.create();
		complexEntity.setSimpleEntityProperty(simpleEntity);

		final AnotherComplexEntity anotherComplexEntity = AnotherComplexEntity.T.create();
		final SimpleEntity simpleEntity2 = SimpleEntity.T.create();
		anotherComplexEntity.setSimpleEntityProperty(simpleEntity2);
		complexEntity.setAnotherComplexEntityProperty(anotherComplexEntity);

		complexEntity.setBooleanProperty(true);
		complexEntity.setStringProperty("simpleEntityTestString");

		complexEntity.setComplexEntityList(new ArrayList<ComplexEntity>());
		complexEntity.getComplexEntityList().add(complexEntity);
		final ComplexEntity complexEntity2 = ComplexEntity.T.create();
		complexEntity.getComplexEntityList().add(complexEntity2);

		final ComplexEntity complexEntity3 = ComplexEntity.T.create();
		complexEntity.setComplexEntityMap(new HashMap<String, ComplexEntity>());
		complexEntity.getComplexEntityMap().put("testMapKey", complexEntity3);

		complexEntity.setStringList(new ArrayList<String>());
		complexEntity.getStringList().add("test");

		// remove entity references from entity
		GMCoreTools.removeEntityReferences(complexEntity);

		// make sure all non-entity-properties are still set
		assertThat(complexEntity.getBooleanProperty()).isNotNull();
		assertThat(complexEntity.getStringProperty()).isNotNull();
		assertThat(complexEntity.getStringList()).isNotNull();

		// make sure all properties with entity refs were removed.
		assertThat(complexEntity.getComplexEntityProperty()).isNull();
		assertThat(complexEntity.getAnotherComplexEntityProperty()).isNull();
		assertThat(complexEntity.getComplexEntityList()).isEmpty();
		assertThat(complexEntity.getComplexEntityMap()).isEmpty();

		// entity references are only removed from the single entity passed to the method
		assertThat(anotherComplexEntity.getSimpleEntityProperty()).isNotNull();
	}

	@Test
	public void testVisitedEntities() {

		final Set<GenericEntity> expectedVisitedEntities = new HashSet<GenericEntity>();

		final ComplexEntity complexEntity = ComplexEntity.T.create();
		complexEntity.setComplexEntityProperty(complexEntity); // self reference
		expectedVisitedEntities.add(complexEntity);

		final AnotherComplexEntity anotherComplexEntity = AnotherComplexEntity.T.create();
		complexEntity.setAnotherComplexEntityProperty(anotherComplexEntity);
		expectedVisitedEntities.add(anotherComplexEntity);

		complexEntity.setComplexEntityList(new ArrayList<ComplexEntity>());
		complexEntity.getComplexEntityList().add(complexEntity); // self reference
		final ComplexEntity complexEntity2 = ComplexEntity.T.create();
		complexEntity.getComplexEntityList().add(complexEntity2);
		expectedVisitedEntities.add(complexEntity2);

		// no entities will be matched
		final Set<GenericEntity> entitiesToMatch = new HashSet<GenericEntity>();

		// since no entities will be matched, it doesn't matter if visitMatchInclusive is true or false
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, false);
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, true);

		// anotherComplexEntity will be matched and won't be visited (visitMatchInclusive==false)
		entitiesToMatch.add(anotherComplexEntity);
		expectedVisitedEntities.remove(anotherComplexEntity);
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, false);

		// with visitMatchInclusive==true, anotherComplexEntity should still be visited
		expectedVisitedEntities.add(anotherComplexEntity);
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, true);

		// add further entities
		final SimpleEntity simpleEntity1 = SimpleEntity.T.create();
		complexEntity.setSimpleEntityProperty(simpleEntity1);
		final SimpleEntity simpleEntity2 = SimpleEntity.T.create();
		anotherComplexEntity.setSimpleEntityProperty(simpleEntity2);

		// only simpleEntity1 is visited (because we stop at anotherComplexEntity, thus simpleEntity2 is unreachable)
		expectedVisitedEntities.add(simpleEntity1);
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, true);
		expectedVisitedEntities.remove(anotherComplexEntity);
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, false);

		entitiesToMatch.add(complexEntity);
		expectedVisitedEntities.clear();
		expectedVisitedEntities.add(complexEntity);
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, true);
		expectedVisitedEntities.clear();
		traverseEntityGraphAndCheckVisitedEntities(complexEntity, entitiesToMatch, expectedVisitedEntities, false);
	}

	private static void traverseEntityGraphAndCheckVisitedEntities(final GenericEntity root,
			final Set<GenericEntity> entitiesToMatch, final Set<GenericEntity> expectedVisitedEntities,
			final boolean visitMatchInclusive) {
		final StandardTraversingContext standardTraversingContext = new StandardTraversingContext();
		standardTraversingContext.setVisitMatchInclusive(visitMatchInclusive);
		standardTraversingContext.setMatcher(new EntityVisitingMatcher(entitiesToMatch));

		final List<GenericEntity> allCriterionVisitorVisitedEntitiesList = new ArrayList<>();
		final TraversingVisitor allCriterionVisitor = new TraversingVisitor() {

			public void visitTraversing(final TraversingContext traversingContext) {
				final Object object = traversingContext.getObjectStack().peek();

				if (object instanceof GenericEntity) {
					allCriterionVisitorVisitedEntitiesList.add((GenericEntity) object);
				}
			}
		};

		final List<GenericEntity> entityCriterionOnlyVisitorVisitedEntitiesList = new ArrayList<>();
		final TraversingVisitor entityCriterionOnlyVisitor = new TraversingVisitor() {

			public void visitTraversing(final TraversingContext traversingContext) {
				final BasicCriterion criterion = traversingContext.getTraversingStack().peek();
				if (criterion instanceof EntityCriterion) {
					final GenericEntity entity = (GenericEntity) traversingContext.getObjectStack().peek();
					entityCriterionOnlyVisitorVisitedEntitiesList.add(entity);
				}
			}
		};

		standardTraversingContext.setTraversingVisitor(new CompoundTraversingVisitor(allCriterionVisitor,
				entityCriterionOnlyVisitor));

		final BaseType baseType = GMF.getTypeReflection().getBaseType();
		baseType.traverse(standardTraversingContext, root);

		final Set<GenericEntity> entityCriterionOnlyVisitorVisitedEntities = new HashSet<>(
				entityCriterionOnlyVisitorVisitedEntitiesList);
		final Set<GenericEntity> allCriterionVisitorVisitedEntities = new HashSet<>(
				allCriterionVisitorVisitedEntitiesList);
		final Set<GenericEntity> traversingContextVisitedEntities = new HashSet<>(
				standardTraversingContext.getVisitedObjects());

		// make sure we actually visited all entities expected to be visited (and only those)
		assertThat(entityCriterionOnlyVisitorVisitedEntities).isEqualTo(expectedVisitedEntities);
		if (visitMatchInclusive) {
			// match is included --> all-visitor may only visit the expected entities
			assertThat(allCriterionVisitorVisitedEntities).isEqualTo(expectedVisitedEntities);
		} else {
			/*
			 * match is not included, BUT the all-visitor may visit entities earlier (i.e. before the match), e.g. when
			 * the criterion is PROPERTY.
			 */
			assertThat(allCriterionVisitorVisitedEntities).containsAll(expectedVisitedEntities);
		}

		// make sure the entity-criterion-visitor didn't visit an entity more than once
		assertThat(entityCriterionOnlyVisitorVisitedEntitiesList).hasSize(
				entityCriterionOnlyVisitorVisitedEntities.size());

		// the all-criterion-visitor may have visited entities more than once
		// (this is expected, because entities can also be visited via ROOT or PROPERTY criterion.)
		assertThat(allCriterionVisitorVisitedEntitiesList.size()).isGreaterThanOrEqualTo(
				allCriterionVisitorVisitedEntities.size());

		// make sure the traversing context reports those entities as visited that actually have been visited.
		assertThat(traversingContextVisitedEntities).isEqualTo(allCriterionVisitorVisitedEntities);
	}

	@Test
	public void testStopTraversingAtMatchedEntity() {

		final ComplexEntity complexEntity = ComplexEntity.T.create();

		final AnotherComplexEntity anotherComplexEntity = AnotherComplexEntity.T.create();
		complexEntity.setAnotherComplexEntityProperty(anotherComplexEntity);

		final SimpleEntity simpleEntity = SimpleEntity.T.create();
		anotherComplexEntity.setSimpleEntityProperty(simpleEntity);

		final StandardTraversingContext standardTraversingContext = new StandardTraversingContext();
		standardTraversingContext.setVisitMatchInclusive(false);
		standardTraversingContext.setMatcher(new Matcher() {
			public boolean matches(final TraversingContext traversingContext) {

				// stop at anotherComplexEntity --> never traverse simpleEntity
				final boolean result = (traversingContext.getCurrentCriterionType().equals(CriterionType.ENTITY) && traversingContext
						.getObjectStack().peek() == anotherComplexEntity);

				return result;
			}
		});

		final Set<GenericEntity> visitedEntities = new HashSet<>();
		standardTraversingContext.setTraversingVisitor(new TraversingVisitor() {
			public void visitTraversing(final TraversingContext traversingContext) {
				final Object value = traversingContext.getObjectStack().peek();
				if (value instanceof GenericEntity) {
					visitedEntities.add((GenericEntity) value);
				}
			}
		});

		final Set<GenericEntity> expectedVisitedEntities = new HashSet<>();
		expectedVisitedEntities.add(complexEntity);
		expectedVisitedEntities.add(anotherComplexEntity);

		GMF.getTypeReflection().getBaseType().traverse(standardTraversingContext, complexEntity);

		assertThat(visitedEntities).isEqualTo(expectedVisitedEntities);
	}

	@Test
	public void testVisitedObjectsWhenStoppingAtRoot() {

		final SimpleEntity simpleEntity = SimpleEntity.T.create();

		final StandardTraversingContext standardTraversingContext = new StandardTraversingContext();
		// set matcher that matches everything, i.e. stops immediately
		standardTraversingContext.setMatcher(new Matcher() {
			public boolean matches(final TraversingContext traversingContext) {
				return true;
			}
		});

		final Set<GenericEntity> visitedEntities = new HashSet<>();
		standardTraversingContext.setTraversingVisitor(new TraversingVisitor() {
			public void visitTraversing(final TraversingContext traversingContext) {
				final Object value = traversingContext.getObjectStack().peek();
				if (value instanceof GenericEntity) {
					visitedEntities.add((GenericEntity) value);
				}
			}
		});

		// match not inclusive --> no visited entity
		standardTraversingContext.setVisitMatchInclusive(false);
		final Set<GenericEntity> expectedVisitedEntities = new HashSet<>();
		GMF.getTypeReflection().getBaseType().traverse(standardTraversingContext, simpleEntity);
		assertThat(visitedEntities).isEqualTo(expectedVisitedEntities);
		assertThat(new HashSet<GenericEntity>(standardTraversingContext.getVisitedObjects())).isEqualTo(
				expectedVisitedEntities);

		// with inclusive match the entity should be visited
		standardTraversingContext.setVisitMatchInclusive(true);
		expectedVisitedEntities.add(simpleEntity);
		GMF.getTypeReflection().getBaseType().traverse(standardTraversingContext, simpleEntity);
		assertThat(visitedEntities).isEqualTo(expectedVisitedEntities);
		/*
		 * MLA: the problem here is the assumption that "getVisitedObjects" returns the entities passed to the visitors,
		 * but it's actually the list of entities traversed (on ENTITY level - ROOT alone doesn't count!).
		 */
		// assertThat(new HashSet<GenericEntity>(standardTraversingContext.getVisitedObjects())).isEqualTo(
		// expectedVisitedEntities);
	}

	@Test
	public void testTraverseAndTransformEntities() {
		String alias = "simpleEntity";
		Now nowValueDescriptor = Now.T.create();
		SelectQuery query = new SelectQueryBuilder().from(SimpleEntity.class, alias).where()
				.property(alias, "stringProperty").eq().operand(nowValueDescriptor).select(alias).done();

		System.out.println("before:\n" + GMCoreTools.getDescription(query));

		Transformer<GenericEntity, ? extends Object, TraversingContext> transformer = new Transformer<GenericEntity, Object, TraversingContext>() {
			public Object transform(GenericEntity entityToCheckAndMaybeTransform, TraversingContext ignored)
					throws TransformerException {
				if (entityToCheckAndMaybeTransform instanceof ValueDescriptor) {
					return "replaced";
				} else {
					// nothing to transform
					return entityToCheckAndMaybeTransform;
				}
			}
		};

		SelectQuery processedQuery = (SelectQuery) GMCoreTools.traverseAndTransformEntities(query, transformer);

		System.out.println("after:\n" + GMCoreTools.getDescription(processedQuery));
		
		assertThat(GMCoreTools.getDescription(processedQuery)).contains("replaced");
	}
}
