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
package com.braintribe.model.processing.query.test.normalization;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.ComparisonFailure;
import org.junit.Test;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.AbstractQueryBuilder;
import com.braintribe.model.processing.query.fluent.ConditionBuilder;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.normalization.QueryNormalizer;
import com.braintribe.model.query.Query;

/**
 * Provides tests for {@link QueryNormalizer}.
 * 
 * @author michael.lafite
 */
public class QueryNormalizerTest {

	private static final String ALIAS = "SAMPLE_ALIAS";
	private static final String PROPERTY = "SAMPLE_PROPERTY";
	private static final String VALUE = "SAMPLE_VALUE";
	private static final String PROPERTY2 = "SAMPLE_PROPERTY2";
	private static final String VALUE2 = "SAMPLE_VALUE2";

	@Test
	public void testJunctionNormalization() {
		check(sqcb().conjunction().close(), sqb());
		check(sqcb().disjunction().close(), sqb());
		check(sqcb().disjunction().conjunction().conjunction().close().close().close(), sqb());
		check(sqcb().conjunction().property(ALIAS, PROPERTY).eq(VALUE).close(),
				sqcb().property(ALIAS, PROPERTY).eq(VALUE));
		check(sqcb().disjunction().property(ALIAS, PROPERTY).eq(VALUE).close(),
				sqcb().property(ALIAS, PROPERTY).eq(VALUE));

		check(sqcb().disjunction().conjunction().property(ALIAS, PROPERTY).eq(VALUE).close().close(),
				sqcb().property(ALIAS, PROPERTY).eq(VALUE));

		check(sqcb().disjunction().conjunction().property(ALIAS, PROPERTY).eq(VALUE).property(ALIAS, PROPERTY2)
				.eq(VALUE2).close().close(),
				sqcb().conjunction().property(ALIAS, PROPERTY).eq(VALUE).property(ALIAS, PROPERTY2).eq(VALUE2).close());
		
		check(eqcb().disjunction().close(), eqb());
	}

	@Test
	public void testNegationNormalization() {
		check(sqcb().negation().conjunction().close(), sqb());
		check(sqcb().negation().disjunction().close(), sqb());
		check(sqcb().negation().conjunction().negation().disjunction().close().close(), sqb());
		check(sqcb().negation().negation().property(ALIAS, PROPERTY).eq(VALUE),
				sqcb().property(ALIAS, PROPERTY).eq(VALUE));
		check(sqcb().negation().conjunction().negation().disjunction().property(ALIAS, PROPERTY).eq(VALUE).close()
				.close(), sqcb().property(ALIAS, PROPERTY).eq(VALUE));
		check(sqcb().negation().negation().negation().negation().negation().property(ALIAS, PROPERTY).eq(VALUE), sqcb()
				.negation().property(ALIAS, PROPERTY).eq(VALUE));
		check(sqcb().negation().disjunction().negation().conjunction().property(ALIAS, PROPERTY).eq(VALUE)
				.property(ALIAS, PROPERTY2).eq(VALUE2).close().close(), sqcb().conjunction().property(ALIAS, PROPERTY)
				.eq(VALUE).property(ALIAS, PROPERTY2).eq(VALUE2).close());
	}

	private static void check(final AbstractQueryBuilder<?> originalQueryBuilder,
			final AbstractQueryBuilder<?> expectedNormalizedQueryBuilder) {
		final Query originalQuery = originalQueryBuilder.done();

		final Query expectedNormalizedQuery = expectedNormalizedQueryBuilder.done();
		final String expectedNormalizedQueryString = GMCoreTools.getDescription(expectedNormalizedQuery);

		final Query actualNormalizedQuery = QueryNormalizer.normalizeQuery(originalQuery);
		final String actualNormalizedQueryString = GMCoreTools.getDescription(actualNormalizedQuery);

		try {
			assertThat(actualNormalizedQueryString).isEqualTo(expectedNormalizedQueryString);
		} catch (final ComparisonFailure failure) {
			System.err.println("Comparison failure!");
			System.err.println("expected:");
			System.err.println(expectedNormalizedQueryString);
			System.err.println();
			System.err.println("actual:");
			System.err.println(actualNormalizedQueryString);
			throw failure;
		}
	}

	private static SelectQueryBuilder sqb() {
		return new SelectQueryBuilder().from(GenericEntity.class, ALIAS);
	}

	private static ConditionBuilder<SelectQueryBuilder> sqcb() {
		return sqb().where();
	}
	
	private static EntityQueryBuilder eqb() {
		return EntityQueryBuilder.from(GenericEntity.class);
	}

	private static ConditionBuilder<EntityQueryBuilder> eqcb() {
		return eqb().where();
	}
}
