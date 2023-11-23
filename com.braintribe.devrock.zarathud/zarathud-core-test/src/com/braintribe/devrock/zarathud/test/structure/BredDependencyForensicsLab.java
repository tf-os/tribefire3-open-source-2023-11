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
package com.braintribe.devrock.zarathud.test.structure;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.zarathud.test.extraction.resolving.AbstractResolvingRunnerLab;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * apropos test-category : currently requires the artifacts to be present in the system's local repository..  will be changed to 
 * use the repolet eventually..
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class BredDependencyForensicsLab extends AbstractResolvingRunnerLab {


	@Test
	public void test_direct_aggregator_terminal() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-direct-aggregator-terminal#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> pair = test( terminal);
		Assert.assertTrue("unexpectedly, Zed hasn't retrieved any data for [" + terminal + "]", pair.first != null);
		Assert.assertTrue("Expected reported rating for ["+ terminal + "] was [NONE], found [" + pair.first.toString() + "]", pair.first == ForensicsRating.OK);
	}

	@Test
	public void test_indirect_aggregator_terminal() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-indirect-aggregator-terminal#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> pair = test( terminal);
		Assert.assertTrue("unexpectedly, Zed hasn't retrieved any data for [" + terminal + "]", pair.first != null);
		Assert.assertTrue("Expected reported rating for ["+ terminal + "] was [NONE], found [" + pair.first.toString() + "]", pair.first == ForensicsRating.OK);
	}
	
	@Test
	public void test_direct_hybrid_terminal() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-direct-hybrid-terminal#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> pair = test( terminal);
		Assert.assertTrue("unexpectedly, Zed hasn't retrieved any data for [" + terminal + "]", pair.first != null);
		Assert.assertTrue("Expected reported rating for ["+ terminal + "] was [NONE], found [" + pair.first.toString() + "]", pair.first == ForensicsRating.OK);
	}
	
	@Test
	public void test_indirect_hybrid_terminal() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-indirect-hybrid-terminal#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> pair = test( terminal);
		Assert.assertTrue("unexpectedly, Zed hasn't retrieved any data for [" + terminal + "]", pair.first != null);
		Assert.assertTrue("Expected reported rating for ["+ terminal + "] was [NONE], found [" + pair.first.toString() + "]", pair.first == ForensicsRating.OK);
	}
	
	
	@Test
	public void test_aggregator() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-aggregator-one#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> test = test( terminal);		
		Assert.assertTrue("unexpectedly, Zed has retrieved data from [" + terminal + "]", test.first == null && test.second == null);
	}
	
	@Test
	public void test_hybrid() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-hybrid-one#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> pair = test( terminal);
		Assert.assertTrue("unexpectedly, Zed hasn't retrieved any data for [" + terminal + "]", pair.first != null);
		Assert.assertTrue("Expected reported rating for ["+ terminal + "] was [NONE], found [" + pair.first.toString() + "]", pair.first == ForensicsRating.OK);
	}
	
	@Test
	public void test_perserve_terminal() {
		String terminal = "com.braintribe.devrock.test.zarathud:z-preserving-terminal#1.0.1-pc";
		Pair<ForensicsRating,Map<FingerPrint,ForensicsRating>> pair = test( terminal);
		Assert.assertTrue("unexpectedly, Zed hasn't retrieved any data for [" + terminal + "]", pair.first != null);
		Assert.assertTrue("Expected reported rating for ["+ terminal + "] was [NONE], found [" + pair.first.toString() + "]", pair.first == ForensicsRating.OK);
	}
	
}
