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
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.query.PropertyQuery;

/**
 * <p>
 * Originally designed to test the (now removed) PropertyQueryInterceptor, 
 * this method is being kept just to ensure CryptoAspect is not affecting 
 * the experted behaviour of PropertyQuery(ies).\
 */
@Ignore // To be re-enabled if CryptoAspect ever react on queries.
public class CryptoPropertyQueryInterceptorTest extends CryptoInterceptorTestBase {

	@Test
	public void testSelectEncryptedPropertyFromEncryptedEntity() throws Exception {
		testPropertyQuery(Encrypted.T, "encryptedProperty", false);
	}

	@Test
	public void testSelectStandardPropertyFromEncryptedEntity() throws Exception {
		testPropertyQuery(Encrypted.T, "standardProperty", true);
	}

	@Test
	public void testSelectEncryptedPropertyFromEncryptedMultiEntity() throws Exception {
		testPropertyQuery(EncryptedMulti.T, "encryptedProperty", false);
	}

	@Test
	public void testSelectEncryptedProperty1FromEncryptedMultiEntity() throws Exception {
		testPropertyQuery(EncryptedMulti.T, "encryptedProperty1", false);
	}

	@Test
	public void testSelectEncryptedProperty2FromEncryptedMultiEntity() throws Exception {
		testPropertyQuery(EncryptedMulti.T, "encryptedProperty2", false);
	}

	@Test
	public void testSelectEncryptedProperty3FromEncryptedMultiEntity() throws Exception {
		testPropertyQuery(EncryptedMulti.T, "encryptedProperty3", false);
	}

	@Test
	public void testSelectEncryptedProperty4FromEncryptedMultiEntity() throws Exception {
		testPropertyQuery(EncryptedMulti.T, "encryptedProperty4", false);
	}

	@Test
	public void testSelectStandardPropertyFromEncryptedMultiEntity() throws Exception {
		testPropertyQuery(EncryptedMulti.T, "standardProperty", true);
	}

	@Test
	public void testSelectHashedPropertyFromHashedEntity() throws Exception {
		testPropertyQuery(Hashed.T, "hashedProperty", false);
	}

	@Test
	public void testSelectStandardPropertyFromHashedEntity() throws Exception {
		testPropertyQuery(Hashed.T, "standardProperty", true);
	}

	@Test
	public void testSelectHashedPropertyFromHashedMultiEntity() throws Exception {
		testPropertyQuery(HashedMulti.T, "hashedProperty", false);
	}

	@Test
	public void testSelectHashedProperty1FromHashedMultiEntity() throws Exception {
		testPropertyQuery(HashedMulti.T, "hashedProperty1", false);
	}

	@Test
	public void testSelectHashedProperty2FromHashedMultiEntity() throws Exception {
		testPropertyQuery(HashedMulti.T, "hashedProperty2", false);
	}

	@Test
	public void testSelectHashedProperty3FromHashedMultiEntity() throws Exception {
		testPropertyQuery(HashedMulti.T, "hashedProperty3", false);
	}

	@Test
	public void testSelectHashedProperty4FromHashedMultiEntity() throws Exception {
		testPropertyQuery(HashedMulti.T, "hashedProperty4", false);
	}

	@Test
	public void testSelectStandardPropertyFromHashedMultiEntity() throws Exception {
		testPropertyQuery(HashedMulti.T, "standardProperty", false);
	}

	@Test
	public void testSelectEncryptedPropertyFromMixedEntity() throws Exception {
		testPropertyQuery(Mixed.T, "encryptedProperty", false);
	}

	@Test
	public void testSelectHashedPropertyFromMixedEntity() throws Exception {
		testPropertyQuery(Mixed.T, "hashedProperty", false);
	}

	@Test
	public void testSelectStandardPropertyFromMixedEntity() throws Exception {
		testPropertyQuery(Mixed.T, "standardProperty", false);
	}

	@Test
	public void testSelectEncryptedPropertyFromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "encryptedProperty", false);
	}

	@Test
	public void testSelectEncryptedProperty1FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "encryptedProperty1", false);
	}

	@Test
	public void testSelectEncryptedProperty2FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "encryptedProperty2", false);
	}

	@Test
	public void testSelectEncryptedProperty3FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "encryptedProperty3", false);
	}

	@Test
	public void testSelectEncryptedProperty4FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "encryptedProperty4", false);
	}

	@Test
	public void testSelectHashedPropertyFromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "hashedProperty", false);
	}

	@Test
	public void testSelectHashedProperty1FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "hashedProperty1", false);
	}

	@Test
	public void testSelectHashedProperty2FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "hashedProperty2", false);
	}

	@Test
	public void testSelectHashedProperty3FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "hashedProperty3", false);
	}

	@Test
	public void testSelectHashedProperty4FromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "hashedProperty4", false);
	}

	@Test
	public void testSelectStandardPropertyFromMixedMultiEntity() throws Exception {
		testPropertyQuery(MixedMulti.T, "standardProperty", true);
	}

	protected void testPropertyQuery(EntityType<? extends StandardIdentifiable> entity, String propertyName, boolean assertEncryptedValue) throws Exception {

		create(entity, TestDataProvider.inputAString);

		assertProperties(entity, TestDataProvider.inputAString);

		PropertyQuery propertyQuery = PropertyQueryBuilder.forProperty(entity, 1L, propertyName).done();

		String result = (String) aopSession.query().property(propertyQuery).result().getPropertyValue();

		Assertions.assertThat(result).isNotNull().as("Unexpected result");

		String expected = null;
		if (assertEncryptedValue) {
			expected = TestDataProvider.inputAString;
		} else {
			expected = TestDataProvider.getExpected(entity, propertyName, TestDataProvider.inputAString);
		}

		Assert.assertEquals(expected, result);

	}
}
