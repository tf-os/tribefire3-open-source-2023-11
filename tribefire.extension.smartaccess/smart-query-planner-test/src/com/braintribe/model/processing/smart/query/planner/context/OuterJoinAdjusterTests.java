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
package com.braintribe.model.processing.smart.query.planner.context;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ConditionType;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class OuterJoinAdjusterTests {

	private Condition c;

	private static final String INNER = JoinType.inner.toString();
	private static final String RIGHT = JoinType.right.toString();
	private static final String FULL = JoinType.full.toString();
	
	@Test
	public void dirctProperty_LeftJoin() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", INNER, "j", JoinType.left)
				.where()
					.property("j", "p").eq(45)
		.done());
		// @formatter:on

		runTestForConfiguredCondition();
	}

	@Test
	public void dirctProperty_FullJoin() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", RIGHT, "j", JoinType.full)
				.where()
					.property("j", "p").eq(45)
		.done());
		// @formatter:on
		
		runTestForConfiguredCondition();
	}

	@Test
	public void dirctProperty_OnlyWhenValueNotNull() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", FULL, "j", JoinType.full)
				.where()
					.property("j", "p").eq(null)
		.done());
		// @formatter:on

		runTestForConfiguredCondition();
	}

	@Test
	public void twoNullableJoinsCmpared() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", FULL, "j1", JoinType.full)
					.join("f", RIGHT, "j2", JoinType.right)
				.where()
					.property("j1", "p").eq().property("j2", "p")
		.done());
		// @formatter:on
		
		runTestForConfiguredCondition();
	}

	@Test
	public void oneJoinBecomesNonnullableAsDependencyOfAnother() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", INNER, "j1", JoinType.left)
					.join("f", INNER, "j2", JoinType.left)
				.where()
					.conjunction()
						.property("j1", "q").ilike("xxx")
						.property("j1", "p").eq().property("j2", "p")
					.close()
				.done());
		// @formatter:on
		
		runTestForConfiguredCondition();
	}

	@Test
	public void negationCanNeverMeanNonNullable() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", FULL, "j", JoinType.full)
				.where()
					.conjunction()
						.negation().property("j", "q").like("yy")
						.negation().property("j", "q").ilike("xxx")
					.close()
				.done());
		// @formatter:on
		
		runTestForConfiguredCondition();
	}
	
	@Test
	public void disjunction() throws Exception {
		// @formatter:off
		c = extractCondition(
				query().from(GenericEntity.T, "f")
					.join("f", INNER, "j1", JoinType.left)
					.join("f", FULL, "j2", JoinType.full)
				.where()
					.disjunction()
						.conjunction()
							.property("j1", "q").eq(49)
							.property("j2", "p").eq(28)
						.close()
						.conjunction()
							.property("j1", "q").eq(99)
							.close()
					.close()
				.done());
		// @formatter:on
		
		runTestForConfiguredCondition();
	}
	
	private void runTestForConfiguredCondition() {
		adjustJoins();
		assertJoinTypeMatchesPropertyName();
	}
	
	// #######################################
	// ## . . . . . . Adjusting . . . . . . ##
	// #######################################

	private void adjustJoins() {
		List<Condition> conjunctionOperands = extractConjunctionOperands();
		OuterJoinAdjuster.run(conjunctionOperands);
	}

	private List<Condition> extractConjunctionOperands() {
		if (c.conditionType() == ConditionType.conjunction) {
			return ((Conjunction) c).getOperands();

		} else {
			return Arrays.asList(c);
		}
	}

	// #######################################
	// ## . . . . . . Assertions . . . . . .##
	// #######################################
	private void assertJoinTypeMatchesPropertyName() {
		Set<Join> allJoins = findAllJoins();

		for (Join join: allJoins) {
			BtAssertions.assertThat(join.getJoinType().toString()).isEqualTo(join.getProperty());
		}
	}

	private Set<Join> findAllJoins() {
		final Set<Join> result = CollectionTools2.newSet();

		StandardTraversingContext tc = new StandardTraversingContext() {
			@Override
			public void registerAsVisited(GenericEntity entity, Object associate) {
				if (entity instanceof Join) {
					result.add((Join) entity);
				}
				super.registerAsVisited(entity, associate);
			}
		};

		BaseType.INSTANCE.traverse(tc, c);
		return result;
	}

	// #######################################
	// ## . . . . . . . Helpers . . . . . . ##
	// #######################################

	@SuppressWarnings("unchecked")
	private <C extends Condition> C extractCondition(SelectQuery query) {
		return (C) query.getRestriction().getCondition();
	}

	private SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

}
