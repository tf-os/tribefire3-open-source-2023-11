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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.api.shortening.SignatureExpert;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.MetaModelProvider;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Query;

public class AbstractSelectQueryTests {

	protected SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

	protected String stringify(Query query) {
		return BasicQueryStringifier.create().stringify(query);
	}

	protected String stringify(Query query, SignatureExpert mode) {
		return BasicQueryStringifier.create().shorteningMode().custom(mode).stringify(query);
	}

	protected Person getNicknamePerson() {
		Person p = newPerson(1l, null);
		p.setNicknames(asSet("n1", "n2", "n3", "n4"));

		return p;
	}

	protected Person getPerson() {
		return newPerson(1L, "p");
	}

	protected Company getCompany() {
		return newCompany(1l, "c");
	}

	protected Company getTwoPersonCompany() {
		Person p1 = newPerson(1l, "Jack");
		Person p2 = newPerson(2l, "John");

		Company c = newCompany(1l, null);
		c.setPersons(asList(p1, p2));

		return c;
	}

	protected Owner getOwner() {
		Owner o = Owner.T.create();
		o.setId(1L);

		return o;
	}

	protected Company getFourPersonCompany() {
		Person p1 = newPerson(1L, "p1");
		Person p2 = newPerson(2L, "p2");
		Person p3 = newPerson(3L, "p3");
		Person p4 = newPerson(4L, "p4");

		return newCompany(1l, null, p1, p2, p3, p4);
	}

	private Person newPerson(long id, String name) {
		Person p = Person.T.create();
		p.setName(name);
		p.setId(id);
		return p;
	}

	private Company newCompany(long id, String name, Person... persons) {
		Company c = Company.T.create();
		c.setName(name);
		c.setId(id);
		c.setPersons(asList(persons));
		return c;
	}

	public static ModelOracle getModelOracle() {
		return new BasicModelOracle(MetaModelProvider.provideRawModel());
	}

}
