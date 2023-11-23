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
package com.braintribe.model.processing.query.test.stringifier;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 *
 */
public class IndexJoinQueryTests extends AbstractSelectQueryTests {

	// ####################################
	// ## . . . . . Value Join . . . . . ##
	// ####################################

	@Test
	public void simpleValueJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.from(Company.class, "_Company")
				.where()
				.property("_Person", "companyName").eq().property("_Company", "indexedName")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person, com.braintribe.model.processing.query.test.model.Company _Company where _Person.companyName = _Company.indexedName"));
	}

	// ####################################
	// ## . . . . . Range Join . . . . . ##
	// ####################################

	@Test
	public void simpleRangeJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.from(Company.class, "_Company")
				.where()
				.property("_Person", "birthDate").ge().property("_Company", "indexedDate")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person, com.braintribe.model.processing.query.test.model.Company _Company where _Person.birthDate >= _Company.indexedDate"));
	}

	// ####################################
	// ## . . Generated Value Join . . . ##
	// ####################################

	@Test
	public void mergeLookupJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.from(Company.class, "_Company")
				.where()
				.property("_Person", "companyName").eq().property("_Company", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person, com.braintribe.model.processing.query.test.model.Company _Company where _Person.companyName = _Company.name"));
	}

	@Test
	public void mergeLookupJoinWithJoinOperand() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Company", "name")
				.from(Address.class, "_Address")
				.from(Company.class, "_Company")
				.join("_Company", "address", "_Address2")
				.where()
				.entity("_Address").eq().entity("_Address2")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _Company.name from com.braintribe.model.processing.query.test.model.Address _Address, com.braintribe.model.processing.query.test.model.Company _Company join _Company.address _Address2 where _Address = _Address2"));
	}

	@Test
	public void mergeRangeJoin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.from(Company.class, "_Company")
				.where()
				.property("_Person", "companyName").ge().property("_Company", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person, com.braintribe.model.processing.query.test.model.Company _Company where _Person.companyName >= _Company.name"));
	}

	@Test
	public void mergeRangeJoinWithJoinOperand() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.select("_Address", "name")
				.select("_Company", "name")
				.from(Address.class, "_Address")
				.from(Company.class, "_Company")
				.join("_Company", "address", "_Address2")
				.where()
				.property("_Address", "name").ge().property("_Address2", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select _Address.name, _Company.name from com.braintribe.model.processing.query.test.model.Address _Address, com.braintribe.model.processing.query.test.model.Company _Company join _Company.address _Address2 where _Address.name >= _Address2.name"));
	}
}
