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

import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Encrypted;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.EncryptedMulti;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Hashed;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.HashedMulti;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.HashedNonDeterministic;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Standard;
import com.braintribe.model.processing.aspect.crypto.test.interceptor.CryptoInterceptorTestBase;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;

/**
 * <p>
 * Originally designed to test the (now removed) EntityQueryInterceptor, 
 * this method is being kept just to ensure CryptoAspect is not affecting 
 * the experted behaviour of EntityQuery(ies).\
 */
@Ignore // To be re-enabled if CryptoAspect ever react on queries.
public class CryptoEntityQueryInterceptorLabsTest extends CryptoInterceptorTestBase {
	

	@Test
	public void testQueryHashedNonDeterministicEntityLab() throws Exception {

		create(HashedNonDeterministic.T, 1L, TestDataProvider.inputAString);
		create(HashedNonDeterministic.T, 2L, TestDataProvider.inputBString);

		EntityQuery query = EntityQueryBuilder.from(HashedNonDeterministic.class).done();
		
		List<Object> result = aopSession.query().entities(query).list();

		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(2);

		
		/**
		 * Restriction on non-deterministic property only, must not return results
		 */
		
		query = EntityQueryBuilder
				.from(HashedNonDeterministic.class)
					.where()
						.conjunction()
							.property("hashedProperty").eq(TestDataProvider.inputAString)
						.close()
					.done();
		
		result = aopSession.query().entities(query).list();
		
		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(0);

		/**
		 * Restriction on non-deterministic property and id property, must return single result
		 */
		
		query = EntityQueryBuilder
				.from(HashedNonDeterministic.class)
					.where()
						.conjunction()
							.property("id").eq(1L)
							.property("hashedProperty").eq(TestDataProvider.inputAString)
						.close()
					.done();
		
		result = aopSession.query().entities(query).list();
		
		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(0);
		

		/**
		 * Restriction on non-deterministic property and id property, must return single result
		 */
		
		query = EntityQueryBuilder
				.from(HashedNonDeterministic.class)
					.where()
						.conjunction()
							.property("id").eq(2L)
							.property("hashedProperty").eq(TestDataProvider.inputBString)
						.close()
					.done();
		
		result = aopSession.query().entities(query).list();

		/**
		 * Restriction on non-deterministic property and id property, but input not matching entity state. must return no results
		 */
		
		query = EntityQueryBuilder
				.from(HashedNonDeterministic.class)
					.where()
						.conjunction()
							.property("id").eq(2L)
							.property("hashedProperty").eq(TestDataProvider.inputAString) //id 2 was set to inputBString
						.close()
					.done();
		
		result = aopSession.query().entities(query).list();
		
		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(0);
		

		/**
		 * Restriction on non-deterministic property and id property, but input not matching entity state. must return no results
		 */
		
		query = EntityQueryBuilder
				.from(HashedNonDeterministic.class)
					.where()
						.conjunction()
							.property("id").eq(1L)
							.property("hashedProperty").eq(TestDataProvider.inputBString) //id 1 was set to inputAString
						.close()
					.done();
		
		result = aopSession.query().entities(query).list();
		
		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(0);
		
		
	}

	@Test
	public void testQueryEncryptedEntityLab() throws Exception {

		create(EncryptedMulti.T, TestDataProvider.inputAString);
		
		assertProperties(EncryptedMulti.T, TestDataProvider.inputAString);

		EntityQuery query = EntityQueryBuilder
				.from(EncryptedMulti.T)
					.where()
						.conjunction()
							.property("encryptedProperty1").eq(TestDataProvider.inputAString)
							.property("encryptedProperty2").eq(TestDataProvider.inputAString)
							.property("encryptedProperty3").eq(TestDataProvider.inputAString)
							.property("encryptedProperty4").eq(TestDataProvider.inputAString)
						.close()
					.done();
		
		List<Object> result = aopSession.query().entities(query).list();
		
		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(0);
		
	}
	
	@Test
	public void testQueryHashedEntityLab() throws Exception {
		
		create(HashedMulti.T, TestDataProvider.inputAString);
		
		assertProperties(HashedMulti.T, TestDataProvider.inputAString);

		EntityQuery query = EntityQueryBuilder
				.from(HashedMulti.T)
					.where()
						.conjunction()
							.property("hashedProperty1").eq(TestDataProvider.inputAString)
							.property("hashedProperty2").eq(TestDataProvider.inputAString)
							.property("hashedProperty3").eq(TestDataProvider.inputAString)
							.property("hashedProperty4").eq(TestDataProvider.inputAString)
						.close()
					.done();
		
		List<Object> result = aopSession.query().entities(query).list();
		
		Assertions.assertThat(result).isNotNull().as("Wrong number of entities returned from the query").hasSize(0);
		
	}
	
