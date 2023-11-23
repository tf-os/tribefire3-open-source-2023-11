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
package com.braintribe.model.generic.template;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;

public class TemplateTest {
	@Test
	public void testInvalidExpressions() throws Exception {
		testInvalidExpression("foo ${x");
		testInvalidExpression("foo ${");
		testInvalidExpression("foo ${}");
	}
	
	@Test
	public void testValidExpressions() throws Exception {
		testValidExpression("", true);
		testValidExpression("test", true, Pair.of("test", false));
		testValidExpression("Hello ${name}!", false, Pair.of("Hello ", false), Pair.of("name", true), Pair.of("!", false));
		testValidExpression("${name}", false, Pair.of("name", true));
		testValidExpression("$${name}", true, Pair.of("${name}", false));
		testValidExpression("$name", true, Pair.of("$name", false));
	}
	
	@SafeVarargs
	private final void testValidExpression(String expression, boolean staticOnly, Pair<String, Boolean>... fs) {
		Template expectedTemplate = buildTemplate(fs);
		Template template = Template.parse(expression);
		
		List<TemplateFragment> fragments = template.fragments();
		List<TemplateFragment> expectedFragments = expectedTemplate.fragments();
		
		Assertions.assertThat(fragments.size()).isEqualTo(expectedTemplate.fragments().size());
		
		boolean fail = false;
		
		for (int i = 0; i < fragments.size(); i++) {
			TemplateFragment fragment = fragments.get(i);
			TemplateFragment expectedFragment = expectedFragments.get(i);
			
			if (fragment.isPlaceholder() != expectedFragment.isPlaceholder()) {
				fail = true;
				break;
			}
			
			if (!fragment.getText().equals(expectedFragment.getText())) {
				fail = true;
				break;
			}
		}
		
		if (fail) {
			Assertions.fail("Expression was not parsed as expected: " + expression);
		}
		
		if (template.isStaticOnly() != staticOnly)
			Assertions.fail("Template was not conform to the expected isStaticOnly status: " + expression);
	}

	private void testInvalidExpression(String expression) {
		try {
			Template.parse(expression);
			Assertions.fail("Expression should have thrown an IllegalArgumentException: " + expression);
		}
		catch (IllegalArgumentException e) {
			// noop
		}
	}
	
	private Template buildTemplate(Pair<String, Boolean>... fs) {
		List<TemplateFragment> fragments = new ArrayList<>();
		
		for (Pair<String, Boolean> f: fs) {
			fragments.add(new TemplateFragment() {
				@Override
				public boolean isPlaceholder() {
					return f.getSecond();
				}
				@Override
				public String getText() {
					return f.getFirst();
				}
			});
		}
		
		return new Template() {
			
			@Override
			public List<TemplateFragment> fragments() {
				return fragments;
			}
			
			@Override
			public String expression() {
				return "<n/a>";
			}
		};
	}
}
