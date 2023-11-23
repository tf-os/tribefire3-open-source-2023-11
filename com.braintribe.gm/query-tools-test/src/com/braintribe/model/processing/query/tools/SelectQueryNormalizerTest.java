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
package com.braintribe.model.processing.query.tools;

import static com.braintribe.utils.junit.assertions.BtAssertions.assertThat;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.From;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * 
 */
public class SelectQueryNormalizerTest {

	@Test
	public void selectionsAreAdded() throws Exception {
		SelectQuery query = query().from(GenericEntity.T, "ge").done();
		assertThat(query.getSelections()).isNullOrEmpty();

		query = normalize(query);
		assertThat(query.getSelections()).hasSize(1);

		Object from = query.getSelections().get(0);
		assertThat(from.getClass()).isAssignableTo(From.class);
		assertThat(((From) from).getEntityTypeSignature()).isEqualTo(GenericEntity.class.getName());
	}

	/**
	 * There was a bug that would cause that the original query was modified (as collections were not cloned but the
	 * original value was taken).
	 */
	@Test
	public void originalQueryNotChanged() throws Exception {
		// @formatter:off
		SelectQuery query = query()
				.select("m", "types")
				.from(GmMetaModel.T, "m")
				.where()
					.property("m", "id").eq(1l)
				.done();
		// @formatter:on

		assertThat(query.getFroms().get(0).getJoins()).isNullOrEmpty();

		SelectQuery normalizedQuery = normalize(query);

		assertThat(query.getFroms().get(0).getJoins()).isNullOrEmpty();
		assertThat(normalizedQuery.getFroms().get(0).getJoins()).hasSize(1);
	}

	/**
	 * Since there is now way to express an empty collection as a GM value in many cases, we should generally treat null
	 * as an empty collection in a context where it makes sense.
	 */
	@Test
	public void inOperatorIsNullSafe() {
		// @formatter:off
		SelectQuery query = query()
				.from(GmMetaModel.T, "p")
					.select("p", "name")
					.where()
						.value("bob").in(null)
				.done();
		// @formatter:on

		query = normalize(query);

		ValueComparison vc = (ValueComparison) query.getRestriction().getCondition();
		assertThat(vc.getRightOperand()).isInstanceOf(Set.class);
	}

	private SelectQuery normalize(SelectQuery query) {
		return new SelectQueryNormalizer(query, false, false).normalize();
	}

	private SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

}
