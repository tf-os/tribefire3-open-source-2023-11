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
package com.braintribe.model.processing.vde.impl.bvd.logic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.bvd.logic.Disjunction;
import com.braintribe.model.bvd.logic.Negation;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.logic.DisjunctionVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * Provides tests for {@link DisjunctionVde}.
 * 
 */
public class DisjunctionVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator(); 
	
	@Test
	public void testNullOperandDisjunction() throws Exception {

		Disjunction logic = $.disjunction();

		Object result = evaluate(logic);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void testEmptyOperandDisjunction() throws Exception {

		Disjunction logic = $.disjunction();
		logic.setOperands(new ArrayList<Object>());
		
		Object result = evaluate(logic);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void testMultipleSimpleOperandDisjunction() throws Exception {

		Disjunction logic = $.disjunction();
		ArrayList<Object> operands = new ArrayList<Object>();
		operands.add(true);
		operands.add(false);
		operands.add(true);
		logic.setOperands(operands);
		
		Object result = evaluate(logic);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void testMultipleOperandDisjunction() throws Exception {

		Disjunction logic = $.disjunction();
		Negation negation = Negation.T.create();
		negation.setOperand(true);
		
		ArrayList<Object> operands = new ArrayList<Object>();
		operands.add(false);
		operands.add(negation);
		operands.add(false);
		logic.setOperands(operands);
		
		Object result = evaluate(logic);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Boolean.class);
		assertThat(result).isEqualTo(false);
	}
	
	@Test  (expected= VdeRuntimeException.class)
	public void testMultipleOperandDisjunctionFail() throws Exception {

		Disjunction logic = $.disjunction();
		
		ArrayList<Object> operands = new ArrayList<Object>();
		operands.add(true);
		operands.add(new Date()); // only object that evaluate to Boolean allowed
		operands.add(true);
		logic.setOperands(operands);
		
		evaluate(logic);
	}
}
