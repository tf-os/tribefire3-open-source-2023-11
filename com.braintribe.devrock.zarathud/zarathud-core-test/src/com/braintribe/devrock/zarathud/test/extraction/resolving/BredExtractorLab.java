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
package com.braintribe.devrock.zarathud.test.extraction.resolving;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class BredExtractorLab extends AbstractResolvingRunnerLab {
	
	
	@Test
	public void testZOne() {		
		test( "com.braintribe.devrock.test.zarathud:z-one#[,]");
	}
	
	@Test
	public void testZParams() {
		test( "com.braintribe.devrock.test.zarathud:z-params#[,]");
	}

	@Test
	public void testZAnnotations() {
		test( "com.braintribe.devrock.test.zarathud:z-annotations#[,]");
	}

	@Test
	public void testZTwo() {
		test( "com.braintribe.devrock.test.zarathud:z-two#[,]");
	}
	
	@Test
	public void testZThree() {
		test( "com.braintribe.devrock.test.zarathud:z-three#[,]");
	}
	@Test
	public void testZFour() {
		test(  "com.braintribe.devrock.test.zarathud:z-four#[,]");
	}
	
	@Test
	public void testZFive() {
		test(  "com.braintribe.devrock.test.zarathud:z-five#[,]");
	}
	
	@Test
	public void testZSix() {
		test( "com.braintribe.devrock.test.zarathud:z-six#[,]");
	}
	@Test
	public void testZSeven() {
		test(  "com.braintribe.devrock.test.zarathud:z-seven#[,]");
	}


	//@Test
	public void testZScratch() {
		test( "com.braintribe.devrock.test.zarathud:z-scratch#[,]");
	}
	
	@Test
	public void testZInners() {
		test( "com.braintribe.devrock.test.zarathud:z-inners#[,]");
	}
	
	
	@Test
	public void testZSuppress() {
		test(  "com.braintribe.devrock.test.zarathud:z-suppress#[,]");
	}
	

	@Test
	public void testZModelOne() {
		test( "com.braintribe.devrock.test.zarathud:z-model-one#[,]");
	}
	@Test
	public void testZModelTwo() {
		test( "com.braintribe.devrock.test.zarathud:z-model-two#[,]");
	}
	
	@Test
	public void testZAggregator() {
		test( "com.braintribe.devrock.test.zarathud:z-direct-aggregator-terminal#[,]");
	}
	
	public static void main (String [] args) {
		if (args.length == 0){
			System.err.println("parameters : <name>\nwhere <name> is a valid condensed artifact name");
			return;
		}		
		String condensedName = args[0];				
		new BredExtractorLab().test(condensedName);
	}
}
