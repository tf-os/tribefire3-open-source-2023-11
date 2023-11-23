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
package com.braintribe.model.access.hibernate.hql;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.SysPrint.spOut;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.model.acl.AclHaTestEntity;
import com.braintribe.model.access.hibernate.tests.Acl_HbmTest;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;

/**
 * Tests for {@link SelectHqlBuilder}
 */
public class SelectHqlBuilderTest {

	/**
	 * Complementary test to {@link Acl_HbmTest}s.
	 * <p>
	 * Checks that the ACL condition is encoded in an optimized way using an explicit nested query, rather than a disjunction.
	 * 
	 * @see DisjunctedInOptimizer
	 */
	@Test
	public void aclTest() throws Exception {
		SelectHqlBuilder hqlBuilder = new SelectHqlBuilder(aclQuery());
		hqlBuilder.buildHql();

		String hql = hqlBuilder.builder.toString();
		spOut(hql);

		// Asserting "exists (" substring checks the optimization kicked in
		// Actual correctness is tested in the Acl_HbmTest
		assertThat(hql).contains(" exists (");
	}

	private SelectQuery aclQuery() {
		String ACC = "acl.accessibility";

		// @formatter:off
		return new SelectQueryBuilder().from(AclHaTestEntity.T, "e") //
				.where()
					.conjunction()
						.disjunction()
						.property("e", "name").eq("WHATEVER") // irrelevant condition to make the query less trivial
							.value("G1").in().property("e", ACC)
							.value("G2").in().property("e", ACC)
						.close()
						.negation()
							.disjunction()
								.value("!D1").in().property("e",ACC)
								.value("!D2").in().property("e",ACC)
							.close()
					.close()
					.done();
		// @formatter:on
	}

}
