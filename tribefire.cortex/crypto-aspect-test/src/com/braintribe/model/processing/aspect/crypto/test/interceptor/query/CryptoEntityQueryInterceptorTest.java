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
package com.braintribe.model.processing.aspect.crypto.test.interceptor.query;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Encrypted;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.EncryptedMulti;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Hashed;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.HashedMulti;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Mixed;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.MixedMulti;
import com.braintribe.model.processing.aspect.crypto.test.interceptor.CryptoInterceptorTestBase;
import com.braintribe.model.processing.query.fluent.ConditionBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;

/**
 * <p>
 * Originally designed to test the (now removed) EntityQueryInterceptor, 
 * this method is being kept just to ensure CryptoAspect is not affecting 
 * the experted behaviour of EntityQuery(ies).\
 */
@Ignore // To be re-enabled if CryptoAspect ever react on queries.
public class CryptoEntityQueryInterceptorTest extends CryptoInterceptorTestBase {

	// ######################################################
	// ## .. Select entity - No restriction / No results .. #
	// ######################################################

	@Test
	public void testSelectFromEncryptedEntityWithNoRestrictionNoResults() throws Exception {
		testSelectEntityWithNoRestriction(Encrypted.T, false);
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNoRestrictionNoResults() throws Exception {
		testSelectEntityWithNoRestriction(EncryptedMulti.T, false);
	}

	@Test
	public void testSelectFromHashedEntityWithNoRestrictionNoResults() throws Exception {
		testSelectEntityWithNoRestriction(Hashed.T, false);
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNoRestrictionNoResults() throws Exception {
		testSelectEntityWithNoRestriction(HashedMulti.T, false);
	}

	@Test
	public void testSelectFromMixedEntityWithNoRestrictionNoResults() throws Exception {
		testSelectEntityWithNoRestriction(Mixed.T, false);
	}

	@Test
	public void testSelectFromMixedMultiEntityWithNoRestrictionNoResults() throws Exception {
		testSelectEntityWithNoRestriction(MixedMulti.T, false);
	}

	// #########################################
	// ## .. Select entity - No restriction .. #
	// #########################################

	@Test
	public void testSelectFromEncryptedEntityWithNoRestriction() throws Exception {
		testSelectEntityWithNoRestriction(Encrypted.T, true);
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectEntityWithNoRestriction(EncryptedMulti.T, true);
	}

	@Test
	public void testSelectFromHashedEntityWithNoRestriction() throws Exception {
		testSelectEntityWithNoRestriction(Hashed.T, true);
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectEntityWithNoRestriction(HashedMulti.T, true);
	}

	@Test
	public void testSelectFromMixedEntityWithNoRestriction() throws Exception {
		testSelectEntityWithNoRestriction(Mixed.T, true);
	}

	@Test
	public void testSelectFromMixedMultiEntityWithNoRestriction() throws Exception {
		testSelectEntityWithNoRestriction(MixedMulti.T, true);
	}

	// ##################################################################
	// ## .. Select entity - value comparison restriction - matching .. #
	// ##################################################################

	@Test
	public void testSelectFromEncryptedEntityWithValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Encrypted.T, false, "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, false, "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty1() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, false, "encryptedProperty1");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty2() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, false, "encryptedProperty2");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty3() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, false, "encryptedProperty3");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty4() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, false, "encryptedProperty4");
	}

	@Test
	public void testSelectFromHashedEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Hashed.T, false, "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithValueComparisonOnHashedProperty1() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty1");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithValueComparisonOnHashedProperty2() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty2");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithValueComparisonOnHashedProperty3() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty3");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithValueComparisonOnHashedProperty4() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty4");
	}

	@Test
	public void testSelectFromMixedEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Mixed.T, false, "hashedProperty");
	}

	@Test
	public void testSelectFromMixedEntityWithValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Mixed.T, false, "encryptedProperty");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty1");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, false, "encryptedProperty1");
	}

	// ######################################################################
	// ## .. Select entity - value comparison restriction - not matching .. #
	// ######################################################################

	@Test
	public void testSelectFromEncryptedEntityWithNotMatchingValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Encrypted.T, true, false, "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty1() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty1");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty2() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty2");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty3() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty3");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty4() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty4");
	}

	@Test
	public void testSelectFromHashedEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Hashed.T, true, false, "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty1() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty1");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty2() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty2");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty3() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty3");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty4() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty4");
	}

	@Test
	public void testSelectFromMixedEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Mixed.T, true, false, "hashedProperty");
	}

	@Test
	public void testSelectFromMixedEntityWithNotMatchingValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(Mixed.T, true, false, "encryptedProperty");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, true, false, "hashedProperty1");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, true, false, "encryptedProperty1");
	}

	// #############################################################
	// ## .. Select entity - conjunction restriction - matching .. #
	// #############################################################

	@Test
	public void testSelectFromEncryptedEntityWithMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(Encrypted.T, false, true, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, false, true, "standardProperty", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	@Test
	public void testSelectFromHashedEntityWithMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(Hashed.T, false, true, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, false, true, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4");
	}

	@Test
	public void testSelectFromMixedEntityWithMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(Mixed.T, false, true, "standardProperty", "hashedProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, false, true, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	// #################################################################
	// ## .. Select entity - conjunction restriction - not matching .. #
	// #################################################################

	@Test
	public void testSelectFromEncryptedEntityWithNotMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(Encrypted.T, true, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	@Test
	public void testSelectFromHashedEntityWithNotMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(Hashed.T, true, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(HashedMulti.T, true, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4");
	}

	@Test
	public void testSelectFromMixedEntityWithNotMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(Mixed.T, true, false, "standardProperty", "hashedProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithNotMatchingConjunction() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, true, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	// #############################################################
	// ## .. Select entity - disjunction restriction - matching .. #
	// #############################################################

	@Test
	public void testSelectFromEncryptedEntityWithMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(Encrypted.T, true, true, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(EncryptedMulti.T, true, true, "standardProperty", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	@Test
	public void testSelectFromHashedEntityWithMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(Hashed.T, true, true, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(HashedMulti.T, true, true, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4");
	}

	@Test
	public void testSelectFromMixedEntityWithMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(Mixed.T, true, true, "standardProperty", "hashedProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(MixedMulti.T, true, true, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	// #################################################################
	// ## .. Select entity - disjunction restriction - not matching .. #
	// #################################################################

	@Test
	public void testSelectFromEncryptedEntityWithNotMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(Encrypted.T, true, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromEncryptedMultiEntityWithNotMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	@Test
	public void testSelectFromHashedEntityWithNotMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(Hashed.T, true, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectFromHashedMultiEntityWithNotMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(HashedMulti.T, true, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4");
	}

	@Test
	public void testSelectFromMixedEntityWithNotMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(Mixed.T, true, false, "standardProperty", "hashedProperty", "encryptedProperty");
	}

	@Test
	public void testSelectFromMixedMultiEntityWithNotMatchingDisjunction() throws Exception {
		testSelectEntityWithDisjunctionRestriction(MixedMulti.T, true, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	// ##################
	// ## .. Commons .. #
	// ##################

	protected void testSelectEntityWithNoRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertResults) throws Exception {

		if (assertResults) {
			create(type, TestDataProvider.inputAString);
			assertProperties(type, TestDataProvider.inputAString);
		}

		SelectQuery query = new SelectQueryBuilder().from(type, "e").done();

		List<?> result = query(query);

		if (assertResults) {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);
		} else {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(0);
		}

	}

	protected void testSelectEntityWithConjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertResults, String... propertyNames) throws Exception {
		testSelectEntityWithConjunctionRestriction(type, assertResults, true, propertyNames);
	}

	protected void testSelectEntityWithConjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertResults, boolean matching, String... propertyNames) throws Exception {

		create(type, TestDataProvider.inputAString);

		assertProperties(type, TestDataProvider.inputAString);

		EntityQueryBuilder builder = EntityQueryBuilder.from(type);

		ConditionBuilder<?> cb = builder.where();
		for (int i = 0; i < propertyNames.length; i++) {
			if (!matching && i == propertyNames.length - 1) {
				cb.property(propertyNames[i]).eq("UNMATCHING-VALUE");
			} else {
				cb.property(propertyNames[i]).eq(TestDataProvider.inputAString);
			}
		}

		EntityQuery query = builder.done();

		List<?> result = queryEntities(query);

		if (matching && assertResults) {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);
		} else {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(0);
		}

	}

	protected void testSelectEntityWithDisjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertResults, String... propertyNames) throws Exception {
		testSelectEntityWithDisjunctionRestriction(type, assertResults, true, propertyNames);
	}

	protected void testSelectEntityWithDisjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertResults, boolean matching, String... propertyNames) throws Exception {

		create(type, TestDataProvider.inputAString);

		assertProperties(type, TestDataProvider.inputAString);

		EntityQueryBuilder builder = EntityQueryBuilder.from(type);

		JunctionBuilder<EntityQueryBuilder> cb = builder.where().disjunction();
		for (int i = 0; i < propertyNames.length; i++) {
			if (!matching && i == propertyNames.length - 1) {
				cb.property(propertyNames[i]).eq("UNMATCHING-VALUE");
			} else {
				cb.property(propertyNames[i]).eq(TestDataProvider.inputAString);
			}
		}

		EntityQuery query = builder.done();

		List<?> result = queryEntities(query);

		if (assertResults) {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);
		} else {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(0);
		}

	}

}
