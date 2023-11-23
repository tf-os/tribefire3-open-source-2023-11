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
package com.braintribe.model.processing.traversing.engine.impl.skip.conditional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.mpc.logic.MpcJunctionCapture;
import com.braintribe.model.mpc.value.MpcElementAxis;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.builder.api.MpcBuilder;
import com.braintribe.testing.category.KnownIssue;

/**
 * Tests for {@link MpcConfigurableSkipper}
 */
@Category(KnownIssue.class)
public class MpcConfigurableSkipperTest extends AbstractSkipperTest {

	static final IsTypeKind isListType = TypeConditions.isKind(TypeKind.listType);

	MpcConfigurableSkipper skipper;
	MpcBuilder $;

	@Before
	public void setup() {
		skipper = new MpcConfigurableSkipper();
		$ = MPC.builder();
	}
	
	@Test
	public void  testPropertyMatchListItem() throws Exception {
		ModelPathCondition condition = $.listItem();
		
		skipper.setCondition(condition);
		
		propertyMatchListItem(skipper);
	}
	
	@Test
	public void  testPropertyNameMatch() throws Exception {
		ModelPathCondition condition = $.property("someB");
		
		skipper.setCondition(condition);
		
		propertyNameMatch(skipper);
	}
	
	@Test
	public void  testMatchesTypeOfMatch() throws Exception {
		ModelPathCondition condition = $.matchesType(MpcElementAxis.value, isListType);
		
		skipper.setCondition(condition);
		
		matchesTypeOfMatch(skipper);
	}
	
	@Test
	public void  testNegationMatch() throws Exception {
		ModelPathCondition condition = $.negation(
											$.root()
										);
		
		skipper.setCondition(condition);
		
		negationMatch(skipper);
	}
	
	@Test
	public void  testSequenceMatch() throws Exception {
		ModelPathCondition condition = $.sequenceWithoutCapture($.root(),
																$.property("someB")
															);
		
		skipper.setCondition(condition);
		
		sequenceMatch(skipper);
	}
	
	@Test
	public void  testDisjunctionMatch() throws Exception {
		ModelPathCondition condition = $.disjunction(MpcJunctionCapture.none,
											$.property("enumA"),
											$.property("someB")
										);
		
		skipper.setCondition(condition);
		
		disjunctionMatch(skipper);
	}
		
	@Test
	public void  testConjunctionMatch() throws Exception {
		ModelPathCondition condition = $.conjunction(MpcJunctionCapture.none,
											$.matchesType(MpcElementAxis.value, isListType),
											$.property("list")
										);
		
		skipper.setCondition(condition);
		
		conjunctionMatch(skipper);
	}
	
}
