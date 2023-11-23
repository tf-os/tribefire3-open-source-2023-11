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
package com.braintribe.model.access.smart.test.query;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.smart.query.planner.MultipleCollectionJoins_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class MultipleCollectionJoins_Tests extends AbstractSmartQueryTests {

	/**
	 * {@code select n1, n2 from SmartPersonA p join p.nickNamesSetA n1 join p.nickNamesSetA n2}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#doulbeExplicitSetJoin()
	 */
	@Test
	public void doulbeExplicitSetJoin() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesSetA", "n1")
					.join("p", "nickNamesSetA", "n2")
				.select("n1")
				.select("n2")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertRobBobCombinations();
	}

	/**
	 * {@code select n1, p.nickNamesSetA from SmartPersonA p join p.nickNamesSetA n1}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitAndImplicitSetJoin()
	 */
	@Test
	public void explicitAndImplicitSetJoin() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesSetA", "n1")
				.select("n1")
				.select("p", "nickNamesSetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertRobBobCombinations();
	}

	/**
	 * {@code select n1, p.nickNamesSetA from SmartPersonA p join p.nickNamesSetA n1 where 'Bob' in p.nickNamesSetA}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitAndImplicitSetJoin_WithCondition()
	 */
	@Test
	public void explicitAndImplicitSetJoin_WithCondition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("William").nickNamesA("Will", "Bill").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesSetA", "n1")
				.select("n1")
				.select("p", "nickNamesSetA")
				.where()
					.value("Bob").in().property("p", "nickNamesSetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertRobBobCombinations();
	}

	private void assertRobBobCombinations() {
		assertResultContains("Bob", "Bob");
		assertResultContains("Bob", "Rob");
		assertResultContains("Rob", "Bob");
		assertResultContains("Rob", "Rob");
		assertNoMoreResults();
	}

	/**
	 * {@code select n1, p.nickNamesSetA from SmartPersonA p join p.nickNamesSetA n1 where 'Bob' in p.nickNamesSetA and n1 = 'Robbie'}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitAndImplicitSetJoin_WithExplicitAndImplicitCondition()
	 */
	@Test
	public void explicitAndImplicitSetJoin_WithExplicitAndImplicitCondition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("Roberto").nickNamesA("Bob", "Bobby").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesSetA", "n1")
				.select("n1")
				.select("p", "nickNamesSetA")
				.where()
					.conjunction()
						.value("Bob").in().property("p", "nickNamesSetA")
						.entity("n1").eq("Rob")
					.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("Rob", "Bob");
		assertResultContains("Rob", "Rob");
		assertNoMoreResults();
	}

	/**
	 * {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.nameB in p.nickNamesSetA}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitSetJoin_WithNonDelegatableCondition()
	 */
	@Test
	public void explicitSetJoin_WithNonDelegatableCondition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("William").nickNamesA("Will", "Bill").create();
		bA.personA("Margareth").nickNamesA("Maggie", "Peggy").create();

		bB.personB("Bob").create();
		bB.personB("Will").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesSetA", "n1")
				.from(SmartPersonB.class, "pb")
				.select("n1")
				.where()
					.property("pb", "nameB").in().property("p", "nickNamesSetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("Rob");
		assertResultContains("Bob");
		assertResultContains("Will");
		assertResultContains("Bill");
		assertNoMoreResults();
	}

	/**
	 * {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.nameB in p.nickNamesSetA and 'Bob' in p.nickNamesSetA }
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitSetJoin_WithNonDelegatable_AndAlsoDelegatable_Condition()
	 */
	@Test
	public void explicitSetJoin_WithNonDelegatable_AndAlsoDelegatable_Condition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("William").nickNamesA("Will", "Bill").create();
		bA.personA("Margareth").nickNamesA("Maggie", "Peggy").create();

		bB.personB("Bob").create();
		bB.personB("Will").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesSetA", "n1")
				.from(SmartPersonB.class, "pb")
				.select("n1")
				.where()
					.conjunction()
						.property("pb", "nameB").in().property("p", "nickNamesSetA")
						.value("Bob").in().property("p", "nickNamesSetA")
					.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("Rob");
		assertResultContains("Bob");
		assertNoMoreResults();
	}

	// ###################################################
	// ## . . . . . Join Functions Conditions . . . . . ##
	// ###################################################

	/**
	 * {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.ageB = index(n1)}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitListJoin_WithDelegatable_ListIndexCondition()
	 */
	@Test
	public void explicitListJoin_WithDelegatable_ListIndexCondition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("William").nickNamesA("Will", "Bill").create();
		bA.personA("Margareth").nickNamesA("Maggie", "Peggy").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesListA", "n1")
				.select("n1")
				.where()
					.listIndex("n1").eq(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("Rob");
		assertResultContains("Bill");
		assertResultContains("Peggy");
		assertNoMoreResults();
	}

	/**
	 * {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.ageB = index(n1)}
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitListJoin_WithNonDelegatable_ListIndexCondition()
	 */

	@Test
	public void explicitListJoin_WithNonDelegatable_ListIndexCondition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("William").nickNamesA("Will", "Bill").create();

		bB.personB("Zero").ageB(0).create();
		bB.personB("One").ageB(1).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesListA", "n1")
				.from(SmartPersonB.class, "pb")
				.select("n1")
				.select("pb", "nameB")
				.where()
					.listIndex("n1").eq().property("pb", "ageB")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("Bob", "Zero");
		assertResultContains("Will", "Zero");
		assertResultContains("Rob", "One");
		assertResultContains("Bill", "One");
		assertNoMoreResults();
	}

	/**
	 * {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.ageB = index(n1) and 'Bob' in pb.nickNamesListA }
	 * 
	 * @see MultipleCollectionJoins_PlannerTests#explicitListJoin_WithNonDelegatableListIndex_AndOtherDelegatable_Condition()
	 */
	@Test
	public void explicitListJoin_WithNonDelegatableListIndex_AndOtherDelegatable_Condition() {
		bA.personA("Robert").nickNamesA("Bob", "Rob").create();
		bA.personA("William").nickNamesA("Will", "Bill").create();

		bB.personB("Zero").ageB(0).create();
		bB.personB("One").ageB(1).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "nickNamesListA", "n1")
				.from(SmartPersonB.class, "pb")
				.select("n1")
				.select("pb", "nameB")
				.where()
					.conjunction()
						.listIndex("n1").eq().property("pb", "ageB")
						.value("Bob").in().property("p", "nickNamesListA")
					.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("Bob", "Zero");
		assertResultContains("Rob", "One");
		assertNoMoreResults();
	}
}