	@Test
	public void testEncryptedQueryPerformanceSingleResultAgainstAccessWithoutAspect() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Encrypted.class).where().property("encryptedProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstAccessWithoutAspect(query, 5, 1);
	}
	
	@Test
	public void testHashedQueryPerformanceSingleResultAgainstAccessWithoutAspect() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Hashed.class).where().property("hashedProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstAccessWithoutAspect(query, 5, 1);
	}
	
	@Test
	public void testEncryptedQueryPerformanceMultipleResultAgainstAccessWithoutAspect() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Encrypted.class).where().property("encryptedProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstAccessWithoutAspect(query, 5, 2);
	}
	
	@Test
	public void testHashedQueryPerformanceMultipleResultAgainstAccessWithoutAspect() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Hashed.class).where().property("hashedProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstAccessWithoutAspect(query, 5, 2);
	}
	
	@Test
	public void testEncryptedQueryPerformanceSingleResultAgainstNonCryptedType() throws Exception {
		EntityQuery queryEligible = EntityQueryBuilder.from(Encrypted.class).where().property("encryptedProperty").eq(TestDataProvider.inputAString).done();
		EntityQuery queryIneligible = EntityQueryBuilder.from(Standard.class).where().property("standardProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstNonCryptedType(queryEligible, queryIneligible, 5, 1);
	}
	
	@Test
	public void testHashedQueryPerformanceSingleResultAgainstNonCryptedType() throws Exception {
		EntityQuery queryEligible = EntityQueryBuilder.from(Hashed.class).where().property("hashedProperty").eq(TestDataProvider.inputAString).done();
		EntityQuery queryIneligible = EntityQueryBuilder.from(Standard.class).where().property("standardProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstNonCryptedType(queryEligible, queryIneligible, 5, 1);
	}
	
	@Test
	public void testEncryptedQueryPerformanceMultipleResultAgainstNonCryptedType() throws Exception {
		EntityQuery queryEligible = EntityQueryBuilder.from(Encrypted.class).where().property("encryptedProperty").eq(TestDataProvider.inputAString).done();
		EntityQuery queryIneligible = EntityQueryBuilder.from(Standard.class).where().property("standardProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstNonCryptedType(queryEligible, queryIneligible, 5, 2);
	}
	
	@Test
	public void testHashedQueryPerformanceMultipleResultAgainstNonCryptedType() throws Exception {
		EntityQuery queryEligible = EntityQueryBuilder.from(Hashed.class).where().property("hashedProperty").eq(TestDataProvider.inputAString).done();
		EntityQuery queryIneligible = EntityQueryBuilder.from(Standard.class).where().property("standardProperty").eq(TestDataProvider.inputAString).done();
		testPerformanceAgainstNonCryptedType(queryEligible, queryIneligible, 5, 2);
	}
	
	private void testPerformanceAgainstAccessWithoutAspect(EntityQuery query, int q, int e) throws Exception {

		String type = query.getEntityTypeSignature();
		
		create(type, TestDataProvider.inputAString, e);
		
		long resultDisabled = 0;
		long resultEnabled = 0;

		//fair initialization
		aopSessionWithoutAspect.query().entities(query).list();
		aopSession.query().entities(query).list();
		
		long t = System.currentTimeMillis();
		for (int i = 0; i < q; i++) {
			aopSessionWithoutAspect.query().entities(query).list();
		}
		resultDisabled = System.currentTimeMillis()-t;
		
		t = System.currentTimeMillis();
		for (int i = 0; i < q; i++) {
			aopSession.query().entities(query).list();
		}
		resultEnabled = System.currentTimeMillis()-t;

		System.out.println(q+" queries on "+type+" property resulting in "+e+" rows. without crypto aspect: "+resultDisabled+" ms");
		System.out.println(q+" queries on "+type+" property resulting in "+e+" rows. with crypto aspect: "+resultEnabled+" ms");
		
		
	}
	
	private void testPerformanceAgainstNonCryptedType(EntityQuery queryEligible, EntityQuery queryIneligible, int q, int e) throws Exception {

		String eligibleType = queryEligible.getEntityTypeSignature();
		String ineligibleType = queryIneligible.getEntityTypeSignature();

		create(eligibleType, TestDataProvider.inputAString, e);
		create(ineligibleType, TestDataProvider.inputAString, e);
		
		long resultDisabled = 0;
		long resultEnabled = 0;

		//fair initialization
		aopSession.query().entities(queryEligible).list();
		aopSession.query().entities(queryIneligible).list();
		
		long t = System.currentTimeMillis();
		for (int i = 0; i < q; i++) {
			aopSession.query().entities(queryIneligible).list();
		}
		resultDisabled = System.currentTimeMillis()-t;
		
		t = System.currentTimeMillis();
		for (int i = 0; i < q; i++) {
			aopSession.query().entities(queryEligible).list();
		}
		resultEnabled = System.currentTimeMillis()-t;

		System.out.println(q+" queries on "+ineligibleType+" property resulting in "+e+" rows. with crypto aspect: "+resultDisabled+" ms");
		System.out.println(q+" queries on "+eligibleType+" property resulting in "+e+" rows. with crypto aspect: "+resultEnabled+" ms");
		
	}
	
}
