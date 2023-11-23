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
package com.braintribe.model.processing.smood.querying;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class FulltextQueryTests extends AbstractSmoodTests {

	private List<GenericEntity> entities;

	@Test
	public void queryEntitiesWithFulltext() throws Exception {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().fullText(null, "J").done();
		queryEntities(query);

		BtAssertions.assertThat(entities).containsOnly(p1, p2);
	}

	@Test
	public void queryEntitiesWithFulltextOnLS() throws Exception {
		Person p1 = b.person("Jack").localizedString("default", "First_LS").create();
		Person p2 = b.person("John").localizedString("default", "Second_LS").create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().fullText(null, "_LS").done();
		queryEntities(query);

		BtAssertions.assertThat(entities).containsOnly(p1, p2);
	}

	private void queryEntities(EntityQuery query) throws ModelAccessException {
		entities = smood.queryEntities(query).getEntities();
	}

}
