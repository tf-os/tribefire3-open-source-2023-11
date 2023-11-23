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
package com.braintribe.testing.internal.suite.crud;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;
import com.braintribe.testing.internal.suite.crud.tests.CreateEntitiesTest;
import com.braintribe.testing.internal.suite.crud.tests.DeleteEntitiesTest;
import com.braintribe.testing.internal.suite.crud.tests.MandatoryPropertyTest;
import com.braintribe.testing.internal.suite.crud.tests.StandardTraversingCriteriaTest;
import com.braintribe.testing.internal.suite.crud.tests.UpdateToNullTest;
import com.braintribe.testing.internal.suite.crud.tests.Verificator;
import com.braintribe.utils.lcd.Arguments;

/**
 * Creates a new session to the given access and executes some tests to test the functionality of any Access<br>
 * Instantiate via the constructor {@link #AccessTester(String, PersistenceGmSessionFactory, GmMetaModel...)}<br>
 * If you want to ignore certain properties during the tests use {@link #setPropertyFilter(PropertyFilterPredicate)}<br>
 * Then call {@link #executeTests()}<br>
 *
 * @author Neidhart
 *
 */
public class AccessTester {
	private static Logger logger = Logger.getLogger(AccessTester.class);

	private final PersistenceGmSessionFactory sessionFactory;
	private final String testedAccessId;
	private final PersistenceGmSession testedAccessSession;
	private final Set<EntityType<?>> testedEntityTypes;
	private PropertyFilterPredicate p;
	private Set<GenericEntity> createdEntities;
	
	public boolean testDefaulTraversingCriterion = false;

	private final QueryHelper queryHelper;

	public AccessTester(String accessId, PersistenceGmSessionFactory factory, GmMetaModel... testedModels) {
		this.testedAccessSession = factory.newSession(accessId);
		this.testedAccessId = accessId;
		this.sessionFactory = factory;
		this.testedEntityTypes = new HashSet<>();
		createdEntities = new HashSet<>();

		if (testedModels.length == 0) {
			ModelOracle mo = testedAccessSession.getModelAccessory().getOracle();
			Collection<? extends CustomType> modelEntities = mo.getTypes().onlyEntities().asTypes().collect(Collectors.toSet());
			modelEntities.stream().forEach(e -> testedEntityTypes.add((EntityType<?>) e));
		} else {
			for (GmMetaModel testedModel : testedModels) {
				String typeSignatures = "";
				for (GmEntityType type : testedModel.entityTypeSet()) {
					testedEntityTypes.add(GMF.getTypeReflection().findType(type.getTypeSignature()));
					typeSignatures += "\n" + type.getTypeSignature();
				}

				logger.info("testing model " + testedModel.getName() + " with entity types " + typeSignatures);
			}
		}

		queryHelper = new QueryHelper(testedAccessSession);
	}

	/**
	 *
	 * @param propertyFilterPredicate
	 *            {@link PropertyFilterPredicate}
	 */
	public void setPropertyFilter(PropertyFilterPredicate propertyFilterPredicate) {
		Arguments.notNullWithName("propertyFilterPredicate", propertyFilterPredicate);
		
		p = propertyFilterPredicate;
	}

	public void executeTests() {
		logger.info("Start Tests...");
		logger.info("1) Create all possible entities of the demo model...");
		Collection<GenericEntity> createdEntities = testCreate();
		logger.info("Number of created entities (including entities created to populate properties): " + createdEntities.size());
		
		if (testDefaulTraversingCriterion) {
			logger.info("1.1) check default traversing criterion...");
			StandardTraversingCriteriaTest traverse = new StandardTraversingCriteriaTest(testedAccessId, sessionFactory);
			if (p != null)
				traverse.setFilterPredicate(p);
			int numTestedEntities = traverse.start().size();
			System.out.println("Success! tested " + numTestedEntities + " entities");
		}

		logger.info("2) Read: check if there was an instance of every interesting type created correctly...");
		assertEntitiesArePersisted(createdEntities);
		logger.info("Number of tested entities: " + createdEntities.size());

		logger.info("3) Update: Testing update generically for all entities: set all nullable properties to null...");
		int numUpdatedEntities = genericallyTestUpdate().size();
		logger.info("Succeeded! Num tested entities: " + numUpdatedEntities);
		logger.info("looking up all Mandatory properties and try to set them null...");
		Set<String> mpNames = testSetAllMandatoryPropertiesNull();
		String mpNamesPretty = mpNames.stream().reduce("", (old, next) -> old + "\n" + next);
		logger.info("Cool! the 'Mandatory' metadata was respected throughout. Names of found mandatory properties:\n" + mpNamesPretty);

		logger.info("4) Deleting all entities that were created in the first step piece by piece...");
		deleteEverythingPieceByPiece();

		logger.info("Test finished successfully");
	}

	/**
	 * Returns the created entities
	 */
	private Collection<GenericEntity> testCreate() {
		CreateEntitiesTest test = new CreateEntitiesTest(testedAccessId, sessionFactory);

		if (testedEntityTypes != null && !testedEntityTypes.isEmpty()) {
			test.setTestedEntityTypes(testedEntityTypes);
		}

		if (p != null) {
			test.setFilterPredicate(p);
		}

		Collection<GenericEntity> createdNow = test.start();
		createdEntities.addAll(createdNow);

		return createdNow;
	}

	/**
	 * Asserts that all expected entities are persisted in access
	 */
	private void assertEntitiesArePersisted(Collection<GenericEntity> expectedEntities) {
		Verificator verificator = new Verificator(testedAccessId, sessionFactory);
		if (p != null) {
			verificator.setFilterPredicate(p);
		}
		verificator.assertEntitiesArePersisted(expectedEntities);
	}

	/**
	 * Goes through all entities and checks if there is a property with 'Mandatory' metadata attached. Tries to set
	 * every found to null and checks if an exception is thrown
	 *
	 * @return set of all discovered property names with 'Mandatory' metadata attached
	 */
	private Set<String> testSetAllMandatoryPropertiesNull() {
		MandatoryPropertyTest test = new MandatoryPropertyTest(testedAccessId, sessionFactory);
		test.start();
		return test.getMandatoryPropertyNames();
	}

	/**
	 * generically updating all nullable properties of every entity created so far
	 *
	 * @return list off all updated entities
	 */
	private Set<GenericEntity> genericallyTestUpdate() {
		UpdateToNullTest test = new UpdateToNullTest(testedAccessId, sessionFactory);
		test.setEntitiesToTest(createdEntities);
		createdEntities = new HashSet<>(test.start());
		return createdEntities;
	}

	/**
	 * deletes every entity that was created by this AccessTester until now
	 */
	private void deleteEverythingPieceByPiece() {
		DeleteEntitiesTest test = new DeleteEntitiesTest(testedAccessId, sessionFactory);
		test.setEntitiesToDelete(createdEntities);
		assertEntitiesArePersisted(createdEntities);
		test.start();
		createdEntities.clear();
	}

}
