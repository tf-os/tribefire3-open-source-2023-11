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
package com.braintribe.model.processing.query.test.selection;

import java.util.List;

import com.braintribe.model.processing.query.api.stringifier.QuerySelection;
import com.braintribe.model.processing.query.selection.BasicQuerySelectionResolver;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.stringifier.AbstractSelectQueryTests;
import com.braintribe.model.query.SelectQuery;

public abstract class AbstractSelectionResolverTest extends AbstractSelectQueryTests {
	
	protected List<QuerySelection> stringifyAndResolve(final SelectQuery selectQuery, BasicQuerySelectionResolver resolver) {
		final String queryString = stringify(selectQuery);
		System.out.println(queryString);
		List<QuerySelection> selections = resolver.resolve(selectQuery);
		for (QuerySelection selection : selections) {
			System.out.println(selection.getAlias());
		}
		return selections;
	}
	
	protected SelectQuery singleReferenceQuery() {
		// @formatter:off
		final SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.join("_Person", "company", "_Company")
				.select("_Person", "name")
				.select("_Person", "company")
				.select("_Company")
				.select("_Company","name")
				.done();
		// @formatter:on
		return selectQuery;
	}

	protected SelectQuery collectionReferenceQuery() {
		// @formatter:off
		final SelectQuery selectQuery = query()
				.from(Company.class, "_Company")
				.join("_Company", "persons", "_Person")
				.join("_Person", "localizedString", "_LS")
				.select("_Person", "name")
				.select("_Person", "company")
				.select("_Company")
				.select("_Company","name")
				.select("_LS","id")
				.select("_LS")
				.done();
		// @formatter:on
		return selectQuery;
	}

	protected SelectQuery functionQuery() {
		// @formatter:off
		final SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select("_Person")
				.select().count("_Person")
				.select().asString().entity("_Person")
				.select().avg("_Person", "age")
				.select()
					.concatenate()
						.entity("_Person")
						.value(".")
						.property("name")
						.value(":")
						.entity("_Person")
						.value(".")
						.property("companyName")
					.close()
				.done();
		// @formatter:on
		return selectQuery;
	}

	protected SelectQuery wildcardsQuery() {
		// @formatter:off
		final SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.from(Company.class, "_Company")
				.join("_Company", "persons", "_CompanyPersons")
				.join("_CompanyPersons", "localizedString", "_LS")
				//.join("_CompanyPersons", "indexedCompany", "_IC")
				.done();
		// @formatter:on
		return selectQuery;
	}



}
