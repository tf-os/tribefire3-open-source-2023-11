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
import org.junit.Assert;
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
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;

/**
 * <p>
 * Originally designed to test the (now removed) SelectQueryInterceptor, 
 * this method is being kept just to ensure CryptoAspect is not affecting 
 * the experted behaviour of SelectQuery(ies).\
 */
@Ignore // To be re-enabled if CryptoAspect ever react on queries.
public class CryptoSelectQueryInterceptorTest extends CryptoInterceptorTestBase {

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
	public void testSelectFromMixedMultiEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty1");
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
	public void testSelectFromMixedMultiEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectEntityWithConjunctionRestriction(MixedMulti.T, true, false, "hashedProperty1");
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

	// ###########################################
	// ## .. Select property - No restriction .. #
	// ###########################################

	@Test
	public void testSelectEncryptedPropertyFromEncryptedEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(Encrypted.T, false, "encryptedProperty");
	}

	@Test
	public void testSelectMixedPropertiesFromEncryptedEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(Encrypted.T, false, "encryptedProperty", "standardProperty");
	}

	@Test
	public void testSelectStandardPropertyFromEncryptedEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(Encrypted.T, true, "standardProperty");
	}

	@Test
	public void testSelectEncryptedProperty1FromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(EncryptedMulti.T, false, "encryptedProperty1");
	}

	@Test
	public void testSelectEncryptedProperty2FromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(EncryptedMulti.T, false, "encryptedProperty2");
	}

	@Test
	public void testSelectEncryptedProperty3FromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(EncryptedMulti.T, false, "encryptedProperty3");
	}

	@Test
	public void testSelectEncryptedProperty4FromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(EncryptedMulti.T, false, "encryptedProperty4");
	}

	@Test
	public void testSelectMixedPropertiesFromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty", "encryptedProperty1", "encryptedProperty2", "encryptedProperty3", "encryptedProperty4");
	}

	@Test
	public void testSelectStandardPropertyFromEncryptedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(EncryptedMulti.T, true, "standardProperty");
	}

	@Test
	public void testSelectHashedPropertyFromHashedEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(Hashed.T, false, "hashedProperty");
	}

	@Test
	public void testSelectMixedPropertiesFromHashedEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(Hashed.T, false, "hashedProperty", "standardProperty");
	}

	@Test
	public void testSelectStandardPropertyFromHashedEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(Hashed.T, true, "standardProperty");
	}

	@Test
	public void testSelectHashedProperty1FromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(HashedMulti.T, false, "hashedProperty1");
	}

	@Test
	public void testSelectHashedProperty2FromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(HashedMulti.T, false, "hashedProperty2");
	}

	@Test
	public void testSelectHashedProperty3FromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(HashedMulti.T, false, "hashedProperty3");
	}

	@Test
	public void testSelectHashedProperty4FromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(HashedMulti.T, false, "hashedProperty4");
	}

	@Test
	public void testSelectMixedPropertiesFromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4");
	}

	@Test
	public void testSelectStandardPropertyFromHashedMultiEntityWithNoRestriction() throws Exception {
		testSelectPropertyWithNoRestriction(HashedMulti.T, true, "standardProperty");
	}

	// ####################################################################
	// ## .. Select property - value comparison restriction - matching .. #
	// ####################################################################

	@Test
	public void testSelectPropertyFromEncryptedEntityWithValueComparisonOnEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Encrypted.T, false, "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithValueComparisonOnEncryptedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromHashedEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Hashed.T, false, "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithValueComparisonOnHashedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty1");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithValueComparisonOnHashedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty2");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithValueComparisonOnHashedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty3");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithValueComparisonOnHashedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "hashedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Mixed.T, false, "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty1");
	}

	// ########################################################################
	// ## .. Select property - value comparison restriction - not matching .. #
	// ########################################################################

	@Test
	public void testSelectPropertyFromEncryptedEntityWithNotMatchingValueComparisonOnEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Encrypted.T, true, false, "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingValueComparisonOnEncryptedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromHashedEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Hashed.T, true, false, "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty1");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty2");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty3");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingValueComparisonOnHashedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, true, false, "hashedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Mixed.T, true, false, "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingValueComparisonOnHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, true, false, "hashedProperty1");
	}

	// ##############################################################
	// ## .. Select property - conjunction restriction- matching .. #
	// ##############################################################

	@Test
	public void testSelectPropertyFromEncryptedEntityWithMatchingConjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Encrypted.T, true, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingConjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingConjunctionIncludingEncryptedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "standardProperty", "encryptedProperty", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingConjunctionIncludingEncryptedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "standardProperty", "encryptedProperty", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingConjunctionIncludingEncryptedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "standardProperty", "encryptedProperty", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingConjunctionIncludingEncryptedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, "standardProperty", "encryptedProperty", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromHashedEntityWithMatchingConjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Hashed.T, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingConjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingConjunctionIncludingHashedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty1");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingConjunctionIncludingHashedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty2");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingConjunctionIncludingHashedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty3");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingConjunctionIncludingHashedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithMatchingConjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Mixed.T, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithMatchingConjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Mixed.T, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingConjunctionIncludingProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty1", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingConjunctionIncludingProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty2", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingConjunctionIncludingProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty3", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingConjunctionIncludingProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, "hashedProperty4", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingConjunctionIncludingAllProperties() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty4");
	}

	// ##################################################################
	// ## .. Select property - conjunction restriction- not matching .. #
	// ##################################################################

	@Test
	public void testSelectPropertyFromEncryptedEntityWithNotMatchingConjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Encrypted.T, true, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingConjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingConjunctionIncludingEncryptedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingConjunctionIncludingEncryptedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingConjunctionIncludingEncryptedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingConjunctionIncludingEncryptedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(EncryptedMulti.T, true, false, "standardProperty", "encryptedProperty", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromHashedEntityWithNotMatchingConjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Hashed.T, false, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingConjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingConjunctionIncludingHashedProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty1");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingConjunctionIncludingHashedProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty2");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingConjunctionIncludingHashedProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty3");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingConjunctionIncludingHashedProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithNotMatchingConjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Mixed.T, false, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithNotMatchingConjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithConjunctionRestriction(Mixed.T, false, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingConjunctionIncludingProperty1() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, false, "hashedProperty1", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingConjunctionIncludingProperty2() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, false, "hashedProperty2", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingConjunctionIncludingProperty3() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, false, "hashedProperty3", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingConjunctionIncludingProperty4() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, false, "hashedProperty4", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingConjunctionIncludingAllProperties() throws Exception {
		testSelectPropertyWithConjunctionRestriction(MixedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty4");
	}

	// ##############################################################
	// ## .. Select property - disjunction restriction- matching .. #
	// ##############################################################

	@Test
	public void testSelectPropertyFromEncryptedEntityWithMatchingDisjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Encrypted.T, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingDisjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingDisjunctionIncludingEncryptedProperty1() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingDisjunctionIncludingEncryptedProperty2() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingDisjunctionIncludingEncryptedProperty3() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithMatchingDisjunctionIncludingEncryptedProperty4() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, "standardProperty", "encryptedProperty", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromHashedEntityWithMatchingDisjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Hashed.T, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingDisjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingDisjunctionIncludingHashedProperty1() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty1");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingDisjunctionIncludingHashedProperty2() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty2");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingDisjunctionIncludingHashedProperty3() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty3");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithMatchingDisjunctionIncludingHashedProperty4() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithMatchingDisjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Mixed.T, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithMatchingDisjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Mixed.T, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingDisjunctionIncludingProperty1() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, "hashedProperty1", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingDisjunctionIncludingProperty2() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, "hashedProperty2", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingDisjunctionIncludingProperty3() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, "hashedProperty3", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingDisjunctionIncludingProperty4() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, "hashedProperty4", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithMatchingDisjunctionIncludingAllProperties() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty4");
	}

	// ##################################################################
	// ## .. Select property - disjunction restriction- not matching .. #
	// ##################################################################

	@Test
	public void testSelectPropertyFromEncryptedEntityWithNotMatchingDisjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Encrypted.T, false, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingDisjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingDisjunctionIncludingEncryptedProperty1() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, false, "standardProperty", "encryptedProperty", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingDisjunctionIncludingEncryptedProperty2() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, false, "standardProperty", "encryptedProperty", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingDisjunctionIncludingEncryptedProperty3() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, false, "standardProperty", "encryptedProperty", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromEncryptedMultiEntityWithNotMatchingDisjunctionIncludingEncryptedProperty4() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(EncryptedMulti.T, false, false, "standardProperty", "encryptedProperty", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromHashedEntityWithNotMatchingDisjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Hashed.T, false, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingDisjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingDisjunctionIncludingHashedProperty1() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty1");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingDisjunctionIncludingHashedProperty2() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty2");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingDisjunctionIncludingHashedProperty3() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty3");
	}

	@Test
	public void testSelectPropertyFromHashedMultiEntityWithNotMatchingDisjunctionIncludingHashedProperty4() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(HashedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithNotMatchingDisjunctionIncludingHashedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Mixed.T, false, false, "standardProperty", "hashedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedEntityWithNotMatchingDisjunctionIncludingEncryptedProperty() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(Mixed.T, false, false, "standardProperty", "encryptedProperty");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingDisjunctionIncludingProperty1() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, false, "hashedProperty1", "encryptedProperty1");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingDisjunctionIncludingProperty2() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, false, "hashedProperty2", "encryptedProperty2");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingDisjunctionIncludingProperty3() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, false, "hashedProperty3", "encryptedProperty3");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingDisjunctionIncludingProperty4() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, false, "hashedProperty4", "encryptedProperty4");
	}

	@Test
	public void testSelectPropertyFromMixedMultiEntityWithNotMatchingDisjunctionIncludingAllProperties() throws Exception {
		testSelectPropertyWithDisjunctionRestriction(MixedMulti.T, false, false, "standardProperty", "hashedProperty", "hashedProperty1", "hashedProperty2", "hashedProperty3", "hashedProperty4", "encryptedProperty4");
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

		SelectQueryBuilder builder = new SelectQueryBuilder().from(type, "e");

		ConditionBuilder<?> cb = builder.where();
		for (int i = 0; i < propertyNames.length; i++) {
			if (!matching && i == propertyNames.length - 1) {
				cb.property(propertyNames[i]).eq("UNMATCHING-VALUE");
			} else {
				cb.property(propertyNames[i]).eq(TestDataProvider.inputAString);
			}
		}

		SelectQuery query = builder.done();

		List<?> result = query(query);

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

		SelectQueryBuilder builder = new SelectQueryBuilder().from(type, "e");

		JunctionBuilder<SelectQueryBuilder> cb = builder.where().disjunction();
		for (int i = 0; i < propertyNames.length; i++) {
			if (!matching && i == propertyNames.length - 1) {
				cb.property(propertyNames[i]).eq("UNMATCHING-VALUE");
			} else {
				cb.property(propertyNames[i]).eq(TestDataProvider.inputAString);
			}
		}

		SelectQuery query = builder.done();

		List<?> result = query(query);

		if (assertResults) {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);
		} else {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(0);
		}

	}

	protected void testSelectPropertyWithNoRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertDecrypted, String... propertyNames) throws Exception {

		create(type, TestDataProvider.inputAString);

		assertProperties(type, TestDataProvider.inputAString);

		SelectQueryBuilder builder = new SelectQueryBuilder();

		for (String propertyName : propertyNames) {
			builder.select("e", propertyName);
		}

		SelectQuery query = builder.from(type, "e").done();

		List<Object> result = aopSession.query().select(query).list();

		Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);

		if (propertyNames.length == 1) {

			Assertions.assertThat(result.get(0)).isInstanceOf(String.class).as("Unexpected type of result");
			String resultString = (String) result.get(0);

			String expected = null;
			if (assertDecrypted) {
				expected = TestDataProvider.inputAString;
			} else {
				expected = TestDataProvider.getExpected(type, propertyNames[0], TestDataProvider.inputAString);
			}

			Assert.assertEquals(expected, resultString);

		} else {

			Assertions.assertThat(result.get(0)).isInstanceOf(ListRecord.class).as("Unexpected type of result");
			ListRecord lr = (ListRecord) result.get(0);

			for (int i = 0; i < lr.getValues().size(); i++) {

				Object resultObj = lr.getValues().get(i);
				Assertions.assertThat(resultObj).isInstanceOf(String.class).as("Unexpected type of result");
				String resultString = (String) resultObj;

				String expected = null;
				if (assertDecrypted) {
					expected = TestDataProvider.inputAString;
				} else {
					expected = TestDataProvider.getExpected(type, propertyNames[i], TestDataProvider.inputAString);
				}

				Assert.assertEquals(expected, resultString);

			}

		}

	}

	protected void testSelectPropertyWithConjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertDecrypted, String... propertyNames) throws Exception {
		testSelectPropertyWithConjunctionRestriction(type, assertDecrypted, false, propertyNames);
	}

	protected void testSelectPropertyWithConjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertDecrypted, boolean matching, String... propertyNames) throws Exception {

		create(type, TestDataProvider.inputAString);

		assertProperties(type, TestDataProvider.inputAString);

		SelectQueryBuilder builder = new SelectQueryBuilder();

		for (String propertyName : propertyNames) {
			builder.select("e", propertyName);
		}

		builder.from(type, "e");

		ConditionBuilder<?> cb = builder.where();
		for (int i = 0; i < propertyNames.length; i++) {
			if (!matching && i == propertyNames.length - 1) {
				cb.property(propertyNames[i]).eq("UNMATCHING-VALUE");
			} else {
				cb.property(propertyNames[i]).eq(TestDataProvider.inputAString);
			}
		}

		SelectQuery query = builder.done();

		List<Object> result = aopSession.query().select(query).list();

		if (matching) {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);
		} else {
			Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(0);
		}

		if (matching && assertDecrypted) {

			if (propertyNames.length == 1) {

				Assertions.assertThat(result.get(0)).isInstanceOf(String.class).as("Unexpected type of result");
				String resultString = (String) result.get(0);

				String expected = null;
				if (assertDecrypted) {
					expected = TestDataProvider.inputAString;
				} else {
					expected = TestDataProvider.getExpected(type, propertyNames[0], TestDataProvider.inputAString);
				}

				Assert.assertEquals(expected, resultString);

			} else {

				Assertions.assertThat(result.get(0)).isInstanceOf(ListRecord.class).as("Unexpected type of result");
				ListRecord lr = (ListRecord) result.get(0);

				for (int i = 0; i < lr.getValues().size(); i++) {

					Object resultObj = lr.getValues().get(i);
					Assertions.assertThat(resultObj).isInstanceOf(String.class).as("Unexpected type of result");
					String resultString = (String) resultObj;

					String expected = null;
					if (assertDecrypted) {
						expected = TestDataProvider.inputAString;
					} else {
						expected = TestDataProvider.getExpected(type, propertyNames[i], TestDataProvider.inputAString);
					}

					Assert.assertEquals(expected, resultString);

				}

			}

		}

	}

	protected void testSelectPropertyWithDisjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertDecrypted, String... propertyNames) throws Exception {
		testSelectPropertyWithDisjunctionRestriction(type, assertDecrypted, true, propertyNames);
	}

	protected void testSelectPropertyWithDisjunctionRestriction(EntityType<? extends StandardIdentifiable> type, boolean assertDecrypted, boolean matching, String... propertyNames) throws Exception {

		create(type, TestDataProvider.inputAString);

		assertProperties(type, TestDataProvider.inputAString);

		SelectQueryBuilder builder = new SelectQueryBuilder();

		for (String propertyName : propertyNames) {
			builder.select("e", propertyName);
		}

		builder.from(type, "e");

		JunctionBuilder<?> cb = builder.where().disjunction();
		for (int i = 0; i < propertyNames.length; i++) {
			if (!matching && i == propertyNames.length - 1) {
				cb.property(propertyNames[i]).eq("UNMATCHING-VALUE");
			} else {
				cb.property(propertyNames[i]).eq(TestDataProvider.inputAString);
			}
		}

		SelectQuery query = builder.done();

		List<Object> result = aopSession.query().select(query).list();

		// if (matching) {
		Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select query").hasSize(1);
		// } else {
		// Assertions.assertThat(result).isNotNull().as("Wrong number of entries returned from the select
		// query").hasSize(0);
		// }

		if (assertDecrypted) {

			if (propertyNames.length == 1) {

				Assertions.assertThat(result.get(0)).isInstanceOf(String.class).as("Unexpected type of result");
				String resultString = (String) result.get(0);

				String expected = null;
				if (assertDecrypted) {
					expected = TestDataProvider.inputAString;
				} else {
					expected = TestDataProvider.getExpected(type, propertyNames[0], TestDataProvider.inputAString);
				}

				Assert.assertEquals(expected, resultString);

			} else {

				Assertions.assertThat(result.get(0)).isInstanceOf(ListRecord.class).as("Unexpected type of result");
				ListRecord lr = (ListRecord) result.get(0);

				for (int i = 0; i < lr.getValues().size(); i++) {

					Object resultObj = lr.getValues().get(i);
					Assertions.assertThat(resultObj).isInstanceOf(String.class).as("Unexpected type of result");
					String resultString = (String) resultObj;

					String expected = null;
					if (assertDecrypted) {
						expected = TestDataProvider.inputAString;
					} else {
						expected = TestDataProvider.getExpected(type, propertyNames[i], TestDataProvider.inputAString);
					}

					Assert.assertEquals(expected, resultString);

				}

			}

		}

	}

}
