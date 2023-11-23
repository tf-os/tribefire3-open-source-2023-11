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

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.query.functions.aggregate.Average;
import com.braintribe.model.query.functions.aggregate.Count;
import com.braintribe.model.query.functions.aggregate.Max;
import com.braintribe.model.query.functions.aggregate.Min;
import com.braintribe.model.query.functions.aggregate.Sum;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.query.functions.value.Lower;
import com.braintribe.model.query.functions.value.Upper;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class EmptyClassesTests extends AbstractSelectQueryTests {
	@Test
	public void selectQueryWithEmptyClasses() {
		SelectQuery selectQuery = createEmptyClassSelectQuery();

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select * from <?> <?>, <?> <?> join <?> <?> where null = null order by null limit 0 offset 0");
	}

	@Test
	public void selectQueryWithEmptyClasses2() {
		SelectQuery selectQuery = createEmptyClassSelectQuery2();

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select * from <?> <?>");
	}

	@Test
	public void selectQueryWithEmptyClasses3() {
		SelectQuery selectQuery = createEmptyClassSelectQuery3();

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select *");
	}

	@Test
	public void selectQueryWithEmptyClasses4() {
		SelectQuery selectQuery = createEmptyClassSelectQuery4();

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase("select *");
	}

	@Test
	public void selectQueryWithEmptyClasses5() {
		SelectQuery selectQuery = createEmptyClassSelectQuery5();

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select null, typeSignature(null), listIndex(null), localize(null, null), mapKey(null), avg(null), count(null), max(null), min(null), sum(null), toString(null), concatenation(), lower(null), upper(null) where (() and fulltext(null, null) and not null)"));
	}

	@Test
	public void entityQueryWithEmptyClasses() {
		EntityQuery entityQuery = createEmptyEntityQuery();

		String queryString = stringify(entityQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase("from <?>");
	}

	@Test
	public void propertyQueryWithEmptyClasses() {
		PropertyQuery propertyQuery = createEmptyPropertyQuery();

		String queryString = stringify(propertyQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase("property null of null");
	}

	private static SelectQuery createEmptyClassSelectQuery() {
		SelectQuery query = SelectQuery.T.create();

		From from1 = From.T.create();
		From from2 = From.T.create();
		Join join = Join.T.create();

		from2.setJoins(new HashSet<Join>(Arrays.asList(new Join[] { join })));
		query.setFroms(Arrays.asList(new From[] { from1, from2 }));

		GroupBy groupBy = GroupBy.T.create();
		groupBy.setOperands(Arrays.asList(new Object[] {}));
		query.setGroupBy(groupBy);

		SimpleOrdering ordering = SimpleOrdering.T.create();
		query.setOrdering(ordering);

		ValueComparison condition = ValueComparison.T.create();
		Restriction restriction = Restriction.T.create();
		Paging paging = Paging.T.create();

		restriction.setCondition(condition);
		restriction.setPaging(paging);

		query.setRestriction(restriction);
		query.setSelections(Arrays.asList(new Object[] {}));

		return query;
	}

	@SuppressWarnings("unused")
	private static SelectQuery createEmptyClassSelectQuery2() {
		SelectQuery query = SelectQuery.T.create();

		From from = From.T.create();
		Join join = Join.T.create();

		from.setJoins(new HashSet<Join>(Arrays.asList(new Join[] {})));
		query.setFroms(Arrays.asList(new From[] { from }));

		GroupBy groupBy = GroupBy.T.create();
		query.setGroupBy(groupBy);

		CascadedOrdering ordering = CascadedOrdering.T.create();
		ordering.setOrderings(Arrays.asList(new SimpleOrdering[] {}));
		query.setOrdering(ordering);

		Restriction restriction = Restriction.T.create();
		query.setRestriction(restriction);

		return query;
	}

	private static SelectQuery createEmptyClassSelectQuery3() {
		SelectQuery query = SelectQuery.T.create();
		query.setFroms(Arrays.asList(new From[] {}));

		CascadedOrdering ordering = CascadedOrdering.T.create();
		query.setOrdering(ordering);

		return query;
	}

	private static SelectQuery createEmptyClassSelectQuery4() {
		SelectQuery query = SelectQuery.T.create();
		return query;
	}

	private static SelectQuery createEmptyClassSelectQuery5() {
		SelectQuery query = SelectQuery.T.create();

		PropertyOperand propertyOperand = PropertyOperand.T.create();
		EntitySignature entitySignature = EntitySignature.T.create();
		ListIndex listIndex = ListIndex.T.create();
		Localize localize = Localize.T.create();
		MapKey mapKey = MapKey.T.create();
		Average average = Average.T.create();
		Count count = Count.T.create();
		Max max = Max.T.create();
		Min min = Min.T.create();
		Sum sum = Sum.T.create();
		AsString asString = AsString.T.create();
		Concatenation concatenation = Concatenation.T.create();
		Lower lower = Lower.T.create();
		Upper upper = Upper.T.create();

		query.setSelections(Arrays.asList(new Object[] { propertyOperand, entitySignature, listIndex, localize, mapKey, average, count, max, min, sum,
				asString, concatenation, lower, upper }));

		Disjunction disjunction = Disjunction.T.create();
		FulltextComparison fulltextComparison = FulltextComparison.T.create();
		Negation negation = Negation.T.create();

		Conjunction conjunction = Conjunction.T.create();
		conjunction.setOperands(Arrays.asList(new Condition[] { disjunction, fulltextComparison, negation }));

		Restriction restriction = Restriction.T.create();
		restriction.setCondition(conjunction);
		query.setRestriction(restriction);

		return query;
	}

	private static EntityQuery createEmptyEntityQuery() {
		EntityQuery query = EntityQuery.T.create();
		return query;
	}

	private static PropertyQuery createEmptyPropertyQuery() {
		PropertyQuery query = PropertyQuery.T.create();
		return query;
	}
}
