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
package com.braintribe.model.processing.vde.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.parser.TemplateStringParser;

public class TemplateStringParserTest {
	@Test 
	public void testDollarNonBracketNonName() {
		String subjects[] = {
				"$(name)",
				"$[name]",
				"$!"};

		for (String subject: subjects) {
			Object result = TemplateStringParser.parse(subject);
			assertThat(result).isEqualTo(subject);
		}
	}
	
	@Test
	public void testTemplateParsing() {
		Object o1 = TemplateStringParser.parse("");
		Object o2 = TemplateStringParser.parse("Hallo $name");
		Object o3 = TemplateStringParser.parse("$name");
		Object o4 = TemplateStringParser.parse("Just text");
		Object o5 = TemplateStringParser.parse("Escape $${ Test");
		
		assertThat(o1).isEqualTo("");
		assertThat(o2).isInstanceOf(Concatenation.class);
		assertThat(o3).isInstanceOf(Variable.class);
		assertThat(o4).isEqualTo("Just text");
		assertThat(o5).isEqualTo("Escape ${ Test");
		
		Concatenation c = (Concatenation)o2;
		
		List<Object> operands = c.getOperands();
		assertThat(operands.size()).isEqualTo(2);
		
		assertThat(operands.get(0)).isInstanceOf(String.class);
		assertThat(operands.get(1)).isInstanceOf(Variable.class);
		
		
	}
	
	@Test(expected = IllegalStateException.class)
	public void testTemplateParsingUnfinishedBracedVar() {
		TemplateStringParser.parse("Hallo${egal");
	}
	
	@Test(expected = IllegalStateException.class)
	public void testTemplateParsingIncompleteVarAtEnd() {
		TemplateStringParser.parse("Hallo$");
	}
}
