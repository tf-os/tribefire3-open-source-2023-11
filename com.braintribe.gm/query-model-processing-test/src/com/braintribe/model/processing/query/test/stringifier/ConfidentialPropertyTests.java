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

import static com.braintribe.utils.SysPrint.spOut;

import org.junit.Test;

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.test.stringifier.model.ConfidentialEntity;
import com.braintribe.model.query.Query;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class ConfidentialPropertyTests extends AbstractSelectQueryTests {

	private Query query;

	@Test
	public void simpleSelectQuery() {
		// @formatter:off
		 query = query()
				.from(ConfidentialEntity.T, "e")
				.where()
					.property("e", "password").eq("password123")
				.done();
		// @formatter:on

		assertResult("select * from com.braintribe.model.processing.query.test.stringifier.model.ConfidentialEntity e where e.password = ***");
	}

	@Test
	public void convolutedSelectQuery() {
		// @formatter:off
		query = query()
				.from(ConfidentialEntity.T, "e")
					.join("e","siblings", "ss")
					.join("ss","sibling", "s")
				.where()
					.property("s", "sibling.sibling.password").eq("password123")
				.done();
		// @formatter:on

		assertResult(
				"select * from com.braintribe.model.processing.query.test.stringifier.model.ConfidentialEntity e join e.siblings ss join ss.sibling s where s.sibling.sibling.password = ***");
	}

	@Test
	public void simpleEntityQuery() {
		// @formatter:off
		query = EntityQueryBuilder.from(ConfidentialEntity.T)
				.where()
					.property("password").eq("password123")
				.done();
		// @formatter:on

		assertResult("from com.braintribe.model.processing.query.test.stringifier.model.ConfidentialEntity where password = ***");
	}

	@Test
	public void simplePropertyQuery() {
		// @formatter:off
		query = PropertyQueryBuilder.forProperty(ConfidentialEntity.T, 1L, "siblings")
				.where()
					.property("password").eq("password123")
				.done();
		// @formatter:on

		assertResult(
				"property siblings of reference(com.braintribe.model.processing.query.test.stringifier.model.ConfidentialEntity, 1l) where password = ***");
	}

	private void assertResult(String expected) {
		String actual = query.stringify();
		spOut(actual);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

}
