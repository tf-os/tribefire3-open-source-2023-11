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

import com.braintribe.model.processing.query.smart.test.model.accessA.discriminator.DiscriminatorEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorBase;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType2;
import com.braintribe.model.processing.smart.query.planner.DiscriminatorEntity_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class DiscriminatorEntity_Tests extends AbstractSmartQueryTests {

	private static final String DISC_OTHER = "other";

	/** @see DiscriminatorEntity_PlannerTests#selectPropert_HierarchyBase() */
	@Test
	public void selectPropert_HierarchyBase() {
		bA.discriminatorEntityA("t1", SmartDiscriminatorType1.DISC_TYPE1).create();
		bA.discriminatorEntityA("t2", SmartDiscriminatorType2.DISC_TYPE2).create();
		bA.discriminatorEntityA("other", DISC_OTHER).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.class, "e")
				.select("e", "name")
				.orderBy().property("e", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("t1");
		assertResultContains("t2");
		assertNoMoreResults();
	}

	/** @see DiscriminatorEntity_PlannerTests#selectPropert_HierarchyLeaf() */
	@Test
	public void selectPropert_HierarchyLeaf() {
		bA.discriminatorEntityA("t1", SmartDiscriminatorType1.DISC_TYPE1).type1Name("t1Name").create();
		bA.discriminatorEntityA("t2", SmartDiscriminatorType2.DISC_TYPE2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorType1.class, "e")
				.select("e", "type1Name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("t1Name");
		assertNoMoreResults();
	}

	/** @see DiscriminatorEntity_PlannerTests#selectTheDiscriminatorProperty() */
	@Test
	public void selectTheDiscriminatorProperty() {
		bA.discriminatorEntityA("t1", SmartDiscriminatorType1.DISC_TYPE1).type1Name("t1Name").create();
		bA.discriminatorEntityA("t2", SmartDiscriminatorType2.DISC_TYPE2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorType2.class, "e")
				.select("e", "discriminator")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(SmartDiscriminatorType2.DISC_TYPE2);
		assertNoMoreResults();
	}

	/** @see DiscriminatorEntity_PlannerTests#selectEntity_HierarchyLeaf() */
	@Test
	public void selectEntity_HierarchyLeaf() {
		DiscriminatorEntityA e;
		e = bA.discriminatorEntityA("t2", SmartDiscriminatorType2.DISC_TYPE2).create();
		e = bA.discriminatorEntityA("t1", SmartDiscriminatorType1.DISC_TYPE1).type1Name("t1Name").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorType1.class, "e")
				.select("e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartDiscriminatorType1(e));
		assertNoMoreResults();
	}

	/** @see DiscriminatorEntity_PlannerTests#selectEntity_HierarchyBase() */
	@Test
	public void selectEntity_HierarchyBase() {
		DiscriminatorEntityA e1 = bA.discriminatorEntityA("t1", SmartDiscriminatorType1.DISC_TYPE1).type1Name("t1Name").create();
		DiscriminatorEntityA e2 = bA.discriminatorEntityA("t2", SmartDiscriminatorType2.DISC_TYPE2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.class, "e")
				.select("e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartDiscriminatorType1(e1));
		assertResultContains(smartDiscriminatorType2(e2));
		assertNoMoreResults();
	}

	/** @see DiscriminatorEntity_PlannerTests#selectTypeSignature() */
	@Test
	public void selectTypeSignature() {
		bA.discriminatorEntityA("t1", SmartDiscriminatorType1.DISC_TYPE1).create();
		bA.discriminatorEntityA("t2", SmartDiscriminatorType2.DISC_TYPE2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.class, "e")
				.select().entitySignature().entity("e")
				.done();
		// @formatter:on

		evaluate(selectQuery);
		
		assertResultContains(SmartDiscriminatorType1.class.getName());
		assertResultContains(SmartDiscriminatorType2.class.getName());
		assertNoMoreResults();
	}

	/** @see DiscriminatorEntity_PlannerTests#selectEntity_ConditionOnType() */
	@Test
	public void selectEntity_ConditionOnType() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartDiscriminatorBase.class, "e")
				.select("e")
				.where()
					.entitySignature("e").eq(SmartDiscriminatorType1.class.getName())
				.done();
		// @formatter:on

		evaluate(selectQuery);

		// TODO FINISH
	}

}
