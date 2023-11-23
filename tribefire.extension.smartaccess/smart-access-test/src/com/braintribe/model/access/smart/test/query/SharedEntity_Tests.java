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

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.shared.SharedSource;
import com.braintribe.model.processing.query.smart.test.model.smart.shared.SmartSourceOwnerA;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * Tests for such queries which need to be split into multiple smart queries, cause the hierarchy rooted at given source
 * entity is not mapped to exactly one delegate hierarchy. The most simple example of such query is
 * <tt>select ge from GenericEntity ge</tt>.
 */
public class SharedEntity_Tests extends AbstractSmartQueryTests {

	/** Splitting the query and concatenating. */
	@Test
	public void selectSharedEntity() {
		SharedSource ss1 = bA.sharedSource("s1", accessIdA);
		SharedSource ss2 = bB.sharedSource("s2", accessIdB);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s")
				.from(SharedSource.class, "s")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		SharedSource sss1 = smartSharedSource(ss1);
		SharedSource sss2 = smartSharedSource(ss2);
		
		BtAssertions.assertThat(sss1).isNotSameAs(sss2);
		
		assertResultContains(sss1);
		assertResultContains(sss2);
		assertNoMoreResults();

	}

	/** This should be the same as any other normal join use-case. */
	@Test
	public void selectPropertyWhichIsSharedEntity() {
		SharedSource ss;
		ss = bB.sharedSource("s1", accessIdB);
		ss = bA.sharedSource("s2", accessIdA);

		bA.sourceOwnerA("so").sharedSource(ss).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s", "sharedSource")
				.from(SmartSourceOwnerA.class, "s")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartSharedSource(ss));
		assertNoMoreResults();
	}

	/** Should do query on itself; */
	@Test
	public void selectSharedProperty_kpaEntity() {
		SharedSource ss;
		ss = bA.sharedSource("s1", accessIdA);
		ss = bB.sharedSource("s2", accessIdB);

		bA.sourceOwnerA("so").kpaSharedSourceUuid(ss.getUuid()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s", "kpaSharedSource")
				.from(SmartSourceOwnerA.class, "s")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartSharedSource(ss));
		assertNoMoreResults();
	}

	/** Should do query on itself; */
	@Test
	@SuppressWarnings("unused")
	public void selectSharedProperty_kpaSet() {
		SharedSource ss1 = bA.sharedSource("s1", accessIdA);
		SharedSource ss2 = bB.sharedSource("s2", accessIdB);
		SharedSource ss3 = bB.sharedSource("s3", accessIdB);
		SharedSource ss4 = bB.sharedSource("s4", accessIdB);

		bA.sourceOwnerA("so").kpaSharedSourceUuidSet(ss1.getUuid(), ss2.getUuid()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("s", "kpaSharedSourceSet")
				.from(SmartSourceOwnerA.class, "s")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartSharedSource(ss1));
		assertResultContains(smartSharedSource(ss2));
		assertNoMoreResults();
	}

	// #####################################
	// ## . . . . . . HELPERS . . . . . . ##
	// #####################################

	protected SharedSource smartSharedSource(SharedSource ss) {
		return newInstance(SharedSource.T, ss.getId(), ss.getPartition());
	}

}
